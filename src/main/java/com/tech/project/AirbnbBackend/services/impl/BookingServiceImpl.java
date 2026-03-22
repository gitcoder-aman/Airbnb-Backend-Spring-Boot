package com.tech.project.AirbnbBackend.services.impl;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.tech.project.AirbnbBackend.dto.BookingDto;
import com.tech.project.AirbnbBackend.dto.BookingRequest;
import com.tech.project.AirbnbBackend.dto.GuestDto;
import com.tech.project.AirbnbBackend.dto.HotelReportDto;
import com.tech.project.AirbnbBackend.entities.*;
import com.tech.project.AirbnbBackend.entities.enums.BookingStatus;
import com.tech.project.AirbnbBackend.exception.BookingExpiredException;
import com.tech.project.AirbnbBackend.exception.ResourceNotFoundException;
import com.tech.project.AirbnbBackend.exception.UnAuthorisedException;
import com.tech.project.AirbnbBackend.repositories.*;
import com.tech.project.AirbnbBackend.services.BookingExpirationManager;
import com.tech.project.AirbnbBackend.services.BookingService;
import com.tech.project.AirbnbBackend.services.CheckoutService;
import com.tech.project.AirbnbBackend.strategy.PriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.tech.project.AirbnbBackend.utils.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final BookingExpirationManager expirationManager;
    private final int BOOKING_EXPIRATION_TIME_IN_MINUTES = 10;
    private final CheckoutService checkoutService;
    private final PriceService priceService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    @Override
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {

        log.info("Initialising Booking for hotel : {},room: {},date:{}-{}", bookingRequest.getHotelId(), bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
        Hotel hotel = hotelRepository
                .findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + bookingRequest.getHotelId()));

        Room room = roomRepository
                .findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID" + bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getNumberOfRooms());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckInDate()) + 1;
        log.info("inventoryList size:{}", inventoryList);
        log.info("dayCount size:{}", inventoryList);

        if (inventoryList.size() < daysCount) {
            throw new IllegalStateException("Room is not available anymore");
        }

        //Reserve the room/update the booked count of inventories
        inventoryRepository.initBooking(
                room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getNumberOfRooms()
        );

        //calculate total price
        BigDecimal priceForOneRoom = priceService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getNumberOfRooms()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomCount(bookingRequest.getNumberOfRooms())
                .amount(totalPrice)
                .build();

        Booking saveBookingData = bookingRepository.save(booking);
        return modelMapper.map(saveBookingData, BookingDto.class);
    }

    @Transactional
    @Override
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {

        log.info("Adding Guests for  Booking with Id:{} ", bookingId);

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID " + bookingId));

        User user = getCurrentUser();
        log.info("Current User Details:{}", user.getId());
        log.info("Current User Details:{}", user.getName());

        log.info("Current Booking User Details:{}", booking.getUser().getId());
        log.info("Current Booking User Details:{}", booking.getUser().getName());

        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnAuthorisedException("Booking does not belong to this user with id: " + user.getId());
        }

        if (hasBookingExpired(booking)) {
            expirationManager.doBookingStatusExpired(bookingId);

            throw new BookingExpiredException("Booking has already expired");
        }
        if (booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Booking is not under reserved state , cannot add guest");
        }
        for (GuestDto guestDto : guestDtoList) {
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Transactional
    @Override
    public String initiatePayment(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("This Booking rooms have already confirmed.try another");
        }
        User user = getCurrentUser();
        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnAuthorisedException("Booking does not belong to this user with id: " + user.getId());
        }

        if (hasBookingExpired(booking)) {
            expirationManager.doBookingStatusExpired(bookingId);
            throw new BookingExpiredException("Booking has already expired");
        }
        String checkOutSession = checkoutService.getCheckOutSession(booking, frontendUrl + "/payments/success", frontendUrl + "/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return checkOutSession;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {

        log.info("event {}", event.getType());

        if (!"checkout.session.completed".equals(event.getType())) {
            log.warn("Unhandled event type: {}", event.getType());
            return;
        }

        //  Step 1: Deserialize safely
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        StripeObject stripeObject;

        if (deserializer.getObject().isPresent()) {
            stripeObject = deserializer.getObject().get();
        } else {
            // fallback for Stripe v28+
            try {
                stripeObject = deserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException e) {
                throw new RuntimeException(e);
            }
        }

        if (!(stripeObject instanceof Session session)) {
            throw new RuntimeException("Failed to deserialize Checkout Session");
        }

        String sessionId = session.getId();
        log.info("Session ID: {}", sessionId);

        //  Step 2: Retrieve booking
        Booking booking = bookingRepository
                .findByPaymentSessionId(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found for session ID: " + sessionId));

        //  Step 3: Confirm booking
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        //  Step 4: Lock inventory
        List<Inventory> lockedInventory = inventoryRepository.findAndLockReservedInventory(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomCount()
        );

        if (lockedInventory.isEmpty()) {
            throw new RuntimeException("Inventory not available");
        }

        log.info("RoomCount: {}", booking.getRoomCount());
        int updatedRows = inventoryRepository.confirmBooking(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomCount()
        );
        if (updatedRows == 0) {
            throw new RuntimeException("Inventory update failed - conditions not met");
        }

        log.info("Booking confirmed successfully for Booking ID: {}", booking.getId());
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        log.info("Cancel Booking for  Booking with Id:{} ", bookingId);

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID " + bookingId));

        User user = getCurrentUser();

        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnAuthorisedException("Booking does not belong to this user with id: " + user.getId());
        }
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only Confirmed Booking can be cancelled");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        List<Inventory> lockedInventory = inventoryRepository.findAndLockReservedInventory(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomCount()
        );
        if (lockedInventory.isEmpty()) {
            throw new RuntimeException("Inventory not available");
        }
        int updatedRows = inventoryRepository.cancelBooking(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomCount()
        );
        if (updatedRows == 0) {
            throw new RuntimeException("Inventory update failed - conditions not met");
        }
        //handle the refund
        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundCreateParams);

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID " + bookingId));

        User user = getCurrentUser();

        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnAuthorisedException("Booking does not belong to this user with id: " + user.getId());
        }
        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with hotel Id: " + hotelId));
        User user = getCurrentUser();

        log.info("Getting all bookings for the hotel with id: {}", hotelId);

        if (!user.equals(hotel.getOwner())) {
            throw new AccessDeniedException("You are not the owner of hotel with id: " + hotelId);
        }
        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        return bookings.stream()
                .map((element) -> modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with hotel Id: " + hotelId));
        User user = getCurrentUser();

        log.info("Generating Report for the hotel with id: {}", hotelId);

        if (!user.equals(hotel.getOwner())) {
            throw new AccessDeniedException("You are not the owner of hotel with id: " + hotelId);
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);
        long totalConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenuesOfConfirmedBooking = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenues = totalConfirmedBookings == 0 ? BigDecimal.ZERO :
                totalRevenuesOfConfirmedBooking.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);
        return new HotelReportDto(totalConfirmedBookings, totalRevenuesOfConfirmedBooking, avgRevenues);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();

        return bookingRepository.findByUser(user)
                .stream()
                .map((element) -> modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void updateBookingStatusInCheckIn(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        LocalDate now = LocalDate.now();

        //  Too early
        if (now.isBefore(booking.getCheckInDate())) {
            throw new RuntimeException("Too early to check-in");
        }

        //  Too late
        if (now.isAfter(booking.getCheckOutDate())) {
            throw new RuntimeException("Check-in time expired");
        }

        //  Already checked-in
        if (booking.getBookingStatus() == BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Already checked-in");
        }

        //  Wrong status
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Only confirmed bookings can be checked-in");
        }

        //  Update
        booking.setBookingStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
    }

    @Scheduled(cron = "0 0 * * * *") // every hour  // this is updated by system
//    @Scheduled(cron = "0 * * * * *") // every minute
    public void updateCompletedBookings() {
        List<Booking> bookings = bookingRepository
                .findByBookingStatusAndCheckOutDateBefore(
                        BookingStatus.CHECKED_IN,
                        LocalDate.now()
                );

        for (Booking booking : bookings) {
            booking.setBookingStatus(BookingStatus.COMPLETED);
        }

        bookingRepository.saveAll(bookings);
    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(BOOKING_EXPIRATION_TIME_IN_MINUTES).isBefore(LocalDateTime.now());
    }
}

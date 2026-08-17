import client from './client';

export const authApi = {
  signup: (payload) => client.post('/v1/auth/signup', payload),
  login: (payload) => client.post('/v1/auth/login', payload),
  refresh: () => client.post('/v1/auth/refresh'),
};

export const userApi = {
  getProfile: () => client.get('/v1/users/profile'),
  updateProfile: (payload) => client.put('/v1/users/profile', payload),
};

export const hotelApi = {
  getAllHotels: (page = 0, size = 10) =>
    client.get('/v1/hotels', { params: { page, size } }),
  searchHotels: (criteria) => client.post('/v1/hotels/search', criteria),
  getHotelInfo: (hotelId) => client.get(`/v1/hotels/${hotelId}/info`),
  getRoomsByHotel: (hotelId, checkInDate, checkOutDate) =>
    client.get(`/v1/hotels/${hotelId}/rooms`, {
      params: { checkInDate, checkOutDate },
    }),
};

export const bookingApi = {
  initBooking: (payload) => client.post('/v1/bookings/init', payload),
  addGuests: (bookingId, guests) =>
    client.post(`/v1/bookings/${bookingId}/addGuests`, guests),
  initiatePayment: (bookingId) =>
    client.post(`/v1/bookings/${bookingId}/payment`),
  cancelBooking: (bookingId) =>
    client.post(`/v1/bookings/${bookingId}/cancel`),
  expireBooking: (bookingId) =>
    client.post(`/v1/bookings/${bookingId}/expire`),
  getBookingStatus: (bookingId) =>
    client.post(`/v1/bookings/${bookingId}/status`),
  getMyBookings: () => client.get('/v1/bookings/myBookings'),
  getAllOwnerBookings: () => client.get('/v1/bookings/all'),
  checkIn: (bookingId) => client.post(`/v1/bookings/${bookingId}/check-in`),
};

export const reviewApi = {
  createReview: (roomId, payload) =>
    client.post(`/v1/rooms/${roomId}/reviews`, payload),
  getReviews: (params) => client.get('/v1/reviews', { params }),
  updateReview: (reviewId, payload) =>
    client.put(`/v1/reviews/${reviewId}`, payload),
  deleteReview: (reviewId) => client.delete(`/v1/reviews/${reviewId}`),
  hasCompletedBookingForReview: (roomId) =>
    client.get(`/v1/reviews/has-completed-booking/${roomId}`),
};

export const adminApi = {
  createHotel: (payload) => client.post('/v1/admin/hotels', payload),
  getHotelById: (hotelId) => client.get(`/v1/admin/hotels/${hotelId}`),
  updateHotel: (hotelId, payload) =>
    client.put(`/v1/admin/hotels/${hotelId}`, payload),
  updatePartialHotel: (hotelId, payload) =>
    client.patch(`/v1/admin/hotels/${hotelId}`, payload),
  deleteHotel: (hotelId) => client.delete(`/v1/admin/hotels/${hotelId}`),
  activateHotel: (hotelId) =>
    client.patch(`/v1/admin/hotels/activate/${hotelId}`),
  getMyHotels: () => client.get('/v1/admin/hotels'),
  getHotelBookings: (hotelId) =>
    client.get(`/v1/admin/hotels/${hotelId}/bookings`),
  getHotelReport: (hotelId, startDate, endDate) =>
    client.get(`/v1/admin/hotels/${hotelId}/reports`, {
      params: { startDate, endDate },
    }),
};

export const adminRoomApi = {
  createRoom: (hotelId, payload) =>
    client.post(`/v1/admin/hotels/${hotelId}/rooms`, payload),
  getRoomsInHotel: (hotelId) =>
    client.get(`/v1/admin/hotels/${hotelId}/rooms`),
  getRoomById: (hotelId, roomId) =>
    client.get(`/v1/admin/hotels/${hotelId}/rooms/${roomId}`),
  updateRoom: (hotelId, roomId, payload) =>
    client.put(`/v1/admin/hotels/${hotelId}/rooms/${roomId}`, payload),
  deleteRoom: (hotelId, roomId) =>
    client.delete(`/v1/admin/hotels/${hotelId}/rooms/${roomId}`),
};

export const adminInventoryApi = {
  getInventoryByRoom: (roomId) =>
    client.get(`/v1/admin/inventory/room/${roomId}`),
  updateInventory: (roomId, payload) =>
    client.patch(`/v1/admin/inventory/room/${roomId}`, payload),
};

const textBody = (value) => ({
  data: value,
  headers: { 'Content-Type': 'text/plain' },
});

const postText = (url, value) => {
  const { data, headers } = textBody(value);
  return client.post(url, data, { headers });
};

export const aiApi = {
  chat: (question) => postText('/v1/ai/chat', question),
  search: (prompt) => postText('/v1/ai/search', prompt),
  hotelDetail: (prompt) => postText('/v1/ai/hotel-detail', prompt),
};
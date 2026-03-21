-- Step 1: Drop old constraint (use correct name if different)
ALTER TABLE booking
DROP CONSTRAINT IF EXISTS booking_booking_status_check;

-- Step 2: Add new constraint with updated enum values
ALTER TABLE booking
    ADD CONSTRAINT booking_booking_status_check
        CHECK (
            booking_status IN (
                               'RESERVED',
                               'GUEST_ADDED',
                               'PAYMENT_PENDING',
                               'CONFIRMED',
                               'CHECKED_IN',
                               'COMPLETED',
                               'CANCELLED',
                               'EXPIRED'
                )
            );
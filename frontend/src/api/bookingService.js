import { api } from './client';

// POST /api/bookings
// payload: { customerId, carId, pickupHubId, dropHubId, pickupDatetime,
//            returnDatetime, addons: [{ addonId, quantity }], estimatedAmount, remarks }
//
// Note: estimatedAmount is sent for display continuity only — the server
// recomputes rental + add-on totals itself from current rates and never
// trusts a client-submitted price.
export async function createBooking(payload) {
  return api.post('/bookings', payload);
}

// GET /api/bookings/:confirmationNo
export async function getBookingByConfirmation(confirmationNo) {
  return api.get(`/bookings/confirmation/${confirmationNo}`);
}

// GET /api/bookings/me?customerId=
export async function getLastBookingForCustomer(customerId) {
  return api.get('/bookings/me', { params: { customerId } });
}

// GET /api/bookings?customerId= — every booking for the customer, not just
// the last one. Backs the "modify booking" page's picker: a logged-in
// customer sees all their bookings and chooses which to change.
export async function getBookingsForCustomer(customerId) {
  return api.get('/bookings', { params: { customerId } });
}

// PUT /api/bookings/:confirmationNo
export async function modifyBooking(confirmationNo, patch) {
  return api.put(`/bookings/confirmation/${confirmationNo}`, patch);
}

// DELETE /api/bookings/:confirmationNo
export async function cancelBooking(confirmationNo) {
  return api.delete(`/bookings/confirmation/${confirmationNo}`);
}

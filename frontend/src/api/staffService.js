import { api } from './client';

// Staff: hand a vehicle over to the customer at pickup.
export async function handoverVehicle(confirmationNo, { fuelStatus, notes } = {}) {
  return api.post(`/staff/bookings/${confirmationNo}/handover`, { fuelStatus, notes });
}

// Staff: process a return — closes the booking, frees the car, generates an invoice.
export async function processReturn(
  confirmationNo,
  { extraMiles = 0, extraChargeAmount = 0, damageNotes = '', fuelStatus } = {}
) {
  return api.post(`/staff/bookings/${confirmationNo}/return`, {
    extraMiles,
    extraChargeAmount,
    damageNotes,
    fuelStatus,
  });
}

// Staff: cancellation lookup shares the same record shape as the customer flow.
export { getBookingByConfirmation, cancelBooking } from './bookingService';

// GET /api/staff/bookings — every booking, for the staff booking page:
// customer info, car (type + registration number), confirmation no, etc.
export async function getAllBookings() {
  return api.get('/staff/bookings');
}

// GET /api/staff/dashboard?hubId= — per car type, how many are available /
// handed over / in maintenance right now. hubId is optional; omit it for
// a fleet-wide view.
export async function getDashboard(hubId) {
  return api.get('/staff/dashboard', { params: { hubId } });
}

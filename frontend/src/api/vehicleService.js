import { api } from './client';

// GET /api/car-types?hubId=&pickupDatetime=&returnDatetime=
// hubId/dates are optional — omit them for the full catalog (e.g. an
// admin view), or pass them to get back only the car types that actually
// have an available car at that hub for that window. A type with zero
// available cars simply isn't returned, rather than showing up on the
// vehicle-selection page as a dead end.
export async function getCarTypes({ hubId, pickupDatetime, returnDatetime } = {}) {
  return api.get('/car-types', { params: { hubId, pickupDatetime, returnDatetime } });
}

// GET /api/cars/available?hubId=&pickupDatetime=&returnDatetime=
//
// Now also sends the two dates, which the old mock never used — the real
// backend runs the same overlap-check query built for the FLEMAN Home Page
// work (a car is available only if no CONFIRMED/ONGOING booking overlaps
// the requested window), so a car mid-rental for those exact dates no
// longer shows up as bookable just because its status flag says AVAILABLE.
export async function getAvailableCars({ hubId, pickupDatetime, returnDatetime } = {}) {
  return api.get('/cars/available', {
    params: { hubId, pickupDatetime, returnDatetime },
  });
}

// GET /api/cars/:id
export async function getCarById(carId) {
  return api.get(`/cars/${carId}`);
}

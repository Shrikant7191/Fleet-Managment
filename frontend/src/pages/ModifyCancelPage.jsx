import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Overlay from '../components/Overlay';
import * as bookingService from '../api/bookingService';
import { getCityById, getStateById } from '../api/locationService';
import { useBooking } from '../context/BookingContext';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { formatCurrency, formatDateTime } from '../utils/format';

const statusStyles = {
  CONFIRMED: 'bg-success/12 text-success',
  ONGOING: 'bg-primary/12 text-primary',
  COMPLETED: 'bg-ink/10 text-ink/60',
  CANCELLED: 'bg-danger/12 text-danger',
  PENDING: 'bg-warn/15 text-warn',
};

export default function ModifyCancelPage() {
  const { customer } = useAuth();
  const { applyPrefill, beginEditBooking } = useBooking();
  const navigate = useNavigate();
  const toast = useToast();

  // Logged-in customers pick from their own bookings; a guest (or anyone
  // who'd rather not browse a list) can still look one up by confirmation
  // number - both paths land on the same detail view below.
  const [myBookings, setMyBookings] = useState([]);
  const [myBookingsLoading, setMyBookingsLoading] = useState(false);

  const [confirmationNo, setConfirmationNo] = useState(customer ? '' : 'WDR-2894');
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [cancelOpen, setCancelOpen] = useState(false);

  useEffect(() => {
    if (!customer) {
      setMyBookings([]);
      return;
    }
    setMyBookingsLoading(true);
    bookingService
      .getBookingsForCustomer(customer.customerId)
      .then(setMyBookings)
      .finally(() => setMyBookingsLoading(false));
  }, [customer]);

  async function fetchBooking(no) {
    const target = no ?? confirmationNo;
    setLoading(true);
    setError('');
    try {
      const result = await bookingService.getBookingByConfirmation(target);
      setBooking(result);
      setConfirmationNo(result.confirmationNo);
    } catch (err) {
      setBooking(null);
      setError(err.message || 'Booking not found');
    } finally {
      setLoading(false);
    }
  }

  function modifyFromFetched() {
    if (!booking) return;
    const pickupCity = getCityById(booking.pickupHub?.cityId);
    const pickupState = getStateById(booking.pickupHub?.stateId);
    const dropCity = getCityById(booking.dropHub?.cityId);
    const dropState = getStateById(booking.dropHub?.stateId);
    const sameLocation = booking.pickupHubId === booking.dropHubId;

    applyPrefill({
      pickupMode: 'city',
      pickupState: pickupState?.stateId || '',
      pickupCity: pickupCity?.cityName || '',
      pickupDate: booking.pickupDatetime?.slice(0, 10) || '',
      pickupTime: booking.pickupDatetime?.slice(11, 16) || '10:00',
      returnDate: booking.returnDatetime?.slice(0, 10) || '',
      returnTime: booking.returnDatetime?.slice(11, 16) || '10:00',
      differentDropoff: !sameLocation,
      dropoffMode: 'city',
      dropoffState: dropState?.stateId || '',
      dropoffCity: dropCity?.cityName || '',
    });
    toast(`Loaded ${booking.confirmationNo} into the search — pick a new car to modify`);
    navigate('/');
  }

  // "Change vehicle" / "Change add-ons": rather than starting a whole new
  // booking, this hands the vehicle/add-ons page the existing booking's
  // hub, dates, car, and current add-on quantities, and flags it as an
  // edit - see BookingContext.beginEditBooking. Those pages then save the
  // one thing that changed straight back onto this booking and return here.
  function changeVehicle() {
    if (!booking) return;
    beginEditBooking(booking);
    navigate('/vehicles');
  }

  function changeAddons() {
    if (!booking) return;
    const quantities = {};
    (booking.addonLines || []).forEach((line) => {
      quantities[line.addonId] = line.quantity;
    });
    beginEditBooking(booking, quantities);
    navigate('/booking/add-ons');
  }

  async function doCancel() {
    try {
      const result = await bookingService.cancelBooking(confirmationNo);
      setBooking(result);
      setMyBookings((list) => list.map((b) => (b.confirmationNo === result.confirmationNo ? result : b)));
      setCancelOpen(false);
      toast(`Booking ${result.confirmationNo} cancelled — vehicle released back into the fleet`);
    } catch (err) {
      toast(err.message || 'Could not cancel booking');
    }
  }

  return (
    <div className="anim-screen-fade mx-auto max-w-[900px] px-6 pt-14 pb-28 md:px-8">
      <div className="mb-9">
        <h1 className="mb-2 font-display text-[26px] font-semibold tracking-[-0.01em] md:text-[30px]">Modify or cancel a booking</h1>
        <p className="max-w-135 text-[15px] text-ink/55">
          {customer
            ? 'Pick one of your bookings below, or look one up by confirmation number.'
            : "Enter your confirmation number and we'll pull up the record."}
        </p>
      </div>

      {customer && (
        <div className="mb-7">
          {myBookingsLoading ? (
            <p className="text-sm text-ink/50">Loading your bookings…</p>
          ) : myBookings.length ? (
            <div className="flex flex-col gap-2.5">
              {myBookings.map((b) => (
                <button
                  type="button"
                  key={b.confirmationNo}
                  onClick={() => fetchBooking(b.confirmationNo)}
                  className={`flex items-center justify-between gap-4 rounded-2xl border bg-white/70 p-4 text-left transition-colors ${
                    booking?.confirmationNo === b.confirmationNo ? 'border-primary bg-primary/5' : 'border-line hover:border-ink/25'
                  }`}
                >
                  <div>
                    <p className="font-mono text-[13px] font-semibold text-ink">{b.confirmationNo}</p>
                    <p className="text-[12.5px] text-ink/50">
                      {b.car?.brand} {b.car?.model} · {formatDateTime(b.pickupDatetime)}
                    </p>
                  </div>
                  <span className={`rounded-full px-3 py-1.5 text-[11px] font-bold tracking-[0.06em] uppercase ${statusStyles[b.bookingStatus] || ''}`}>
                    {b.bookingStatus}
                  </span>
                </button>
              ))}
            </div>
          ) : (
            <p className="text-sm text-ink/50">You don't have any bookings yet.</p>
          )}
        </div>
      )}

      <div className="max-w-130 rounded-[22px] border border-line bg-white/70 p-6.5 shadow-[0_6px_20px_rgba(18,21,28,0.05)]">
        <label className="field block">
          <span className="mb-1.5 block text-[11.5px] font-medium text-ink/50">
            {customer ? 'Or look up by confirmation no' : 'Booking confirmation no'}
          </span>
          <div className="mt-1.5 flex gap-2.5">
            <input
              type="text"
              placeholder="WDR-2894"
              value={confirmationNo}
              onChange={(e) => setConfirmationNo(e.target.value)}
              className="flex-1 rounded-[11px] border border-ink/10 bg-white/70 px-[11px] py-2.5 text-[13.5px] outline-none focus:border-steel"
            />
            <button
              type="button"
              onClick={() => fetchBooking()}
              disabled={loading}
              className="flex items-center gap-1.5 rounded-full bg-primary px-6.5 py-3 text-[14.5px] font-semibold text-white shadow-[0_4px_14px_rgba(47,111,237,0.3)] transition-[filter] hover:brightness-95 disabled:opacity-60"
            >
              {loading ? 'Fetching…' : 'Fetch record'}
            </button>
          </div>
        </label>
      </div>

      {error && <p className="mt-4 max-w-130 rounded-lg bg-danger/10 px-3 py-2 text-[12.5px] text-danger">{error}</p>}

      {booking && (
        <div className="mt-5.5">
          <div className="rounded-[22px] border border-line bg-white/70 p-6.5 shadow-[0_6px_20px_rgba(18,21,28,0.05)]">
            <div className="mb-4 flex items-center justify-between">
              <span className="font-mono text-lg font-semibold">{booking.confirmationNo}</span>
              <span className={`rounded-full px-3 py-1.5 text-[11.5px] font-bold tracking-[0.06em] uppercase ${statusStyles[booking.bookingStatus] || ''}`}>
                {booking.bookingStatus}
              </span>
            </div>
            <table className="w-full border-collapse text-[13.5px]">
              <tbody>
                <tr>
                  <td className="w-40 py-2.5 text-ink/50">Vehicle</td>
                  <td className="py-2.5">
                    {booking.carType?.carTypeName} · {booking.car?.brand} {booking.car?.model}
                    {booking.car?.vehicleNumber ? ` (${booking.car.vehicleNumber})` : ''}
                  </td>
                </tr>
                <tr>
                  <td className="py-2.5 text-ink/50">Pickup</td>
                  <td className="py-2.5">
                    {booking.pickupHub?.hubName} — {formatDateTime(booking.pickupDatetime)}
                  </td>
                </tr>
                <tr>
                  <td className="py-2.5 text-ink/50">Return</td>
                  <td className="py-2.5">
                    {booking.dropHub?.hubName} — {formatDateTime(booking.returnDatetime)}
                  </td>
                </tr>
                <tr>
                  <td className="py-2.5 text-ink/50">Renter</td>
                  <td className="py-2.5">
                    {booking.customer?.fullName} — {booking.customer?.email}
                  </td>
                </tr>
                <tr>
                  <td className="py-2.5 text-ink/50">Add-ons</td>
                  <td className="py-2.5">
                    {booking.addonLines?.length
                      ? booking.addonLines.map((l) => `${l.addon?.addonName} ×${l.quantity}`).join(', ')
                      : 'None'}
                  </td>
                </tr>
                <tr>
                  <td className="py-2.5 text-ink/50">Estimated total</td>
                  <td className="py-2.5 font-semibold">{formatCurrency(booking.estimatedAmount)}</td>
                </tr>
              </tbody>
            </table>

            {booking.bookingStatus !== 'CANCELLED' && booking.bookingStatus !== 'COMPLETED' && (
              <div className="mt-6 flex flex-wrap gap-3">
                <button
                  type="button"
                  onClick={changeVehicle}
                  className="rounded-full border border-line bg-white px-5.5 py-2.5 text-sm font-medium text-ink transition-colors hover:border-ink/30 hover:bg-paper"
                >
                  Change vehicle
                </button>
                <button
                  type="button"
                  onClick={changeAddons}
                  className="rounded-full border border-line bg-white px-5.5 py-2.5 text-sm font-medium text-ink transition-colors hover:border-ink/30 hover:bg-paper"
                >
                  Change add-ons
                </button>
                <button
                  type="button"
                  onClick={modifyFromFetched}
                  className="rounded-full border border-line bg-white px-5.5 py-2.5 text-sm font-medium text-ink transition-colors hover:border-ink/30 hover:bg-paper"
                >
                  Change location or dates
                </button>
                <button
                  type="button"
                  onClick={() => setCancelOpen(true)}
                  className="flex items-center gap-1.5 rounded-full border border-danger/30 bg-white px-5.5 py-2.5 text-sm font-medium text-danger transition-colors hover:bg-danger/6"
                >
                  Cancel booking
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      <Overlay open={cancelOpen} onClose={() => setCancelOpen(false)}>
        <h3 className="mb-2.5 font-display text-[19px] font-semibold">Cancel this booking?</h3>
        <p className="mb-4.5 text-[13.5px] leading-[1.55] text-ink/55">
          This releases the vehicle back into the fleet and can't be undone.
        </p>
        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            onClick={doCancel}
            className="rounded-full bg-danger px-6.5 py-3 text-[14.5px] font-semibold text-white transition-[filter] hover:brightness-95"
          >
            Yes, cancel booking
          </button>
          <button
            type="button"
            onClick={() => setCancelOpen(false)}
            className="rounded-full border border-line bg-white px-5.5 py-2.5 text-sm font-medium text-ink transition-colors hover:border-ink/30 hover:bg-paper"
          >
            Keep booking
          </button>
        </div>
      </Overlay>
    </div>
  );
}

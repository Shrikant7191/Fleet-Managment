import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import StepBar from '../components/StepBar';
import VehicleArt from '../components/VehicleArt';
import * as vehicleService from '../api/vehicleService';
import * as bookingService from '../api/bookingService';
import { useBooking } from '../context/BookingContext';
import { useToast } from '../context/ToastContext';
import { formatCurrency } from '../utils/format';

export default function VehicleSelectionPage() {
  const { draft, selectCar, pickupDatetime, returnDatetime, editingBooking, clearEditingBooking } = useBooking();
  const navigate = useNavigate();
  const toast = useToast();
  const [cars, setCars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!draft.search.pickupHubId) {
      navigate('/');
      return;
    }
    setLoading(true);
    // Every car at this hub comes back now, not just the strictly-available
    // ones - a car that's booked or maintenance-flagged still shows up
    // (disabled, with its status badge) instead of quietly vanishing from
    // the list. car.bookableNow (date-range aware) drives the disabled
    // state; car.status is still shown for the specific badge text.
    vehicleService
      .getAvailableCars({ hubId: draft.search.pickupHubId, pickupDatetime, returnDatetime })
      .then(setCars)
      .finally(() => setLoading(false));
  }, [draft.search.pickupHubId, pickupDatetime, returnDatetime, navigate]);

  async function handleContinue() {
    if (!draft.car) {
      toast('Pick a car to continue');
      return;
    }
    if (editingBooking) {
      setSaving(true);
      try {
        await bookingService.modifyBooking(editingBooking.confirmationNo, { carId: draft.car.carId });
        toast(`Vehicle updated for ${editingBooking.confirmationNo}`);
        clearEditingBooking();
        navigate('/modify');
      } catch (err) {
        toast(err.message || 'Could not update the vehicle');
      } finally {
        setSaving(false);
      }
      return;
    }
    navigate('/booking/add-ons');
  }

  return (
    <div className="anim-screen-fade mx-auto max-w-[1120px] px-6 pt-14 pb-28 md:px-8">
      <button type="button" onClick={() => navigate(editingBooking ? '/modify' : '/')} className="mb-5.5 flex items-center gap-1.5 text-[13.5px] font-medium text-ink/55 hover:text-ink">
        &larr; {editingBooking ? `Back to ${editingBooking.confirmationNo}` : 'Back to search'}
      </button>
      <StepBar current={2} />
      <div className="mb-9">
        <h1 className="mb-2 font-display text-[26px] font-semibold tracking-[-0.01em] md:text-[30px]">
          {editingBooking ? `Change vehicle for ${editingBooking.confirmationNo}` : 'Choose your car'}
        </h1>
        <p className="max-w-135 text-[15px] text-ink/55">
          Base rates shown per day, week, and month for your selected dates. Availability updates live as you pick.
        </p>
      </div>

      {loading ? (
        <p className="text-sm text-ink/50">Loading available cars…</p>
      ) : (
        <div className="flex flex-col gap-3.5">
          {cars.map((car) => {
            const selected = draft.car?.carId === car.carId;
            const disabled = !car.bookableNow;
            return (
              <button
                type="button"
                key={car.carId}
                disabled={disabled}
                onClick={() => selectCar(car)}
                className={`grid grid-cols-1 items-center gap-5 rounded-[20px] border bg-white/70 p-4.5 text-left shadow-[0_6px_20px_rgba(18,21,28,0.05)] transition-all sm:grid-cols-[96px_1fr_auto_auto] ${
                  disabled
                    ? 'cursor-not-allowed opacity-55'
                    : selected
                      ? 'border-primary shadow-[0_0_0_3px_rgba(47,111,237,0.14),0_12px_28px_rgba(18,21,28,0.09)]'
                      : 'cursor-pointer border-line hover:-translate-y-0.5 hover:shadow-[0_12px_28px_rgba(18,21,28,0.09)]'
                }`}
              >
                <VehicleArt carTypeName={car.carType?.carTypeName} className="w-full sm:w-24" />

                <div>
                  <p className="font-mono text-[10.5px] tracking-[0.14em] text-steel uppercase">{car.carType?.carTypeName}</p>
                  <p className="mt-0.75 mb-0.5 font-display text-[16.5px] font-semibold">
                    {car.brand} {car.model}
                  </p>
                  <p className="text-[12.5px] text-ink/50">
                    {[car.color, `${car.seatingCapacity} seats`, car.fuelType?.toLowerCase(), car.vehicleNumber]
                      .filter(Boolean)
                      .join(' · ')}
                  </p>
                </div>

                <div className="flex gap-4.5 justify-self-start sm:gap-5">
                  <div className="text-[11px] tracking-[0.06em] text-ink/40 uppercase">
                    Daily
                    <b className="mt-0.5 block font-display text-[14.5px] tracking-normal text-ink normal-case">
                      {formatCurrency(car.carType?.dailyRate)}
                    </b>
                  </div>
                  <div className="text-[11px] tracking-[0.06em] text-ink/40 uppercase">
                    Weekly
                    <b className="mt-0.5 block font-display text-[14.5px] tracking-normal text-ink normal-case">
                      {formatCurrency(car.carType?.weeklyRate)}
                    </b>
                  </div>
                  <div className="text-[11px] tracking-[0.06em] text-ink/40 uppercase">
                    Monthly
                    <b className="mt-0.5 block font-display text-[14.5px] tracking-normal text-ink normal-case">
                      {formatCurrency(car.carType?.monthlyRate)}
                    </b>
                  </div>
                </div>

                <div className="justify-self-start sm:justify-self-end">
                  {disabled && car.status === 'UNDER_MAINTENANCE' && (
                    <span className="rounded-full bg-ink/6 px-3 py-1.5 text-[11.5px] font-semibold tracking-[0.08em] text-ink/40 uppercase">
                      Unavailable
                    </span>
                  )}
                  {disabled && car.status !== 'UNDER_MAINTENANCE' && (
                    <span className="rounded-full bg-danger/9 px-3 py-1.5 text-[11.5px] font-semibold tracking-[0.08em] text-danger uppercase">
                      Fully booked
                    </span>
                  )}
                </div>
              </button>
            );
          })}
          {cars.length === 0 && <p className="text-sm text-ink/50">No cars found at this hub for your dates.</p>}
        </div>
      )}

      <div className="mt-7 flex flex-wrap gap-3">
        <button
          type="button"
          onClick={handleContinue}
          disabled={saving}
          className="flex items-center gap-1.5 rounded-full bg-primary px-6.5 py-3 text-[14.5px] font-semibold text-white shadow-[0_4px_14px_rgba(47,111,237,0.3)] transition-[filter] hover:brightness-95 disabled:opacity-60"
        >
          {editingBooking ? (saving ? 'Saving…' : 'Save vehicle change') : 'Continue booking'}
        </button>
        <button
          type="button"
          onClick={() => navigate(editingBooking ? '/modify' : '/')}
          className="rounded-full border border-line bg-white px-5.5 py-2.5 text-sm font-medium text-ink transition-colors hover:border-ink/30 hover:bg-paper"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}

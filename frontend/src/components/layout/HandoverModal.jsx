import { useEffect, useState } from 'react';
import Overlay from '../Overlay';
import * as staffService from '../../api/staffService';
import * as bookingService from '../../api/bookingService';
import * as vehicleService from '../../api/vehicleService';
import { useToast } from '../../context/ToastContext';

const fuelLevels = [
  { label: '¼', value: 25 },
  { label: '½', value: 50 },
  { label: '¾', value: 75 },
  { label: 'Full', value: 100 },
];

export default function HandoverModal({ open, onClose }) {
  const toast = useToast();
  const [confirmationNo, setConfirmationNo] = useState('WDR-2894');
  const [notes, setNotes] = useState('Clean, no visible damage, documents in glovebox');
  const [fuel, setFuel] = useState(50);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  // Looked up as the staff member types a confirmation number - shows the
  // assigned car's registration number, and every other car of that same
  // type at the pickup hub, so staff can see at a glance how many are
  // available vs. already out.
  const [booking, setBooking] = useState(null);
  const [fleetOfType, setFleetOfType] = useState([]);
  const [lookingUp, setLookingUp] = useState(false);

  useEffect(() => {
    if (!open) {
      setBooking(null);
      setFleetOfType([]);
    }
  }, [open]);

  async function handleLookup() {
    if (!confirmationNo.trim()) return;
    setLookingUp(true);
    setError('');
    try {
      const found = await bookingService.getBookingByConfirmation(confirmationNo);
      setBooking(found);
      const carsAtHub = await vehicleService.getAvailableCars({ hubId: found.pickupHubId });
      setFleetOfType(carsAtHub.filter((c) => c.carTypeId === found.car?.carTypeId));
    } catch (err) {
      setBooking(null);
      setFleetOfType([]);
      setError(err.message || 'Could not find that booking');
    } finally {
      setLookingUp(false);
    }
  }

  async function handleDone() {
    setBusy(true);
    setError('');
    try {
      await staffService.handoverVehicle(confirmationNo, { fuelStatus: fuel, notes });
      onClose();
      toast(`Vehicle allotted for ${confirmationNo} — pickup set to today, Happy journey!`);
    } catch (err) {
      setError(err.message || 'Could not process hand-over');
    } finally {
      setBusy(false);
    }
  }

  const availableCount = fleetOfType.filter((c) => c.bookableNow).length;

  return (
    <Overlay open={open} onClose={onClose}>
      <h3 className="mb-2.5 font-display text-[19px] font-semibold">Hand over vehicle</h3>

      {error && <p className="mb-3 rounded-lg bg-danger/10 px-3 py-2 text-[12.5px] text-danger">{error}</p>}

      <label className="field mb-3.5 block">
        <span className="mb-1.5 block text-[11.5px] font-medium text-ink/50">Booking confirmation no</span>
        <div className="flex gap-2">
          <input
            className="flex-1 rounded-[11px] border border-ink/10 bg-white/70 px-[11px] py-2.5 text-[13.5px] outline-none focus:border-steel"
            value={confirmationNo}
            onChange={(e) => setConfirmationNo(e.target.value)}
          />
          <button
            type="button"
            onClick={handleLookup}
            disabled={lookingUp}
            className="shrink-0 rounded-[11px] border border-ink/10 bg-white px-3.5 text-[12.5px] font-medium text-ink hover:border-ink/25 disabled:opacity-60"
          >
            {lookingUp ? 'Looking up…' : 'Look up'}
          </button>
        </div>
      </label>

      {booking && (
        <div className="mb-3.5 rounded-2xl border border-line bg-paper p-3.5">
          <p className="text-[13px] text-ink/60">
            Assigned vehicle — <b className="font-mono text-ink">{booking.car?.vehicleNumber || `#${booking.carId}`}</b>{' '}
            ({booking.carType?.carTypeName}, {booking.car?.brand} {booking.car?.model})
          </p>
          <p className="mt-1.5 text-[12.5px] text-ink/50">
            {availableCount} of {fleetOfType.length} {booking.carType?.carTypeName} car{fleetOfType.length === 1 ? '' : 's'} at this hub available right now
          </p>
          {fleetOfType.length > 0 && (
            <ul className="mt-2 flex flex-wrap gap-1.5">
              {fleetOfType.map((c) => (
                <li
                  key={c.carId}
                  className={`rounded-full px-2.5 py-1 font-mono text-[11.5px] ${
                    c.carId === booking.carId
                      ? 'bg-primary/15 text-primary'
                      : c.bookableNow
                        ? 'bg-success/12 text-success'
                        : 'bg-ink/6 text-ink/40'
                  }`}
                >
                  {c.vehicleNumber}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      <label className="field mb-3.5 block">
        <span className="mb-1.5 block text-[11.5px] font-medium text-ink/50">Car status</span>
        <textarea
          rows={2}
          className="w-full rounded-[11px] border border-ink/10 bg-white/70 px-[11px] py-2.5 text-[13.5px] outline-none focus:border-steel"
          placeholder="Clean, no visible damage, documents in glovebox"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
        />
      </label>

      <span className="mb-1.5 mt-3.5 block text-[11.5px] font-medium text-ink/50">Fuel status</span>
      <div className="mb-1 flex gap-2">
        {fuelLevels.map((f) => (
          <button
            type="button"
            key={f.value}
            onClick={() => setFuel(f.value)}
            className={`rounded-[10px] border-[1.5px] px-4 py-2 text-[13px] font-medium transition-colors ${
              fuel === f.value ? 'border-ink bg-ink text-white' : 'border-line text-ink'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      <p className="mt-3 text-[13.5px] leading-[1.55] text-ink/55">
        Available = physically at hub, not allotted, no inspection due in the rental window.
      </p>

      <div className="mt-2 flex flex-wrap gap-3">
        <button
          type="button"
          disabled={busy}
          onClick={handleDone}
          className="flex items-center gap-1.5 rounded-full bg-success px-6.5 py-3 text-[14.5px] font-semibold text-white shadow-[0_4px_14px_rgba(31,157,99,0.3)] transition-[filter] hover:brightness-95 disabled:opacity-60"
        >
          {busy ? 'Processing…' : 'Done'}
        </button>
        <button
          type="button"
          onClick={onClose}
          className="rounded-full border border-line bg-white px-5.5 py-2.5 text-sm font-medium text-ink transition-colors hover:border-ink/30 hover:bg-paper"
        >
          Cancel
        </button>
      </div>
    </Overlay>
  );
}

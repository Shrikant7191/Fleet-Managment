import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Overlay from '../components/Overlay';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import * as bookingService from '../api/bookingService';
import * as staffService from '../api/staffService';
import { daysBetween } from '../context/BookingContext';
import { formatCurrency } from '../utils/format';

const fuelLevels = [
  { label: '¼', value: 25 },
  { label: '½', value: 50 },
  { label: '¾', value: 75 },
  { label: 'Full', value: 100 },
];

export default function StaffReturnPage() {
  const { staff } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();

  const [confirmationNo, setConfirmationNo] = useState('WDR-2894');
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const [extraMiles, setExtraMiles] = useState(0);
  const [extraChargeAmount, setExtraChargeAmount] = useState(0);
  const [damageNotes, setDamageNotes] = useState('');
  const [fuel, setFuel] = useState(25);
  const [modalOpen, setModalOpen] = useState(false);
  const [invoice, setInvoice] = useState(null);

  useEffect(() => {
    if (!staff) {
      navigate('/');
      return;
    }
    fetchBooking('WDR-2894');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [staff]);

  async function fetchBooking(no) {
    setLoading(true);
    setError('');
    setInvoice(null);
    try {
      const result = await bookingService.getBookingByConfirmation(no ?? confirmationNo);
      setBooking(result);
    } catch (err) {
      setBooking(null);
      setError(err.message || 'Booking not found');
    } finally {
      setLoading(false);
    }
  }

  if (!staff) return null;

  const days = booking ? daysBetween(booking.pickupDatetime, booking.returnDatetime) : 0;
  const rentalAmount = booking ? (booking.carType?.dailyRate || 0) * days : 0;
  const addonAmount = booking ? booking.addonLines.reduce((s, l) => s + Number(l.subtotal), 0) : 0;
  const previewTotal = rentalAmount + addonAmount + Number(extraChargeAmount || 0);

  async function handleProcessReturn() {
    try {
      const result = await staffService.processReturn(confirmationNo, {
        extraMiles,
        extraChargeAmount,
        damageNotes,
        fuelStatus: fuel,
      });
      setInvoice(result);
      setModalOpen(false);
      toast('Return recorded — invoice emailed, vehicle available again');
      fetchBooking(confirmationNo);
    } catch (err) {
      toast(err.message || 'Could not process return');
    }
  }

  return (
    <div className="anim-screen-fade mx-auto max-w-[900px] px-6 pt-14 pb-28 md:px-8">
      <div className="mb-9">
        <h1 className="mb-2 font-display text-[26px] font-semibold tracking-[-0.01em] md:text-[30px]">Process return</h1>
        <p className="max-w-135 text-[15px] text-ink/55">
          Look up the booking's confirmation number to pull up its invoice preview.
        </p>
      </div>

      <div className="mb-6 flex max-w-130 gap-2.5">
        <input
          type="text"
          value={confirmationNo}
          onChange={(e) => setConfirmationNo(e.target.value)}
          placeholder="WDR-2894"
          className="flex-1 rounded-[11px] border border-ink/10 bg-white/70 px-[11px] py-2.5 text-[13.5px] outline-none focus:border-steel"
        />
        <button
          type="button"
          onClick={() => fetchBooking()}
          disabled={loading}
          className="rounded-full bg-primary px-5.5 py-2.5 text-sm font-semibold text-white shadow-[0_4px_14px_rgba(47,111,237,0.3)] transition-[filter] hover:brightness-95 disabled:opacity-60"
        >
          {loading ? 'Fetching…' : 'Fetch record'}
        </button>
      </div>

      {error && <p className="mb-4 max-w-130 rounded-lg bg-danger/10 px-3 py-2 text-[12.5px] text-danger">{error}</p>}

      {booking && (
        <div className="rounded-[22px] border border-line bg-white/70 p-6.5 shadow-[0_6px_20px_rgba(18,21,28,0.05)]">
          <p className="mb-4 text-[13.5px] text-ink/60">
            Invoice preview for booking <b className="font-mono text-ink">{booking.confirmationNo}</b> — vehicle{' '}
            <b className="font-mono text-ink">{booking.car?.vehicleNumber}</b> ({booking.bookingStatus})
          </p>

          <table className="w-full border-collapse text-[13.5px]">
            <thead>
              <tr>
                <th className="border-b-2 border-ink px-1.5 py-2.5 text-left text-[11px] tracking-[0.08em] text-ink/45 uppercase">Item</th>
                <th className="border-b-2 border-ink px-1.5 py-2.5 text-left text-[11px] tracking-[0.08em] text-ink/45 uppercase">Detail</th>
                <th className="border-b-2 border-ink px-1.5 py-2.5 text-right text-[11px] tracking-[0.08em] text-ink/45 uppercase">Amount</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td className="border-b border-line px-1.5 py-2.5">Base fare</td>
                <td className="border-b border-line px-1.5 py-2.5">
                  {booking.carType?.carTypeName} · {booking.car?.brand} {booking.car?.model} · {days} day{days > 1 ? 's' : ''} × {formatCurrency(booking.carType?.dailyRate)}
                </td>
                <td className="border-b border-line px-1.5 py-2.5 text-right font-mono">{formatCurrency(rentalAmount)}</td>
              </tr>
              {booking.addonLines.map((l) => (
                <tr key={l.bookingDetailId}>
                  <td className="border-b border-line px-1.5 py-2.5">Add-on</td>
                  <td className="border-b border-line px-1.5 py-2.5">
                    {l.addon?.addonName}, {l.quantity} × {formatCurrency(l.addonRate)}
                  </td>
                  <td className="border-b border-line px-1.5 py-2.5 text-right font-mono">{formatCurrency(l.subtotal)}</td>
                </tr>
              ))}
              <tr>
                <td className="border-b border-line px-1.5 py-2.5">Extra charges</td>
                <td className="border-b border-line px-1.5 py-2.5">
                  <div className="flex flex-wrap items-center gap-2">
                    <input
                      type="number"
                      min="0"
                      value={extraMiles}
                      onChange={(e) => setExtraMiles(Number(e.target.value))}
                      className="w-20 rounded-lg border border-ink/10 px-2 py-1 text-[12.5px] outline-none focus:border-steel"
                    />
                    extra miles ·
                    <input
                      type="number"
                      min="0"
                      value={extraChargeAmount}
                      onChange={(e) => setExtraChargeAmount(Number(e.target.value))}
                      className="w-20 rounded-lg border border-ink/10 px-2 py-1 text-[12.5px] outline-none focus:border-steel"
                    />
                    $ fee
                  </div>
                </td>
                <td className="border-b border-line px-1.5 py-2.5 text-right font-mono">{formatCurrency(extraChargeAmount)}</td>
              </tr>
              <tr>
                <td colSpan={2} className="px-1.5 py-2.5">
                  <b>Total {invoice ? `(Invoice INV-${invoice.invoiceId})` : ''}</b>
                </td>
                <td className="px-1.5 py-2.5 text-right font-mono">
                  <b>{formatCurrency(invoice ? invoice.totalAmount : previewTotal)}</b>
                </td>
              </tr>
            </tbody>
          </table>

          <div className="mt-6 flex flex-wrap gap-3">
            <button
              type="button"
              disabled={booking.bookingStatus === 'COMPLETED'}
              onClick={() => setModalOpen(true)}
              className="flex items-center gap-1.5 rounded-full bg-success px-6.5 py-3 text-[14.5px] font-semibold text-white shadow-[0_4px_14px_rgba(31,157,99,0.3)] transition-[filter] hover:brightness-95 disabled:opacity-50"
            >
              {booking.bookingStatus === 'COMPLETED' ? 'Already returned' : 'Process return'}
            </button>
            <button
              type="button"
              onClick={() => toast('Invoice PDF emailed to the customer')}
              className="rounded-full border border-line bg-white px-5.5 py-2.5 text-sm font-medium text-ink transition-colors hover:border-ink/30 hover:bg-paper"
            >
              Email invoice
            </button>
            <button
              type="button"
              onClick={() => toast('Sent to printer')}
              className="rounded-full border border-line bg-white px-5.5 py-2.5 text-sm font-medium text-ink transition-colors hover:border-ink/30 hover:bg-paper"
            >
              Print invoice
            </button>
          </div>
        </div>
      )}

      <Overlay open={modalOpen} onClose={() => setModalOpen(false)}>
        <h3 className="mb-2.5 font-display text-[19px] font-semibold">Confirm return</h3>
        <label className="field mb-3.5 block">
          <span className="mb-1.5 block text-[11.5px] font-medium text-ink/50">Vehicle</span>
          <input
            type="text"
            readOnly
            value={`${booking?.car?.vehicleNumber || ''} — ${booking?.car?.brand || ''} ${booking?.car?.model || ''}`}
            className="w-full rounded-[11px] border border-ink/10 bg-[#f2f2ef] px-[11px] py-2.5 text-[13.5px]"
          />
        </label>
        <label className="field mb-3.5 block">
          <span className="mb-1.5 block text-[11.5px] font-medium text-ink/50">Damage notes</span>
          <textarea
            rows={2}
            value={damageNotes}
            onChange={(e) => setDamageNotes(e.target.value)}
            placeholder="Minor scratch, rear-left bumper"
            className="w-full rounded-[11px] border border-ink/10 bg-white/70 px-[11px] py-2.5 text-[13.5px] outline-none focus:border-steel"
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
        <div className="mt-4 flex flex-wrap gap-3">
          <button
            type="button"
            onClick={handleProcessReturn}
            className="rounded-full bg-success px-6.5 py-3 text-[14.5px] font-semibold text-white shadow-[0_4px_14px_rgba(31,157,99,0.3)] transition-[filter] hover:brightness-95"
          >
            Done
          </button>
          <button
            type="button"
            onClick={() => setModalOpen(false)}
            className="rounded-full border border-line bg-white px-5.5 py-2.5 text-sm font-medium text-ink transition-colors hover:border-ink/30 hover:bg-paper"
          >
            Cancel
          </button>
        </div>
      </Overlay>
    </div>
  );
}

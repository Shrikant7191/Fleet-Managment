import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import * as staffService from '../api/staffService';
import { formatCurrency, formatDateTime } from '../utils/format';

const statusStyles = {
  CONFIRMED: 'bg-success/12 text-success',
  ONGOING: 'bg-primary/12 text-primary',
  COMPLETED: 'bg-ink/10 text-ink/60',
  CANCELLED: 'bg-danger/12 text-danger',
  PENDING: 'bg-warn/15 text-warn',
};

export default function StaffBookingsPage() {
  const { staff } = useAuth();
  const navigate = useNavigate();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!staff) {
      navigate('/');
      return;
    }
    staffService.getAllBookings().then(setBookings).finally(() => setLoading(false));
  }, [staff, navigate]);

  if (!staff) return null;

  return (
    <div className="anim-screen-fade mx-auto max-w-[1200px] px-6 pt-14 pb-28 md:px-8">
      <div className="mb-9">
        <h1 className="mb-2 font-display text-[26px] font-semibold tracking-[-0.01em] md:text-[30px]">All bookings</h1>
        <p className="max-w-135 text-[15px] text-ink/55">Every reservation, with the renter and the vehicle assigned to it.</p>
      </div>

      {loading ? (
        <p className="text-sm text-ink/50">Loading bookings…</p>
      ) : (
        <div className="overflow-x-auto rounded-[20px] border border-line bg-white/70">
          <table className="w-full min-w-[820px] border-collapse text-[13.5px]">
            <thead>
              <tr className="border-b border-line text-left text-[11.5px] tracking-[0.05em] text-ink/45 uppercase">
                <th className="px-4 py-3 font-medium">Confirmation</th>
                <th className="px-4 py-3 font-medium">Customer</th>
                <th className="px-4 py-3 font-medium">Vehicle</th>
                <th className="px-4 py-3 font-medium">Pickup</th>
                <th className="px-4 py-3 font-medium">Return</th>
                <th className="px-4 py-3 font-medium">Total</th>
                <th className="px-4 py-3 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr key={b.confirmationNo} className="border-b border-line/70 last:border-b-0">
                  <td className="px-4 py-3 font-mono text-ink">{b.confirmationNo}</td>
                  <td className="px-4 py-3">
                    <p className="font-medium text-ink">{b.customer?.fullName}</p>
                    <p className="text-[12px] text-ink/45">{b.customer?.email}</p>
                  </td>
                  <td className="px-4 py-3">
                    <p className="text-ink">{b.carType?.carTypeName} · {b.car?.brand} {b.car?.model}</p>
                    <p className="font-mono text-[12px] text-ink/45">{b.car?.vehicleNumber}</p>
                  </td>
                  <td className="px-4 py-3 text-ink/70">{formatDateTime(b.pickupDatetime)}</td>
                  <td className="px-4 py-3 text-ink/70">{formatDateTime(b.returnDatetime)}</td>
                  <td className="px-4 py-3 font-medium text-ink">{formatCurrency(b.estimatedAmount)}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2.5 py-1 text-[11px] font-bold tracking-[0.05em] uppercase ${statusStyles[b.bookingStatus] || ''}`}>
                      {b.bookingStatus}
                    </span>
                  </td>
                </tr>
              ))}
              {bookings.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-4 py-6 text-center text-ink/45">No bookings yet.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

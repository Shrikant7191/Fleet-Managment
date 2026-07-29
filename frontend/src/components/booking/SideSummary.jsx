import { useBooking } from '../../context/BookingContext';
import { formatCurrency } from '../../utils/format';

export default function SideSummary() {
  const { draft, selectedAddonLines, totals } = useBooking();
  const { car } = draft;

  return (
    <aside className="sticky top-22 rounded-[22px] bg-ink p-5.5 text-white shadow-[0_16px_40px_rgba(18,21,28,0.18)]">
      <h4 className="mb-3.5 font-mono text-[11px] tracking-[0.18em] text-white/50 uppercase">Trip summary</h4>

      <div className="flex justify-between gap-3 border-b border-dashed border-white/14 py-2.5 text-[13px]">
        <span className="shrink-0 text-white/50">Car</span>
        <div className="text-right text-white/92">
          {car ? `${car.brand} ${car.model}` : '—'}
        </div>
      </div>
      <div className="flex justify-between gap-3 border-b border-dashed border-white/14 py-2.5 text-[13px]">
        <span className="shrink-0 text-white/50">Pickup</span>
        <div className="text-right text-white/92">
          {draft.search.pickupDate} · {draft.search.pickupTime}
        </div>
      </div>
      <div className="flex justify-between gap-3 border-b border-dashed border-white/14 py-2.5 text-[13px]">
        <span className="shrink-0 text-white/50">Return</span>
        <div className="text-right text-white/92">
          {draft.search.returnDate} · {draft.search.returnTime}
        </div>
      </div>
      <div className="flex justify-between gap-3 border-b border-dashed border-white/14 py-2.5 text-[13px]">
        <span className="shrink-0 text-white/50">Duration</span>
        <div className="text-right text-white/92">
          {totals.days} day{totals.days > 1 ? 's' : ''}
        </div>
      </div>
      <div className="flex justify-between gap-3 border-b border-dashed border-white/14 py-2.5 text-[13px]">
        <span className="shrink-0 text-white/50">Rental</span>
        <div className="text-right text-white/92">{formatCurrency(totals.rentalAmount)}</div>
      </div>
      {selectedAddonLines.map((line) => (
        <div key={line.addon.addonId} className="flex justify-between gap-3 border-b border-dashed border-white/14 py-2.5 text-[13px]">
          <span className="shrink-0 text-white/50">
            {line.addon.addonName} ×{line.quantity}
          </span>
          <div className="text-right text-white/92">{formatCurrency(line.subtotal)}</div>
        </div>
      ))}

      <div className="flex items-center justify-between gap-3 pt-4 text-sm">
        <span className="text-white/50">Estimated total</span>
        <b className="font-display text-[21px] text-white">{formatCurrency(totals.total)}</b>
      </div>
    </aside>
  );
}

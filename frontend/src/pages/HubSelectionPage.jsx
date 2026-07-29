import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import StepBar from '../components/StepBar';
import { MapPinIcon, ClockIcon } from '../components/icons/Icons';
import * as locationService from '../api/locationService';
import { useBooking } from '../context/BookingContext';

function HubCard({ title, hub }) {
  if (!hub) {
    return (
      <div className="rounded-[20px] border border-line bg-white/70 p-5 text-[13.5px] text-ink/50">
        Hub details aren't available yet.
      </div>
    );
  }
  const city = locationService.getCityById(hub.cityId);
  const state = locationService.getStateById(hub.stateId);

  return (
    <div className="rounded-[20px] border border-primary bg-white/80 p-5 shadow-[0_0_0_3px_rgba(47,111,237,0.1)]">
      <p className="mb-2.5 font-mono text-[11px] tracking-[0.14em] text-steel uppercase">{title}</p>
      <h3 className="mb-1 font-display text-[18px] font-semibold">{hub.hubName}</h3>
      <p className="text-[13px] text-ink/55">
        {[city?.cityName, state?.stateName].filter(Boolean).join(', ')}
      </p>
      <div className="mt-3.5 flex items-start gap-2 text-[13px] text-ink/60">
        <MapPinIcon width="14" height="14" className="mt-0.5 shrink-0 opacity-60" />
        <span>{hub.address}, {hub.pincode}</span>
      </div>
      <div className="mt-2 flex items-center gap-2 text-[13px] text-ink/60">
        <ClockIcon width="14" height="14" className="shrink-0 opacity-60" />
        <span>{hub.contactNo}</span>
      </div>
    </div>
  );
}

// Sits between the home page and the vehicle-selection page, but only for
// a state/city pickup - an airport pickup already resolves to exactly one
// kiosk hub via the airport itself, so BookingSearchCard sends those
// straight to /vehicles and this page is never reached. One column when
// pickup and drop-off are the same hub, two side-by-side columns when
// they're different.
export default function HubSelectionPage() {
  const { draft } = useBooking();
  const navigate = useNavigate();
  const { search } = draft;

  useEffect(() => {
    if (!search.pickupHubId || search.pickupMode === 'airport') {
      navigate(search.pickupHubId ? '/vehicles' : '/', { replace: true });
    }
  }, [search.pickupHubId, search.pickupMode, navigate]);

  if (!search.pickupHubId || search.pickupMode === 'airport') return null;

  const pickupHub = locationService.getHubById(search.pickupHubId);
  const dropHub = search.differentDropoff ? locationService.getHubById(search.dropoffHubId) : null;

  return (
    <div className="anim-screen-fade mx-auto max-w-[1120px] px-6 pt-14 pb-28 md:px-8">
      <button type="button" onClick={() => navigate('/')} className="mb-5.5 flex items-center gap-1.5 text-[13.5px] font-medium text-ink/55 hover:text-ink">
        &larr; Back to search
      </button>
      <StepBar current={1} />
      <div className="mb-9">
        <h1 className="mb-2 font-display text-[26px] font-semibold tracking-[-0.01em] md:text-[30px]">Confirm your hub</h1>
        <p className="max-w-135 text-[15px] text-ink/55">
          {search.differentDropoff
            ? "Here's where you'll pick up and drop off."
            : "Here's where you'll pick up and return the car."}
        </p>
      </div>

      <div className={`grid grid-cols-1 gap-4 ${search.differentDropoff ? 'md:grid-cols-2' : 'md:max-w-115'}`}>
        <HubCard title="Pickup" hub={pickupHub} />
        {search.differentDropoff && <HubCard title="Drop-off" hub={dropHub} />}
      </div>

      <div className="mt-7 flex flex-wrap gap-3">
        <button
          type="button"
          onClick={() => navigate('/vehicles')}
          className="flex items-center gap-1.5 rounded-full bg-primary px-6.5 py-3 text-[14.5px] font-semibold text-white shadow-[0_4px_14px_rgba(47,111,237,0.3)] transition-[filter] hover:brightness-95"
        >
          Continue booking
        </button>
        <button
          type="button"
          onClick={() => navigate('/')}
          className="rounded-full border border-line bg-white px-5.5 py-2.5 text-sm font-medium text-ink transition-colors hover:border-ink/30 hover:bg-paper"
        >
          Change location
        </button>
      </div>
    </div>
  );
}

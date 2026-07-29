const palette = {
  Economy: '#93A5C9',
  Compact: '#3E4F86',
  Sedan: '#2B3A67',
  SUV: '#1F9D63',
  Luxury: '#12151C',
  Minivan: '#FF6B35',
};

export default function VehicleArt({ carTypeName, className = '' }) {
  const bg = palette[carTypeName] || '#2B3A67';
  return (
    <div
      className={`grid h-16 w-24 shrink-0 place-items-center rounded-[14px] ${className}`}
      style={{ background: `${bg}1a` }}
    >
      <svg width="46" height="24" viewBox="0 0 46 24" fill="none">
        <path
          d="M3 16c0-2 1-4 3-4.5L9 8c1.5-1.5 3.5-2.5 6-2.5h12c2 0 4 1 5 2.5l3 3.5c1.5.5 2.5 2 2.5 3.5v2a1 1 0 0 1-1 1h-2a3 3 0 1 0-6 0H16a3 3 0 1 0-6 0H4a1 1 0 0 1-1-1v-2Z"
          fill={bg}
          opacity="0.85"
        />
        <circle cx="13" cy="18" r="2.6" fill="#12151C" />
        <circle cx="33" cy="18" r="2.6" fill="#12151C" />
      </svg>
    </div>
  );
}

import './StatusBadge.css';

export default function StatusBadge({ active }) {
  return (
    <span className={`badge ${active ? 'badge--active' : 'badge--ended'}`}>
      {active ? 'Aktywna' : 'Zakończona'}
    </span>
  );
}

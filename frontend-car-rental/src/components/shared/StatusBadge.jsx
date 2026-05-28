import './StatusBadge.css';

export default function StatusBadge({ active, paymentStatus }) {
  if (paymentStatus === 'PENDING') {
    return <span className="badge badge--pending">Oczekuje na płatność</span>;
  }
  if (paymentStatus === 'EXPIRED') {
    return <span className="badge badge--expired">Wygasła</span>;
  }
  return (
    <span className={`badge ${active ? 'badge--active' : 'badge--ended'}`}>
      {active ? 'Aktywna' : 'Zakończona'}
    </span>
  );
}

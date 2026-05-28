import { useNavigate } from 'react-router-dom';
import StatusBadge from '../shared/StatusBadge';
import './ReservationRow.css';

function pendingMinutesLeft(expiresAt) {
  if (!expiresAt) return null;
  const diff = new Date(expiresAt) - Date.now();
  if (diff <= 0) return 0;
  return Math.ceil(diff / 60000);
}

export default function ReservationRow({ reservation, onDelete, isAdmin }) {
  const navigate = useNavigate();
  const days = Math.max(1, Math.round(
    (new Date(reservation.endDate) - new Date(reservation.startDate)) / 86400000
  ));

  const isPending = reservation.paymentStatus === 'PENDING';
  const minutesLeft = isPending ? pendingMinutesLeft(reservation.stripeSessionExpiresAt) : null;

  return (
    <tr className={`res-row${isPending ? ' res-row--pending' : ''}`} onClick={() => navigate(`/reservations/${reservation.id}`)}>
      <td className="res-row__id">#{reservation.id}</td>
      <td>
        <div className="res-row__dates">
          {reservation.startDate} → {reservation.endDate}
        </div>
        <div className="res-row__days">{days} {days === 1 ? 'dzień' : 'dni'}</div>
      </td>
      <td>
        <StatusBadge active={reservation.status} paymentStatus={reservation.paymentStatus} />
        {isPending && minutesLeft !== null && (
          <div className="res-row__timer">
            {minutesLeft > 0 ? `⏱ ${minutesLeft} min` : 'Wygasła'}
          </div>
        )}
      </td>
      <td className="res-row__price">{reservation.totalPrice?.toFixed(2)} zł</td>
      {isAdmin && <td className="res-row__user">{reservation.username}</td>}
      {isAdmin && (
        <td onClick={(e) => e.stopPropagation()}>
          <button
            className="btn btn--danger btn--sm"
            onClick={() => onDelete(reservation)}
          >
            Usuń
          </button>
        </td>
      )}
    </tr>
  );
}

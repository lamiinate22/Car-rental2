import ReservationRow from './ReservationRow';
import './ReservationTable.css';

export default function ReservationTable({ reservations, onDelete, isAdmin }) {
  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Termin</th>
            <th>Status</th>
            <th>Kwota</th>
            {isAdmin && <th>Użytkownik</th>}
            {isAdmin && <th></th>}
          </tr>
        </thead>
        <tbody>
          {reservations.map((r) => (
            <ReservationRow
              key={r.id}
              reservation={r}
              onDelete={onDelete}
              isAdmin={isAdmin}
            />
          ))}
        </tbody>
      </table>
    </div>
  );
}

import './ConfirmDialog.css';

export default function ConfirmDialog({ message, onConfirm, onCancel }) {
  return (
    <div className="dialog-overlay" onClick={onCancel}>
      <div className="dialog" onClick={(e) => e.stopPropagation()}>
        <p className="dialog__message">{message}</p>
        <div className="dialog__actions">
          <button className="btn btn--ghost" onClick={onCancel}>Anuluj</button>
          <button className="btn btn--danger" onClick={onConfirm}>Usuń</button>
        </div>
      </div>
    </div>
  );
}

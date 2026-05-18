import './OptionCheckbox.css';

export default function OptionCheckbox({ option, checked, onChange }) {
  return (
    <label className={`option-checkbox ${checked ? 'option-checkbox--checked' : ''}`}>
      <input type="checkbox" checked={checked} onChange={(e) => onChange(option.name, e.target.checked)} />
      <span className="option-checkbox__name">{option.name}</span>
      <span className="option-checkbox__price">+{option.price} zł/dzień</span>
    </label>
  );
}

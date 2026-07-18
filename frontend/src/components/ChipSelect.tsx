import type { ReactNode } from "react";

export interface ChipOption<T extends string> {
  value: T;
  label: ReactNode;
}

interface ChipSelectProps<T extends string> {
  options: ChipOption<T>[];
  value: T;
  onChange: (value: T) => void;
  nomeGrupo: string;
}

export function ChipSelect<T extends string>({ options, value, onChange, nomeGrupo }: ChipSelectProps<T>) {
  return (
    <div className="chip-select" role="radiogroup" aria-label={nomeGrupo}>
      {options.map((opcao) => (
        <button
          key={opcao.value}
          type="button"
          role="radio"
          aria-checked={opcao.value === value}
          className={opcao.value === value ? "chip chip-selecionado" : "chip"}
          onClick={() => onChange(opcao.value)}
        >
          {opcao.label}
        </button>
      ))}
    </div>
  );
}

interface ChipMultiSelectProps<T extends string> {
  options: ChipOption<T>[];
  valores: T[];
  onToggle: (value: T) => void;
  nomeGrupo: string;
}

export function ChipMultiSelect<T extends string>({ options, valores, onToggle, nomeGrupo }: ChipMultiSelectProps<T>) {
  return (
    <div className="chip-select" role="group" aria-label={nomeGrupo}>
      {options.map((opcao) => (
        <button
          key={opcao.value}
          type="button"
          aria-pressed={valores.includes(opcao.value)}
          className={valores.includes(opcao.value) ? "chip chip-selecionado" : "chip"}
          onClick={() => onToggle(opcao.value)}
        >
          {opcao.label}
        </button>
      ))}
    </div>
  );
}

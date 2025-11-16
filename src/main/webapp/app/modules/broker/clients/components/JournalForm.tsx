import React from 'react';
import { postJournal, JournalDirection } from 'app/modules/broker/services/traders.service';

interface Props {
  tradingAccountId: string;
  onSuccess: () => void;
}

const JournalForm: React.FC<Props> = ({ tradingAccountId, onSuccess }) => {
  const [direction, setDirection] = React.useState<JournalDirection>('credit');
  const [amount, setAmount] = React.useState<string>('0');
  const [reason, setReason] = React.useState<string>('');
  const [submitting, setSubmitting] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await postJournal(tradingAccountId, { direction, amount: Number(amount), reason });
      onSuccess();
      setAmount('0');
      setReason('');
    } catch (err: any) {
      setError(err?.response?.data?.detail || 'Failed to submit journal');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={submit} data-cy="journal-form">
      <div>
        <label>
          Direction
          <select value={direction} onChange={e => setDirection(e.target.value as JournalDirection)} data-cy="journal-direction">
            <option value="credit">Credit</option>
            <option value="debit">Debit</option>
          </select>
        </label>
      </div>
      <div>
        <label>
          Amount
          <input
            type="number"
            step="0.01"
            value={amount}
            onChange={e => setAmount(e.target.value)}
            required
            min="0.01"
            data-cy="journal-amount"
          />
        </label>
      </div>
      <div>
        <label>
          Reason
          <input type="text" value={reason} onChange={e => setReason(e.target.value)} required data-cy="journal-reason" />
        </label>
      </div>
      {error && <div className="error">{error}</div>}
      <button type="submit" disabled={submitting} data-cy="journal-submit">
        {submitting ? 'Submitting...' : 'Submit Journal'}
      </button>
    </form>
  );
};

export default JournalForm;

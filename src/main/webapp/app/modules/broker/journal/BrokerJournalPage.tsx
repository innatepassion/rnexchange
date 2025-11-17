import React, { useState, useEffect } from 'react';
import { getTraders, TraderSummary, postJournal, JournalDirection, JournalRequest } from 'app/modules/broker/services/traders.service';
import SimulatedBanner from 'app/shared/components/SimulatedBanner';
import './BrokerJournalPage.scss';

/**
 * M6 User Story 2 (T027): Broker funds journal UI for creating credits/debits and viewing recent entries.
 */
const BrokerJournalPage: React.FC = () => {
  const [traders, setTraders] = useState<TraderSummary[]>([]);
  const [selectedTraderId, setSelectedTraderId] = useState<string>('');
  const [direction, setDirection] = useState<JournalDirection>('credit');
  const [amount, setAmount] = useState<string>('');
  const [reason, setReason] = useState<string>('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    getTraders(0, 100)
      .then(data => {
        setTraders(data.content || []);
      })
      .catch(err => {
        setError('Failed to load traders: ' + (err?.message || 'Unknown error'));
      })
      .finally(() => setLoading(false));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTraderId) {
      setError('Please select a trader');
      return;
    }

    setSubmitting(true);
    setError(null);
    setSuccess(null);

    try {
      const payload: JournalRequest = {
        direction,
        amount: Number(amount),
        reason,
      };
      await postJournal(selectedTraderId, payload);
      setSuccess(`Journal entry created successfully: ${direction} ${amount}`);
      setAmount('');
      setReason('');
      // Refresh traders list to show updated balances
      getTraders(0, 100).then(data => setTraders(data.content || []));
    } catch (err: any) {
      setError(err?.response?.data?.detail || 'Failed to submit journal entry');
    } finally {
      setSubmitting(false);
    }
  };

  const selectedTrader = traders.find(t => t.traderId === selectedTraderId);
  const isNegative = selectedTrader && typeof selectedTrader.cash === 'number' && selectedTrader.cash < 0;
  const isAtRisk = isNegative;

  return (
    <div className="broker-journal-page" data-cy="broker-journal-page">
      <SimulatedBanner />
      <h2>Funds Journal</h2>
      <p>Create credit or debit journal entries for trader accounts.</p>

      {loading && <div>Loading traders...</div>}

      {!loading && (
        <div className="journal-form-container">
          <form onSubmit={handleSubmit} className="journal-form" data-cy="journal-form">
            <div className="form-group">
              <label htmlFor="trader-select">
                Trader <span className="required">*</span>
              </label>
              <select
                id="trader-select"
                value={selectedTraderId}
                onChange={e => setSelectedTraderId(e.target.value)}
                required
                data-cy="journal-trader-select"
              >
                <option value="">-- Select Trader --</option>
                {traders.map(trader => (
                  <option key={trader.traderId} value={trader.traderId}>
                    {trader.name} ({trader.login}) - Cash: {trader.cash}
                  </option>
                ))}
              </select>
              {selectedTrader && (
                <div className="trader-info">
                  <div className={`balance ${isNegative ? 'negative' : ''}`}>
                    Current Balance: {selectedTrader.cash}
                    {isNegative && (
                      <span className="badge badge-danger" data-cy="negative-balance-flag">
                        NEGATIVE
                      </span>
                    )}
                  </div>
                  {isAtRisk && (
                    <div className="alert alert-warning" data-cy="at-risk-warning">
                      ⚠️ Account is at risk: Balance is negative
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="direction-select">
                Direction <span className="required">*</span>
              </label>
              <select
                id="direction-select"
                value={direction}
                onChange={e => setDirection(e.target.value as JournalDirection)}
                required
                data-cy="journal-direction"
              >
                <option value="credit">Credit (Add Funds)</option>
                <option value="debit">Debit (Withdraw Funds)</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="amount-input">
                Amount <span className="required">*</span>
              </label>
              <input
                id="amount-input"
                type="number"
                step="0.01"
                min="0.01"
                value={amount}
                onChange={e => setAmount(e.target.value)}
                required
                placeholder="0.00"
                data-cy="journal-amount"
              />
            </div>

            <div className="form-group">
              <label htmlFor="reason-input">
                Reason <span className="required">*</span>
              </label>
              <input
                id="reason-input"
                type="text"
                value={reason}
                onChange={e => setReason(e.target.value)}
                required
                placeholder="Enter reason for journal entry"
                data-cy="journal-reason"
              />
            </div>

            {error && (
              <div className="alert alert-danger" data-cy="journal-error">
                {error}
              </div>
            )}

            {success && (
              <div className="alert alert-success" data-cy="journal-success">
                {success}
              </div>
            )}

            <button type="submit" disabled={submitting} className="btn btn-primary" data-cy="journal-submit">
              {submitting ? 'Submitting...' : 'Create Journal Entry'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
};

export default BrokerJournalPage;

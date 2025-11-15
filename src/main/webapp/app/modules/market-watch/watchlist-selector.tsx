import React, { useState } from 'react';
import { Button, Form, FormFeedback, Input, Label, Modal, ModalBody, ModalFooter, ModalHeader, Spinner } from 'reactstrap';

import type { WatchlistSummary } from './market-watch.reducer';

interface WatchlistSelectorProps {
  watchlists: WatchlistSummary[];
  selectedWatchlistId: number | null;
  isLoading: boolean;
  isSubmitting: boolean;
  onSelect: (watchlistId: number | null) => void;
  onAddSymbol: (symbol: string) => Promise<void>;
}

const WatchlistSelector = ({ watchlists, selectedWatchlistId, isLoading, isSubmitting, onSelect, onAddSymbol }: WatchlistSelectorProps) => {
  const [isModalOpen, setModalOpen] = useState(false);
  const [symbol, setSymbol] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  const handleSelectChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const parsedValue = Number.parseInt(event.target.value, 10);
    onSelect(Number.isNaN(parsedValue) ? null : parsedValue);
  };

  const toggleModal = () => {
    setFormError(null);
    setSymbol('');
    setModalOpen(!isModalOpen);
  };

  const handleAdd = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!symbol.trim()) {
      setFormError('Symbol is required');
      return;
    }
    setFormError(null);
    try {
      await onAddSymbol(symbol.trim());
      toggleModal();
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Unable to add symbol');
    }
  };

  return (
    <div className="market-watch__controls">
      <label htmlFor="market-watch-select" className="form-label">
        Select watchlist
      </label>
      <div className="market-watch__controls-row">
        <select
          id="market-watch-select"
          className="form-select"
          value={selectedWatchlistId ?? ''}
          onChange={handleSelectChange}
          disabled={isLoading || !watchlists.length}
        >
          {!watchlists.length && <option value="">No watchlists available</option>}
          {watchlists.map(watchlist => (
            <option key={watchlist.id} value={watchlist.id}>
              {watchlist.name} {watchlist.symbolCount ? `(${watchlist.symbolCount})` : ''}
            </option>
          ))}
        </select>
        <Button
          color="primary"
          className="market-watch__add-btn"
          onClick={toggleModal}
          disabled={isLoading || !selectedWatchlistId}
          data-testid="add-symbol-button"
        >
          Add symbol
        </Button>
      </div>

      <Modal isOpen={isModalOpen} toggle={toggleModal} centered fade={false}>
        <Form onSubmit={handleAdd}>
          <ModalHeader toggle={toggleModal}>Add symbol to watchlist</ModalHeader>
          <ModalBody>
            <Label htmlFor="symbol-input" className="form-label">
              Symbol
            </Label>
            <Input
              id="symbol-input"
              name="symbol"
              placeholder="e.g., INFY"
              value={symbol}
              onChange={event => setSymbol(event.target.value.toUpperCase())}
              autoFocus
              invalid={Boolean(formError)}
              data-testid="add-symbol-input"
            />
            {formError && <FormFeedback>{formError}</FormFeedback>}
          </ModalBody>
          <ModalFooter>
            <Button color="secondary" onClick={toggleModal} type="button">
              Cancel
            </Button>
            <Button color="primary" type="submit" disabled={isSubmitting}>
              {isSubmitting ? (
                <>
                  <Spinner size="sm" className="me-2" /> Adding…
                </>
              ) : (
                'Add'
              )}
            </Button>
          </ModalFooter>
        </Form>
      </Modal>
    </div>
  );
};

export default WatchlistSelector;

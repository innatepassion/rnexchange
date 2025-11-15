import '@testing-library/jest-dom';
import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';

import WatchlistSelector from './watchlist-selector';

const WATCHLISTS = [
  { id: 1, name: 'Primary', symbolCount: 2, symbols: ['INFY', 'RELIANCE'] },
  { id: 2, name: 'Growth', symbolCount: 1, symbols: ['TCS'] },
];

describe('WatchlistSelector', () => {
  it('renders options and notifies selection changes', () => {
    const onSelect = jest.fn();
    render(
      <WatchlistSelector
        watchlists={WATCHLISTS}
        selectedWatchlistId={1}
        isLoading={false}
        isSubmitting={false}
        onSelect={onSelect}
        onAddSymbol={jest.fn()}
      />,
    );

    const select = screen.getByLabelText(/Select watchlist/i);
    fireEvent.change(select, { target: { value: '2' } });
    expect(onSelect).toHaveBeenCalledWith(2);
  });

  it('validates symbol before invoking add callback', async () => {
    const onAddSymbol = jest.fn().mockResolvedValue({});

    render(
      <WatchlistSelector
        watchlists={WATCHLISTS}
        selectedWatchlistId={1}
        isLoading={false}
        isSubmitting={false}
        onSelect={jest.fn()}
        onAddSymbol={onAddSymbol}
      />,
    );

    fireEvent.click(screen.getByTestId('add-symbol-button'));

    const input = await screen.findByTestId('add-symbol-input');
    fireEvent.change(input, { target: { value: 'INFY' } });
    const form = input.closest('form');
    if (!form) {
      throw new Error('Form not found');
    }
    fireEvent.submit(form);

    await waitFor(() => expect(onAddSymbol).toHaveBeenCalledWith('INFY'));
  });
});

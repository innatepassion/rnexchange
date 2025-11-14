import '@testing-library/jest-dom';
import React from 'react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';

import MarketWatch from './market-watch';
import marketWatchReducer, { MarketWatchState, setConnectionStatus, updateQuote } from './market-watch.reducer';
import type { IQuote } from 'app/shared/model/quote.model';

jest.mock('./use-market-data-subscription', () => ({
  useMarketDataSubscription: jest.fn(() => 'connected'),
}));

const mockAxios = new MockAdapter(axios);

const createStore = (preloadedState?: Partial<MarketWatchState>) =>
  configureStore({
    reducer: {
      marketWatch: marketWatchReducer,
      authentication: () => ({
        isAuthenticated: true,
        account: { login: 'trader1', authorities: ['TRADER'] },
      }),
    },
    preloadedState: preloadedState
      ? {
          marketWatch: { ...marketWatchReducer(undefined, { type: '' }), ...preloadedState },
        }
      : undefined,
  });

const renderWithStore = (store = createStore()) =>
  render(
    <Provider store={store}>
      <MarketWatch />
    </Provider>,
  );

describe('MarketWatch component', () => {
  beforeEach(() => {
    mockAxios.reset();
    jest.clearAllMocks();
  });

  it('renders empty state while watchlists load', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, []);

    renderWithStore();

    await waitFor(() => expect(screen.getByText(/No watchlists configured/i)).toBeInTheDocument());
  });

  it('renders watchlist options and selects the first by default', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [
      { id: 1, name: 'Primary', symbols: ['RELIANCE', 'TCS'], symbolCount: 2 },
      { id: 2, name: 'Commodities', symbols: ['GOLD_FUT_DEC25'], symbolCount: 1 },
    ]);

    renderWithStore();

    await waitFor(() => expect(screen.getByLabelText(/Select watchlist/i)).toBeInTheDocument());
    const dropdown = screen.getByLabelText<HTMLSelectElement>(/Select watchlist/i);

    expect(dropdown.value).toBe('1');
    expect(screen.getByText(/Primary/)).toBeInTheDocument();
  });

  it('displays quotes streamed via redux updates', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['INFY'], symbolCount: 1 }]);

    const store = createStore();
    renderWithStore(store);

    await waitFor(() => expect(screen.getByText(/SIMULATED FEED/i)).toBeInTheDocument());

    const quote: IQuote = {
      symbol: 'INFY',
      lastPrice: 1745.65,
      open: 1720,
      change: 25.65,
      changePercent: 1.49,
      volume: 985000,
      timestamp: '2025-11-14T10:10:00.000Z',
      marketStatus: 'OPEN',
    };

    act(() => {
      store.dispatch(updateQuote(quote));
    });

    await waitFor(() => expect(screen.getByText('INFY')).toBeInTheDocument());
    expect(screen.getByText('1,745.65')).toBeInTheDocument();
    expect(screen.getByText('25.65')).toBeInTheDocument();
    expect(screen.getByText('1.49%')).toBeInTheDocument();
    expect(screen.getByText('9,85,000')).toBeInTheDocument();
  });

  it('updates connection status indicator', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['INFY'], symbolCount: 1 }]);

    const store = createStore();
    renderWithStore(store);

    await waitFor(() => expect(screen.getByTestId('connection-status-fab')).toBeInTheDocument());

    act(() => {
      store.dispatch(setConnectionStatus('reconnecting'));
    });
    await waitFor(() => expect(screen.getByText(/Reconnecting/i)).toBeInTheDocument());

    act(() => {
      store.dispatch(setConnectionStatus('disconnected'));
    });
    await waitFor(() => expect(screen.getByText(/Disconnected/i)).toBeInTheDocument());
  });

  it('shows closed badge when quote marketStatus is HOLIDAY', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['BSEBANK'], symbolCount: 1 }]);

    const store = createStore();
    renderWithStore(store);

    const holidayQuote: IQuote = {
      symbol: 'BSEBANK',
      lastPrice: 4123.4,
      open: 4100,
      change: 23.4,
      changePercent: 0.57,
      volume: 45000,
      timestamp: '2025-11-14T09:30:00.000Z',
      marketStatus: 'HOLIDAY',
    };

    act(() => {
      store.dispatch(updateQuote(holidayQuote));
    });

    await waitFor(() => expect(screen.getByText(/Closed\/Holiday/i)).toBeInTheDocument());
  });

  it('allows switching watchlists from dropdown', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [
      { id: 1, name: 'Primary', symbols: ['INFY'], symbolCount: 1 },
      { id: 2, name: 'Swing', symbols: ['RELIANCE', 'TCS'], symbolCount: 2 },
    ]);

    renderWithStore();

    const select = await screen.findByLabelText<HTMLSelectElement>(/Select watchlist/i);
    fireEvent.change(select, { target: { value: '2' } });

    await waitFor(() => expect(select.value).toBe('2'));
  });

  it('renders educational tooltips for badge and headers', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['INFY'], symbolCount: 1 }]);
    renderWithStore();

    const badge = await screen.findByTestId('simulated-feed-badge');
    expect(badge.getAttribute('title')).toMatch(/simulated/i);

    await waitFor(() => expect(screen.getByTitle('Last traded price streamed from the mock feed.')).toBeInTheDocument());
    expect(screen.getByTestId('connection-status-fab').getAttribute('title')).toMatch(/websocket/i);
  });

  it('shows paused banner when quotes pause', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['INFY'], symbolCount: 1 }]);
    const store = createStore();
    renderWithStore(store);

    const quote: IQuote = {
      symbol: 'INFY',
      lastPrice: 100,
      open: 95,
      change: 5,
      changePercent: 5.26,
      volume: 1000,
      timestamp: '2025-11-14T10:00:00.000Z',
      marketStatus: 'PAUSED',
    };

    act(() => {
      store.dispatch(updateQuote(quote));
    });

    await waitFor(() => expect(screen.getByText(/Feed paused by operator/i)).toBeInTheDocument());
  });

  it('shows holiday banner and frozen row styling', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['NSE100'], symbolCount: 1 }]);
    const store = createStore();
    renderWithStore(store);

    const holidayQuote: IQuote = {
      symbol: 'NSE100',
      lastPrice: 250,
      open: 250,
      change: 0,
      changePercent: 0,
      volume: 0,
      timestamp: '2025-11-14T09:45:00.000Z',
      marketStatus: 'HOLIDAY',
    };

    act(() => {
      store.dispatch(updateQuote(holidayQuote));
    });

    const row = await screen.findByText('NSE100');
    expect(row.closest('tr')?.className).toContain('market-watch__row--frozen');
    expect(screen.getByText(/Exchange holiday in effect/i)).toBeInTheDocument();
  });

  it('renders percent change supplied by server verbatim', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['BANK'], symbolCount: 1 }]);
    const store = createStore();
    renderWithStore(store);

    const quote: IQuote = {
      symbol: 'BANK',
      lastPrice: 300,
      open: 295,
      change: 5,
      changePercent: 42.42,
      volume: 10,
      timestamp: '2025-11-14T09:00:00.000Z',
    };

    act(() => {
      store.dispatch(updateQuote(quote));
    });

    await waitFor(() => expect(screen.getByText('42.42%')).toBeInTheDocument());
  });

  it('allows adding a symbol through the modal workflow', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['INFY'], symbolCount: 1 }]);
    mockAxios.onPost('/api/watchlists/1/items').reply(200, {
      id: 1,
      name: 'Primary',
      items: [
        { id: 1, symbol: 'INFY', sortOrder: 0 },
        { id: 2, symbol: 'RELIANCE', sortOrder: 1 },
      ],
    });

    renderWithStore();

    const addButton = await screen.findByTestId('add-symbol-button');
    await waitFor(() => expect(addButton).not.toBeDisabled());
    fireEvent.click(addButton);

    const input = await screen.findByTestId('add-symbol-input');
    fireEvent.change(input, { target: { value: 'RELIANCE' } });
    const form = input.closest('form');
    if (!form) {
      throw new Error('Form not found');
    }
    fireEvent.submit(form);

    await waitFor(() => expect(screen.getByText('RELIANCE')).toBeInTheDocument());
  });

  it('allows removing a symbol from the table', async () => {
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['INFY'], symbolCount: 1 }]);
    mockAxios.onDelete('/api/watchlists/1/items/INFY').reply(200, {
      id: 1,
      name: 'Primary',
      items: [],
    });

    const store = createStore();
    renderWithStore(store);

    const quote: IQuote = {
      symbol: 'INFY',
      lastPrice: 100,
      open: 100,
      change: 0,
      changePercent: 0,
      volume: 0,
      timestamp: '2025-11-14T10:00:00.000Z',
    };

    act(() => {
      store.dispatch(updateQuote(quote));
    });

    const removeButton = await screen.findByRole('button', { name: /remove/i });
    fireEvent.click(removeButton);

    await waitFor(() => expect(screen.queryByText('INFY')).not.toBeInTheDocument());
  });

  it('warns when first quote exceeds SLA window', async () => {
    jest.useFakeTimers();
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['INFY'], symbolCount: 1 }]);
    mockAxios.onPost('/api/watchlists/1/items').reply(200, {
      id: 1,
      name: 'Primary',
      items: [
        { id: 1, symbol: 'INFY', sortOrder: 0 },
        { id: 2, symbol: 'RELIANCE', sortOrder: 1 },
      ],
    });

    renderWithStore();

    const addButton = await screen.findByTestId('add-symbol-button');
    await waitFor(() => expect(addButton).not.toBeDisabled());
    fireEvent.click(addButton);
    act(() => {
      jest.runOnlyPendingTimers();
    });
    const input = await screen.findByTestId('add-symbol-input');
    fireEvent.change(input, { target: { value: 'RELIANCE' } });
    const form = input.closest('form');
    if (!form) {
      throw new Error('Form not found');
    }
    fireEvent.submit(form);

    await waitFor(() => expect(screen.getByText('RELIANCE')).toBeInTheDocument());

    act(() => {
      jest.advanceTimersByTime(2100);
    });

    await waitFor(() => expect(screen.getByText(/Quote SLA breach/i)).toBeInTheDocument());
    jest.useRealTimers();
  });

  it('does not warn when quote arrives within SLA window', async () => {
    jest.useFakeTimers();
    mockAxios.onGet('/api/watchlists').reply(200, [{ id: 1, name: 'Primary', symbols: ['INFY'], symbolCount: 1 }]);
    mockAxios.onPost('/api/watchlists/1/items').reply(200, {
      id: 1,
      name: 'Primary',
      items: [
        { id: 1, symbol: 'INFY', sortOrder: 0 },
        { id: 2, symbol: 'RELIANCE', sortOrder: 1 },
      ],
    });

    const store = createStore();
    renderWithStore(store);

    const addButton = await screen.findByTestId('add-symbol-button');
    await waitFor(() => expect(addButton).not.toBeDisabled());
    fireEvent.click(addButton);
    act(() => {
      jest.runOnlyPendingTimers();
    });
    const input = await screen.findByTestId('add-symbol-input');
    fireEvent.change(input, { target: { value: 'RELIANCE' } });
    const form = input.closest('form');
    if (!form) {
      throw new Error('Form not found');
    }
    fireEvent.submit(form);

    await waitFor(() => expect(screen.getByText('RELIANCE')).toBeInTheDocument());

    act(() => {
      jest.advanceTimersByTime(1000);
    });

    act(() => {
      store.dispatch(
        updateQuote({
          symbol: 'RELIANCE',
          lastPrice: 123.45,
          open: 120,
          change: 3.45,
          changePercent: 2.87,
          volume: 10,
          timestamp: '2025-11-14T10:10:00.000Z',
          marketStatus: 'OPEN',
        }),
      );
    });

    act(() => {
      jest.advanceTimersByTime(1500);
    });

    expect(screen.queryByText(/Quote SLA breach/i)).not.toBeInTheDocument();
    jest.useRealTimers();
  });
});

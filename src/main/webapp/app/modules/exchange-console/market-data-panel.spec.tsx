import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import MarketDataPanel from './market-data-panel';

const mockAxios = new MockAdapter(axios);

const mockFeedStatus = {
  globalState: 'RUNNING',
  startedAt: '2025-11-14T09:15:00.000Z',
  exchanges: [
    {
      exchangeCode: 'NSE',
      state: 'RUNNING',
      lastTickTime: '2025-11-14T10:32:15.123Z',
      ticksPerSecond: 42,
      activeInstruments: 523,
    },
    {
      exchangeCode: 'BSE',
      state: 'HOLIDAY',
      lastTickTime: '2025-11-13T15:30:00.000Z',
      ticksPerSecond: 0,
      activeInstruments: 0,
    },
    {
      exchangeCode: 'MCX',
      state: 'RUNNING',
      lastTickTime: '2025-11-14T10:32:14.987Z',
      ticksPerSecond: 18,
      activeInstruments: 87,
    },
  ],
};

describe('MarketDataPanel', () => {
  let store;

  beforeEach(() => {
    store = configureStore({
      reducer: {
        authentication: () => ({ account: { authorities: ['EXCHANGE_OPERATOR'] } }),
      },
    });
    mockAxios.reset();
  });

  afterEach(() => {
    jest.clearAllTimers();
  });

  it('renders the panel with feed status', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, mockFeedStatus);

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    await waitFor(() => {
      expect(screen.getByText(/Market Data Feed Control/i)).toBeInTheDocument();
    });
  });

  it('displays start and stop buttons', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, mockFeedStatus);

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /start feed/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /stop feed/i })).toBeInTheDocument();
    });
  });

  it('displays global feed state', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, mockFeedStatus);

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    await waitFor(() => {
      expect(screen.getByText(/RUNNING/i)).toBeInTheDocument();
    });
  });

  it('displays per-exchange metrics in a table', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, mockFeedStatus);

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    await waitFor(() => {
      expect(screen.getByText('NSE')).toBeInTheDocument();
      expect(screen.getByText('BSE')).toBeInTheDocument();
      expect(screen.getByText('MCX')).toBeInTheDocument();
      expect(screen.getByText('42')).toBeInTheDocument(); // NSE ticks/sec
      expect(screen.getByText('18')).toBeInTheDocument(); // MCX ticks/sec
    });
  });

  it('calls start endpoint when start button is clicked', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, { ...mockFeedStatus, globalState: 'STOPPED' });
    mockAxios.onPost('/api/marketdata/mock/start').reply(200, mockFeedStatus);

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    const startButton = await screen.findByRole('button', { name: /start feed/i });
    fireEvent.click(startButton);

    await waitFor(() => {
      expect(mockAxios.history.post.length).toBe(1);
      expect(mockAxios.history.post[0].url).toBe('/api/marketdata/mock/start');
    });
  });

  it('calls stop endpoint when stop button is clicked', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, mockFeedStatus);
    mockAxios.onPost('/api/marketdata/mock/stop').reply(200, { ...mockFeedStatus, globalState: 'STOPPED' });

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    const stopButton = await screen.findByRole('button', { name: /stop feed/i });
    fireEvent.click(stopButton);

    await waitFor(() => {
      expect(mockAxios.history.post.length).toBe(1);
      expect(mockAxios.history.post[0].url).toBe('/api/marketdata/mock/stop');
    });
  });

  it('polls status every 2 seconds', async () => {
    jest.useFakeTimers();
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, mockFeedStatus);

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    await waitFor(() => {
      expect(mockAxios.history.get.length).toBeGreaterThan(0);
    });

    const initialCallCount = mockAxios.history.get.length;

    jest.advanceTimersByTime(2000);

    await waitFor(() => {
      expect(mockAxios.history.get.length).toBeGreaterThan(initialCallCount);
    });

    jest.useRealTimers();
  });

  it('displays educational tooltips on hover', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, mockFeedStatus);

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    // Look for tooltip triggers (info icons or hoverable elements)
    await waitFor(() => {
      const tooltipTriggers = screen.queryAllByRole('button', { name: /info/i });
      expect(tooltipTriggers.length).toBeGreaterThan(0);
    });
  });

  it('shows HOLIDAY state for closed exchanges', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, mockFeedStatus);

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    await waitFor(() => {
      expect(screen.getByText(/HOLIDAY/i)).toBeInTheDocument();
    });
  });

  it('displays last tick time for each exchange', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, mockFeedStatus);

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    await waitFor(() => {
      // Should display formatted timestamps
      const timestamps = screen.getAllByText(/\d{1,2}:\d{2}:\d{2}/);
      expect(timestamps.length).toBeGreaterThan(0);
    });
  });

  it('disables start button when feed is running', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, mockFeedStatus);

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    const startButton = await screen.findByRole('button', { name: /start feed/i });
    expect(startButton).toBeDisabled();
  });

  it('disables stop button when feed is stopped', async () => {
    mockAxios.onGet('/api/marketdata/mock/status').reply(200, { ...mockFeedStatus, globalState: 'STOPPED' });

    render(
      <Provider store={store}>
        <MarketDataPanel />
      </Provider>,
    );

    const stopButton = await screen.findByRole('button', { name: /stop feed/i });
    expect(stopButton).toBeDisabled();
  });
});

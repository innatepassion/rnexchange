import reducer, {
  clearQuotes,
  initialState,
  MarketWatchState,
  selectWatchlist,
  setConnectionStatus,
  setWatchlists,
  updateQuote,
} from './market-watch.reducer';

import type { IQuote } from 'app/shared/model/quote.model';

describe('Market Watch reducer', () => {
  it('should return the initial state', () => {
    expect(reducer(undefined, { type: '' })).toEqual(initialState);
  });

  it('should update connection status', () => {
    const next = reducer(initialState, setConnectionStatus('connected'));
    expect(next.connectionStatus).toBe('connected');
  });

  it('should populate watchlists', () => {
    const watchlists = [
      { id: 1, name: 'Primary', symbolCount: 3 },
      { id: 2, name: 'Commodities', symbolCount: 2 },
    ];
    const next = reducer(initialState, setWatchlists(watchlists));
    expect(next.watchlists).toEqual(watchlists);
    expect(next.isLoadingWatchlists).toBe(false);
  });

  it('should select a watchlist and clear existing quotes', () => {
    const populatedState: MarketWatchState = {
      ...initialState,
      selectedWatchlistId: 1,
      quotes: {
        RELIANCE: {
          symbol: 'RELIANCE',
          lastPrice: 2485.3,
          open: 2475,
          change: 10.3,
          changePercent: 0.42,
          volume: 1523400,
          timestamp: '2025-11-14T10:32:15.123Z',
        },
      },
    };

    const next = reducer(populatedState, selectWatchlist(2));
    expect(next.selectedWatchlistId).toBe(2);
    expect(next.quotes).toEqual({});
  });

  it('should upsert quotes keyed by symbol', () => {
    const quote: IQuote = {
      symbol: 'INFY',
      lastPrice: 1745.65,
      open: 1720.1,
      change: 25.55,
      changePercent: 1.49,
      volume: 982342,
      timestamp: '2025-11-14T10:45:12.000Z',
    };

    const next = reducer(initialState, updateQuote(quote));
    expect(Object.keys(next.quotes)).toEqual(['INFY']);
    expect(next.quotes.INFY).toEqual(quote);
    expect(next.lastUpdateBySymbol.INFY).toBeDefined();
  });

  it('should clear quotes when feed paused or watchlist empty', () => {
    const populatedState: MarketWatchState = {
      ...initialState,
      quotes: {
        TCS: {
          symbol: 'TCS',
          lastPrice: 3321.9,
          open: 3300,
          change: 21.9,
          changePercent: 0.66,
          volume: 123455,
          timestamp: '2025-11-14T11:00:00.000Z',
        },
      },
      lastUpdateBySymbol: { TCS: Date.now() },
    };

    const next = reducer(populatedState, clearQuotes());
    expect(next.quotes).toEqual({});
    expect(next.lastUpdateBySymbol).toEqual({});
  });
});

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { IQuote } from 'app/shared/model/quote.model';

export type ConnectionStatus = 'connecting' | 'connected' | 'reconnecting' | 'disconnected';

export interface WatchlistSummary {
  id: number;
  name: string;
  symbolCount: number;
  symbols?: string[];
}

export interface MarketWatchState {
  connectionStatus: ConnectionStatus;
  watchlists: WatchlistSummary[];
  isLoadingWatchlists: boolean;
  selectedWatchlistId: number | null;
  quotes: Record<string, IQuote>;
  lastUpdateBySymbol: Record<string, number>;
}

export const initialState: MarketWatchState = {
  connectionStatus: 'connecting',
  watchlists: [],
  isLoadingWatchlists: true,
  selectedWatchlistId: null,
  quotes: {},
  lastUpdateBySymbol: {},
};

const marketWatchSlice = createSlice({
  name: 'marketWatch',
  initialState,
  reducers: {
    setConnectionStatus(state, action: PayloadAction<ConnectionStatus>) {
      state.connectionStatus = action.payload;
    },
    setWatchlists(state, action: PayloadAction<WatchlistSummary[]>) {
      state.watchlists = action.payload.map(watchlist => ({
        ...watchlist,
        symbolCount: watchlist.symbolCount ?? watchlist.symbols?.length ?? 0,
      }));
      state.isLoadingWatchlists = false;
      if (action.payload.length > 0 && !state.selectedWatchlistId) {
        state.selectedWatchlistId = action.payload[0].id;
      }
    },
    selectWatchlist(state, action: PayloadAction<number | null>) {
      state.selectedWatchlistId = action.payload;
      state.quotes = {};
      state.lastUpdateBySymbol = {};
    },
    setWatchlistSymbols(state, action: PayloadAction<{ id: number; symbols: string[] }>) {
      const target = state.watchlists.find(w => w.id === action.payload.id);
      if (target) {
        target.symbols = action.payload.symbols;
        target.symbolCount = action.payload.symbols.length;
      }
    },
    updateQuote(state, action: PayloadAction<IQuote>) {
      const quote = action.payload;
      state.quotes[quote.symbol] = quote;
      state.lastUpdateBySymbol[quote.symbol] = Date.now();
    },
    clearQuotes(state) {
      state.quotes = {};
      state.lastUpdateBySymbol = {};
    },
  },
});

export const { setConnectionStatus, setWatchlists, selectWatchlist, setWatchlistSymbols, updateQuote, clearQuotes } =
  marketWatchSlice.actions;

export default marketWatchSlice.reducer;

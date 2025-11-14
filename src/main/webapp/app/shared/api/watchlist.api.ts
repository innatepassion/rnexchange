import axios from 'axios';

export interface WatchlistSummaryResponse {
  id: number;
  name: string;
  symbolCount: number;
  symbols?: string[];
}

export interface WatchlistItemResponse {
  id: number;
  symbol: string;
  sortOrder: number | null;
}

export interface WatchlistResponse {
  id: number;
  name: string;
  items: WatchlistItemResponse[];
}

export const fetchWatchlists = () => axios.get<WatchlistSummaryResponse[]>('/api/watchlists');

export const fetchWatchlist = (watchlistId: number) => axios.get<WatchlistResponse>(`/api/watchlists/${watchlistId}`);

export const addWatchlistSymbol = (watchlistId: number, symbol: string) =>
  axios.post<WatchlistResponse>(`/api/watchlists/${watchlistId}/items`, { symbol });

export const removeWatchlistSymbol = (watchlistId: number, symbol: string) =>
  axios.delete<WatchlistResponse>(`/api/watchlists/${watchlistId}/items/${symbol}`);

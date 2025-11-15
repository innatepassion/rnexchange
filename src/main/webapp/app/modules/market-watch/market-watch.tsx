import './market-watch.scss';

import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Alert, Spinner } from 'reactstrap';
import { AxiosError } from 'axios';
import dayjs from 'dayjs';
import throttle from 'lodash/throttle';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import type { IQuote } from 'app/shared/model/quote.model';
import { clearQuotes, selectWatchlist, setConnectionStatus, setWatchlistSymbols, setWatchlists, updateQuote } from './market-watch.reducer';
import type { WatchlistSummary } from './market-watch.reducer';
import { useMarketDataSubscription } from './use-market-data-subscription';
import { addWatchlistSymbol, fetchWatchlist, fetchWatchlists, removeWatchlistSymbol } from 'app/shared/api/watchlist.api';
import WatchlistSelector from './watchlist-selector';

type ThrottledQuoteHandler = ((quote: IQuote) => void) & { cancel: () => void };

const numberFormatter = new Intl.NumberFormat('en-IN');
const twoDecimalFormatter = new Intl.NumberFormat('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const SLA_WARNING_MS = 2000;
const STALE_THRESHOLD_MS = 10000;
const STALE_CHECK_INTERVAL_MS = 5000;

const MarketWatch = () => {
  const dispatch = useAppDispatch();
  const { watchlists, selectedWatchlistId, quotes, isLoadingWatchlists, connectionStatus, lastUpdateBySymbol } = useAppSelector(
    state => state.marketWatch,
  );

  const [isFetchingWatchlistItems, setIsFetchingWatchlistItems] = useState(false);
  const [subscribedSymbols, setSubscribedSymbols] = useState<string[]>([]);
  const [mutationState, setMutationState] = useState<{ kind: 'add' | 'remove'; symbol?: string } | null>(null);
  const [notice, setNotice] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const throttledHandlers = useRef<Map<string, ThrottledQuoteHandler>>(new Map());
  const pendingQuoteTimersRef = useRef<Map<string, number>>(new Map());
  const pendingFirstQuoteRef = useRef<Map<string, number>>(new Map());
  const [staleSymbols, setStaleSymbols] = useState<Set<string>>(new Set());

  useEffect(() => {
    let cancelled = false;
    const loadWatchlists = async () => {
      try {
        const { data } = await fetchWatchlists();
        if (!cancelled) {
          dispatch(setWatchlists(data));
        }
      } catch (error) {
        if (!cancelled) {
          dispatch(setWatchlists([]));
        }
      }
    };
    loadWatchlists();
    return () => {
      cancelled = true;
      dispatch(clearQuotes());
    };
  }, []);

  const selectedWatchlist = useMemo(
    () => watchlists.find(watchlist => watchlist.id === selectedWatchlistId) ?? null,
    [watchlists, selectedWatchlistId],
  );

  const hydrateWatchlist = useCallback(
    async (watchlistId: number) => {
      setIsFetchingWatchlistItems(true);
      try {
        const { data } = await fetchWatchlist(watchlistId);
        const symbols = data.items?.map(item => item.symbol) ?? [];
        dispatch(setWatchlistSymbols({ id: data.id, symbols }));
        setSubscribedSymbols(symbols);
      } catch (error) {
        setSubscribedSymbols([]);
      } finally {
        setIsFetchingWatchlistItems(false);
      }
    },
    [dispatch],
  );

  useEffect(() => {
    if (!selectedWatchlistId) {
      setSubscribedSymbols([]);
      return;
    }
    const cachedSymbols = selectedWatchlist?.symbols;
    if (cachedSymbols && cachedSymbols.length > 0) {
      setSubscribedSymbols(cachedSymbols);
      return;
    }
    hydrateWatchlist(selectedWatchlistId);
  }, [hydrateWatchlist, selectedWatchlist, selectedWatchlistId]);

  const handleWatchlistChange = (watchlistId: number | null) => {
    setNotice(null);
    dispatch(selectWatchlist(watchlistId));
  };

  const extractErrorMessage = (error: unknown) => {
    if (error instanceof AxiosError) {
      return (error.response?.data?.message as string) || error.message || 'Request failed';
    }
    if (error instanceof Error) {
      return error.message;
    }
    return 'Unexpected error occurred';
  };

  const startQuoteSlaTimer = useCallback((symbol: string) => {
    const normalized = symbol.toUpperCase();
    const start = Date.now();
    pendingFirstQuoteRef.current.set(normalized, start);
    const timeoutId = window.setTimeout(() => {
      const recorded = pendingFirstQuoteRef.current.get(normalized);
      if (recorded && Date.now() - recorded >= SLA_WARNING_MS) {
        setNotice({
          type: 'error',
          message: `Quote SLA breach: ${normalized} has not received its first update within 2 seconds.`,
        });
      }
    }, SLA_WARNING_MS);
    const existing = pendingQuoteTimersRef.current.get(normalized);
    if (existing) {
      clearTimeout(existing);
    }
    pendingQuoteTimersRef.current.set(normalized, timeoutId);
  }, []);

  const clearQuoteSlaTimer = useCallback((symbol: string) => {
    const normalized = symbol.toUpperCase();
    const timeoutId = pendingQuoteTimersRef.current.get(normalized);
    if (timeoutId) {
      clearTimeout(timeoutId);
      pendingQuoteTimersRef.current.delete(normalized);
    }
    pendingFirstQuoteRef.current.delete(normalized);
  }, []);

  const throttledQuoteDispatch = useCallback(
    (quote: IQuote) => {
      const existing = throttledHandlers.current.get(quote.symbol);
      if (existing) {
        existing(quote);
        return;
      }
      const handler = throttle(
        (latest: IQuote) => {
          dispatch(updateQuote(latest));
          clearQuoteSlaTimer(latest.symbol);
        },
        200,
        { leading: true, trailing: true },
      ) as ThrottledQuoteHandler;
      handler(quote);
      throttledHandlers.current.set(quote.symbol, handler);
    },
    [dispatch],
  );

  useEffect(
    () => () => {
      throttledHandlers.current.forEach(handler => handler.cancel());
      throttledHandlers.current.clear();
      pendingQuoteTimersRef.current.forEach(timeoutId => clearTimeout(timeoutId));
      pendingQuoteTimersRef.current.clear();
      pendingFirstQuoteRef.current.clear();
    },
    [],
  );

  useEffect(() => {
    const evaluateStaleness = () => {
      const now = Date.now();
      setStaleSymbols(() => {
        const next = new Set<string>();
        subscribedSymbols.forEach(symbol => {
          const last = lastUpdateBySymbol[symbol];
          if (last && now - last >= STALE_THRESHOLD_MS) {
            next.add(symbol);
          }
        });
        return next;
      });
    };
    evaluateStaleness();
    const intervalId = window.setInterval(evaluateStaleness, STALE_CHECK_INTERVAL_MS);
    return () => window.clearInterval(intervalId);
  }, [lastUpdateBySymbol, subscribedSymbols]);

  const realtimeStatus = useMarketDataSubscription(subscribedSymbols, throttledQuoteDispatch);
  const previousRealtimeStatus = useRef<string | null>(null);

  useEffect(() => {
    Object.keys(quotes).forEach(symbol => {
      if (pendingFirstQuoteRef.current.has(symbol.toUpperCase())) {
        clearQuoteSlaTimer(symbol);
      }
    });
  }, [clearQuoteSlaTimer, quotes]);

  useEffect(() => {
    if (realtimeStatus && previousRealtimeStatus.current !== realtimeStatus) {
      previousRealtimeStatus.current = realtimeStatus;
      dispatch(setConnectionStatus(realtimeStatus));
    }
  }, [realtimeStatus]);

  const handleAddSymbol = useCallback(
    async (symbol: string) => {
      if (!selectedWatchlistId) {
        const message = 'Select a watchlist before adding symbols.';
        setNotice({ type: 'error', message });
        throw new Error(message);
      }
      setMutationState({ kind: 'add', symbol });
      try {
        const previousSymbols = new Set(selectedWatchlist?.symbols ?? subscribedSymbols);
        const { data } = await addWatchlistSymbol(selectedWatchlistId, symbol);
        const symbols = data.items?.map(item => item.symbol) ?? [];
        dispatch(setWatchlistSymbols({ id: data.id, symbols }));
        setSubscribedSymbols(symbols);
        symbols.filter(itemSymbol => !previousSymbols.has(itemSymbol)).forEach(startQuoteSlaTimer);
        setNotice({ type: 'success', message: `${symbol.toUpperCase()} added to ${data.name}.` });
      } catch (error) {
        const message = extractErrorMessage(error);
        setNotice({ type: 'error', message });
        throw new Error(message);
      } finally {
        setMutationState(null);
      }
    },
    [dispatch, selectedWatchlist, selectedWatchlistId, startQuoteSlaTimer, subscribedSymbols],
  );

  const handleRemoveSymbol = async (symbol: string) => {
    if (!selectedWatchlistId) {
      return;
    }
    setMutationState({ kind: 'remove', symbol });
    try {
      const { data } = await removeWatchlistSymbol(selectedWatchlistId, symbol);
      const symbols = data.items?.map(item => item.symbol) ?? [];
      dispatch(setWatchlistSymbols({ id: data.id, symbols }));
      setSubscribedSymbols(symbols);
      clearQuoteSlaTimer(symbol);
      setNotice({ type: 'success', message: `${symbol.toUpperCase()} removed from ${data.name}.` });
    } catch (error) {
      setNotice({ type: 'error', message: extractErrorMessage(error) });
    } finally {
      setMutationState(null);
    }
  };

  const renderEmptyState = () => {
    if (isLoadingWatchlists) {
      return (
        <div className="market-watch__empty">
          <Spinner size="sm" className="me-2" /> Loading watchlists…
        </div>
      );
    }

    if (!watchlists.length) {
      return <div className="market-watch__empty">No watchlists configured for this trader account.</div>;
    }

    if (!subscribedSymbols.length && !isFetchingWatchlistItems) {
      return <div className="market-watch__empty">This watchlist has no instruments yet.</div>;
    }

    return null;
  };

  const pausedSymbols = subscribedSymbols.filter(symbol => quotes[symbol]?.marketStatus === 'PAUSED');
  const holidaySymbols = subscribedSymbols.filter(symbol => quotes[symbol]?.marketStatus === 'HOLIDAY');

  const formatSymbolList = (symbols: string[]) => {
    if (symbols.length > 3) {
      return `${symbols.slice(0, 3).join(', ')} +${symbols.length - 3} more`;
    }
    return symbols.join(', ');
  };

  const formatTimestampLabel = (timestamp?: number) => (timestamp ? dayjs(timestamp).format('HH:mm:ss') : '—');

  const statusBanners = useMemo(() => {
    const banners: { kind: 'paused' | 'holiday'; title: string; description: string; timestamp?: number }[] = [];
    if (pausedSymbols.length) {
      const lastPaused = Math.max(...pausedSymbols.map(symbol => lastUpdateBySymbol[symbol] ?? 0));
      banners.push({
        kind: 'paused',
        title: 'Feed paused by operator',
        description: `Quotes will resume automatically once the operator restarts the feed. Affected symbols: ${formatSymbolList(pausedSymbols)}.`,
        timestamp: lastPaused,
      });
    }
    if (holidaySymbols.length) {
      const lastHoliday = Math.max(...holidaySymbols.map(symbol => lastUpdateBySymbol[symbol] ?? 0));
      banners.push({
        kind: 'holiday',
        title: 'Exchange holiday in effect',
        description: `Holiday hours detected for: ${formatSymbolList(holidaySymbols)}.`,
        timestamp: lastHoliday,
      });
    }
    return banners;
  }, [holidaySymbols, lastUpdateBySymbol, pausedSymbols]);

  const connectionBadgeClass = `status-indicator status-indicator--${connectionStatus}`;
  const connectionStatusMessages = {
    connected: 'WebSocket connection is healthy and delivering live quotes.',
    connecting: 'Attempting to establish the WebSocket connection.',
    reconnecting: 'Connection dropped. Reconnecting automatically…',
    disconnected: 'WebSocket connection unavailable. Quotes may be stale.',
  };
  const connectionStatusLabel = (() => {
    switch (connectionStatus) {
      case 'connected':
        return 'Connected';
      case 'connecting':
        return 'Connecting';
      case 'reconnecting':
        return 'Reconnecting';
      default:
        return 'Disconnected';
    }
  })();

  return (
    <div className="market-watch">
      <header className="market-watch__header">
        <div>
          <h2>Market Watch</h2>
          <span
            className="market-watch__badge"
            data-testid="simulated-feed-badge"
            title="Training-only simulated prices. Do not use for live trading."
            aria-label="Simulated feed badge describing that prices are for training only."
          >
            SIMULATED FEED
          </span>
        </div>
        <WatchlistSelector
          watchlists={watchlists}
          isLoading={isLoadingWatchlists}
          selectedWatchlistId={selectedWatchlistId}
          onSelect={handleWatchlistChange}
          onAddSymbol={handleAddSymbol}
          isSubmitting={Boolean(mutationState && mutationState.kind === 'add')}
        />
      </header>

      {notice && (
        <Alert color={notice.type === 'error' ? 'danger' : 'success'} toggle={() => setNotice(null)} transition={{ timeout: 0 }}>
          {notice.message}
        </Alert>
      )}

      {renderEmptyState()}

      {statusBanners.length > 0 && (
        <div className="market-watch__banners" role="status" aria-live="polite">
          {statusBanners.map(banner => (
            <div key={banner.kind} className={`market-watch__banner market-watch__banner--${banner.kind}`}>
              <div className="market-watch__banner-body">
                <strong>{banner.title}</strong>
                <span>{banner.description}</span>
              </div>
              <span className="market-watch__banner-timestamp">Last update {formatTimestampLabel(banner.timestamp)}</span>
            </div>
          ))}
        </div>
      )}

      {subscribedSymbols.length > 0 && (
        <table className="market-watch__table">
          <thead>
            <tr>
              <th>Symbol</th>
              <th>
                <abbr title="Last traded price streamed from the mock feed." className="market-watch__th-tooltip">
                  LTP
                </abbr>
              </th>
              <th>
                <abbr title="Absolute change vs the session open price." className="market-watch__th-tooltip">
                  Change
                </abbr>
              </th>
              <th>
                <abbr title="Percent change supplied by the backend feed." className="market-watch__th-tooltip">
                  Change %
                </abbr>
              </th>
              <th>
                <abbr title="Cumulative session volume for the instrument." className="market-watch__th-tooltip">
                  Volume
                </abbr>
              </th>
              <th>
                <abbr title="Timestamp of the last received quote." className="market-watch__th-tooltip">
                  Last Updated
                </abbr>
              </th>
              <th>Status</th>
              <th className="text-end actions-col">Actions</th>
            </tr>
          </thead>
          <tbody>
            {subscribedSymbols.map(symbol => {
              const quote = quotes[symbol];
              if (!quote) {
                return (
                  <tr key={symbol}>
                    <td>{symbol}</td>
                    <td colSpan={5} className="market-watch__pending">
                      Waiting for first quote…
                    </td>
                    <td />
                  </tr>
                );
              }
              const rowClass = quote.change > 0 ? 'positive' : quote.change < 0 ? 'negative' : 'neutral';
              const isFrozen = quote.marketStatus && quote.marketStatus !== 'OPEN';
              const isStale = staleSymbols.has(symbol);
              const lastUpdated = dayjs(quote.timestamp).format('HH:mm:ss');
              return (
                <tr
                  key={symbol}
                  className={`${rowClass}${isFrozen || isStale ? ' market-watch__row--frozen' : ''}`}
                  data-testid={`market-watch-row-${symbol}`}
                >
                  <td>{quote.symbol}</td>
                  <td>{twoDecimalFormatter.format(quote.lastPrice)}</td>
                  <td>{twoDecimalFormatter.format(quote.change)}</td>
                  <td>{twoDecimalFormatter.format(quote.changePercent)}%</td>
                  <td>{numberFormatter.format(quote.volume)}</td>
                  <td>{lastUpdated}</td>
                  <td>
                    {quote.marketStatus === 'HOLIDAY' && (
                      <span className="status-chip status-chip--holiday" title="Exchange holiday — quotes are frozen until markets reopen.">
                        Closed/Holiday
                      </span>
                    )}
                    {quote.marketStatus === 'PAUSED' && (
                      <span className="status-chip status-chip--paused" title="Operator paused the feed for this instrument.">
                        Paused
                      </span>
                    )}
                    {isStale && (
                      <span className="status-chip status-chip--stale" title="No quote received in the last 10 seconds.">
                        Stale
                      </span>
                    )}
                  </td>
                  <td className="text-end">
                    {selectedWatchlistId && (
                      <button
                        type="button"
                        className="btn btn-link btn-sm text-danger"
                        disabled={mutationState?.kind === 'remove' && mutationState.symbol === symbol}
                        onClick={() => handleRemoveSymbol(symbol)}
                      >
                        {mutationState?.kind === 'remove' && mutationState.symbol === symbol ? 'Removing…' : 'Remove'}
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}

      <div
        className="market-watch__status-floating"
        data-testid="connection-status-fab"
        title={connectionStatusMessages[connectionStatus]}
        aria-label={connectionStatusMessages[connectionStatus]}
      >
        <span className={connectionBadgeClass} aria-hidden="true" />
        <span className="status-indicator__text">{connectionStatusLabel}</span>
      </div>
    </div>
  );
};

export default MarketWatch;

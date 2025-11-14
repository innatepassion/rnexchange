import './market-watch.scss';

import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Spinner } from 'reactstrap';
import axios from 'axios';
import dayjs from 'dayjs';
import throttle from 'lodash/throttle';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import type { IQuote } from 'app/shared/model/quote.model';
import { clearQuotes, selectWatchlist, setConnectionStatus, setWatchlists, updateQuote } from './market-watch.reducer';
import type { WatchlistSummary } from './market-watch.reducer';
import { useMarketDataSubscription } from './use-market-data-subscription';

type ThrottledQuoteHandler = ((quote: IQuote) => void) & { cancel: () => void };

const numberFormatter = new Intl.NumberFormat('en-IN');
const twoDecimalFormatter = new Intl.NumberFormat('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const MarketWatch = () => {
  const dispatch = useAppDispatch();
  const { watchlists, selectedWatchlistId, quotes, isLoadingWatchlists, connectionStatus, lastUpdateBySymbol } = useAppSelector(
    state => state.marketWatch,
  );

  const [isFetchingWatchlistItems, setIsFetchingWatchlistItems] = useState(false);
  const throttledHandlers = useRef<Map<string, ThrottledQuoteHandler>>(new Map());

  useEffect(() => {
    let cancelled = false;
    const loadWatchlists = async () => {
      try {
        const { data } = await axios.get<WatchlistSummary[]>('/api/watchlists');
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

  const [subscribedSymbols, setSubscribedSymbols] = useState<string[]>([]);

  useEffect(() => {
    const fetchWatchlistItems = async () => {
      if (!selectedWatchlistId) {
        setSubscribedSymbols([]);
        return;
      }
      const cachedSymbols = selectedWatchlist?.symbols;
      if (cachedSymbols && cachedSymbols.length > 0) {
        setSubscribedSymbols(cachedSymbols);
        return;
      }
      setIsFetchingWatchlistItems(true);
      try {
        const { data } = await axios.get<{ id: number; name: string; items: { symbol: string }[] }>(
          `/api/watchlists/${selectedWatchlistId}`,
        );
        const symbols = data.items?.map(item => item.symbol) ?? [];
        setSubscribedSymbols(symbols);
      } catch (error) {
        setSubscribedSymbols([]);
      } finally {
        setIsFetchingWatchlistItems(false);
      }
    };
    fetchWatchlistItems();
  }, [selectedWatchlist, selectedWatchlistId]);

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
    },
    [],
  );

  const realtimeStatus = useMarketDataSubscription(subscribedSymbols, throttledQuoteDispatch);
  const previousRealtimeStatus = useRef<string | null>(null);

  useEffect(() => {
    if (realtimeStatus && previousRealtimeStatus.current !== realtimeStatus) {
      previousRealtimeStatus.current = realtimeStatus;
      dispatch(setConnectionStatus(realtimeStatus));
    }
  }, [realtimeStatus]);

  const handleWatchlistChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value ? parseInt(event.target.value, 10) : null;
    dispatch(selectWatchlist(value));
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
        <div className="market-watch__controls">
          <label htmlFor="market-watch-select" className="form-label">
            Select watchlist
          </label>
          <select id="market-watch-select" className="form-select" value={selectedWatchlistId ?? ''} onChange={handleWatchlistChange}>
            {watchlists.map(watchlist => (
              <option key={watchlist.id} value={watchlist.id}>
                {watchlist.name} {watchlist.symbolCount ? `(${watchlist.symbolCount})` : ''}
              </option>
            ))}
          </select>
        </div>
      </header>

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
              const lastUpdated = dayjs(quote.timestamp).format('HH:mm:ss');
              return (
                <tr key={symbol} className={`${rowClass}${isFrozen ? ' market-watch__row--frozen' : ''}`}>
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

import { useEffect, useMemo, useRef, useState } from 'react';

import type { IQuote } from 'app/shared/model/quote.model';
import { marketDataWebSocketService } from './websocket-service';

export type SubscriptionStatus = 'connecting' | 'connected' | 'reconnecting' | 'disconnected';

export const useMarketDataSubscription = (symbols: string[], onQuote: (quote: IQuote) => void): SubscriptionStatus => {
  const [status, setStatus] = useState<SubscriptionStatus>('connecting');
  const symbolsKey = useMemo(() => symbols.slice().sort().join(','), [symbols]);
  const isMounted = useRef(true);
  const quoteHandlerRef = useRef(onQuote);

  useEffect(
    () => () => {
      isMounted.current = false;
    },
    [],
  );

  useEffect(() => {
    quoteHandlerRef.current = onQuote;
  }, [onQuote]);

  useEffect(() => {
    marketDataWebSocketService.connect(nextStatus => {
      if (isMounted.current) {
        setStatus(nextStatus);
      }
    });
    return () => {
      marketDataWebSocketService.disconnect();
    };
  }, []);

  useEffect(() => {
    if (!symbolsKey) {
      marketDataWebSocketService.subscribe([], onQuote);
      setStatus('connected');
      return;
    }
    if (status === 'connected') {
      marketDataWebSocketService.subscribe(symbols, quote => quoteHandlerRef.current(quote));
    }
  }, [symbolsKey, status]);

  return status;
};

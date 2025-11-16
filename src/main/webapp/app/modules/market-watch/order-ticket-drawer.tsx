import React, { useState, useCallback, useEffect } from 'react';
import { Button, Form, FormGroup, Label, Input, Alert, Spinner, Offcanvas, OffcanvasHeader, OffcanvasBody } from 'reactstrap';
import { AxiosError } from 'axios';
import { placeOrder, OrderResponse, getInstrumentBySymbol } from 'app/shared/api/trading.api';

interface OrderTicketDrawerProps {
  isOpen: boolean;
  onToggle: () => void;
  symbol?: string;
  tradingAccountId?: number;
  onOrderPlaced?: (response: OrderResponse) => void;
}

export const OrderTicketDrawer: React.FC<OrderTicketDrawerProps> = ({ isOpen, onToggle, symbol, tradingAccountId, onOrderPlaced }) => {
  const [side, setSide] = useState<'BUY' | 'SELL'>('BUY');
  const [type, setType] = useState<'MARKET' | 'LIMIT'>('MARKET');
  const [quantity, setQuantity] = useState<string>('');
  const [limitPrice, setLimitPrice] = useState<string>('');
  const [instrumentId, setInstrumentId] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const getErrorText = (error: unknown, fallback: string) => {
    if (error instanceof AxiosError && error.response?.data) {
      const data = error.response.data as { message?: string; title?: string; detail?: string };
      const detail = data.detail || '';
      const backendMessage = data.message || '';
      const title = data.title || '';

      if (detail) {
        return detail;
      }

      if (backendMessage.startsWith('error.') && title) {
        return title;
      }

      return backendMessage || title || fallback;
    }

    if (error instanceof Error) {
      return error.message || fallback;
    }

    return fallback;
  };

  const resetForm = useCallback(() => {
    setSide('BUY');
    setType('MARKET');
    setQuantity('');
    setLimitPrice('');
    setInstrumentId('');
    setMessage(null);
  }, []);

  // Look up instrument ID when symbol changes
  useEffect(() => {
    if (!symbol) {
      setInstrumentId('');
      return;
    }

    const fetchInstrument = async () => {
      try {
        const response = await getInstrumentBySymbol(symbol);
        if (response.data && response.data.length > 0) {
          setInstrumentId(response.data[0].id.toString());
        } else {
          setInstrumentId('');
          setMessage({ type: 'error', text: `Instrument not found for symbol: ${symbol}` });
        }
      } catch (error) {
        setInstrumentId('');
        const errorText = getErrorText(error, 'Failed to look up instrument');
        setMessage({ type: 'error', text: errorText });
      }
    };

    fetchInstrument();
  }, [symbol]);

  const handleToggle = useCallback(() => {
    if (!isSubmitting) {
      resetForm();
      onToggle();
    }
  }, [isSubmitting, onToggle, resetForm]);

  const handleSubmit = useCallback(
    async (e: React.FormEvent) => {
      e.preventDefault();

      if (!tradingAccountId) {
        setMessage({ type: 'error', text: 'Trading account not selected' });
        return;
      }

      if (!instrumentId) {
        setMessage({ type: 'error', text: 'Please select an instrument' });
        return;
      }

      if (!quantity || parseInt(quantity, 10) <= 0) {
        setMessage({ type: 'error', text: 'Quantity must be greater than 0' });
        return;
      }

      if (type === 'LIMIT' && (!limitPrice || parseFloat(limitPrice) <= 0)) {
        setMessage({ type: 'error', text: 'Limit price must be greater than 0' });
        return;
      }

      setIsSubmitting(true);
      try {
        const response = await placeOrder({
          tradingAccountId,
          instrumentId: parseInt(instrumentId, 10),
          side,
          type,
          quantity: parseInt(quantity, 10),
          limitPrice: type === 'LIMIT' ? parseFloat(limitPrice) : undefined,
        });

        setMessage({ type: 'success', text: `Order placed successfully: ${response.data.message || 'Status ' + response.data.status}` });
        if (onOrderPlaced) {
          onOrderPlaced(response.data);
        }

        // Reset form after successful submission
        setTimeout(() => {
          resetForm();
          onToggle();
        }, 1500);
      } catch (error) {
        const errorText = getErrorText(error, 'Failed to place order');
        setMessage({ type: 'error', text: errorText });
      } finally {
        setIsSubmitting(false);
      }
    },
    [tradingAccountId, instrumentId, side, type, quantity, limitPrice, onOrderPlaced, onToggle, resetForm],
  );

  return (
    <Offcanvas isOpen={isOpen} toggle={handleToggle} direction="end">
      <OffcanvasHeader toggle={handleToggle}>Place Order</OffcanvasHeader>
      <OffcanvasBody>
        <Form onSubmit={handleSubmit}>
          {message && (
            <Alert color={message.type === 'error' ? 'danger' : 'success'} toggle={() => setMessage(null)} className="mb-3">
              {message.text}
            </Alert>
          )}

          <FormGroup>
            <Label for="symbol">Symbol</Label>
            <Input id="symbol" type="text" value={symbol || ''} disabled placeholder={symbol || 'Select from Market Watch'} />
          </FormGroup>

          <FormGroup>
            <Label for="side">Side</Label>
            <Input id="side" type="select" value={side} onChange={e => setSide(e.target.value as 'BUY' | 'SELL')} disabled={isSubmitting}>
              <option value="BUY">BUY</option>
              <option value="SELL">SELL</option>
            </Input>
          </FormGroup>

          <FormGroup>
            <Label for="type">Type</Label>
            <Input
              id="type"
              type="select"
              value={type}
              onChange={e => setType(e.target.value as 'MARKET' | 'LIMIT')}
              disabled={isSubmitting}
            >
              <option value="MARKET">Market</option>
              <option value="LIMIT">Limit</option>
            </Input>
          </FormGroup>

          <FormGroup>
            <Label for="quantity">Quantity</Label>
            <Input
              id="quantity"
              type="number"
              placeholder="Enter quantity"
              value={quantity}
              onChange={e => setQuantity(e.target.value)}
              disabled={isSubmitting}
              min="1"
              step="1"
            />
          </FormGroup>

          {type === 'LIMIT' && (
            <FormGroup>
              <Label for="limitPrice">Limit Price</Label>
              <Input
                id="limitPrice"
                type="number"
                placeholder="Enter limit price"
                value={limitPrice}
                onChange={e => setLimitPrice(e.target.value)}
                disabled={isSubmitting}
                min="0.01"
                step="0.01"
              />
            </FormGroup>
          )}

          <FormGroup>
            <Button color="primary" type="submit" disabled={isSubmitting} className="w-100">
              {isSubmitting ? (
                <>
                  <Spinner size="sm" className="me-2" />
                  Placing Order...
                </>
              ) : (
                `Place ${side} Order`
              )}
            </Button>
          </FormGroup>

          <div className="small text-muted mt-3">
            <p>
              <strong>Note:</strong> This is a simulated trading environment for educational purposes only. Prices and executions are based
              on mock data.
            </p>
          </div>
        </Form>
      </OffcanvasBody>
    </Offcanvas>
  );
};

export default OrderTicketDrawer;

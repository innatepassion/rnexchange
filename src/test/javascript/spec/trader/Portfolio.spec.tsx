import '@testing-library/jest-dom';
import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import PortfolioCash from '../../../../main/webapp/app/modules/trader/portfolio-cash';

const mockAxios = new MockAdapter(axios);

describe('PortfolioCash component [US1 - T014]', () => {
  const tradingAccountId = 1;

  beforeEach(() => {
    mockAxios.reset();
  });

  it('renders loading state initially', () => {
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/balance`).reply(200, {});

    render(<PortfolioCash tradingAccountId={tradingAccountId} />);
    expect(screen.getByText(/Loading portfolio and cash data/i)).toBeInTheDocument();
  });

  it('displays cash balance and portfolio value when loaded', async () => {
    const mockCashBalance = {
      balance: 100000.5,
      updatedAt: '2025-01-15T10:30:00Z',
    };

    const mockPositions = [
      {
        id: 1,
        qty: 10,
        avgCost: 2500.0,
        lastPx: 2520.0,
        unrealizedPnl: 200.0,
        realizedPnl: 0,
        mtm: 25200.0,
        instrument: { symbol: 'RELIANCE', exchange: 'NSE' },
      },
    ];

    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, mockPositions);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/balance`).reply(200, mockCashBalance);

    render(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      expect(screen.getByText(/Available Cash/i)).toBeInTheDocument();
      expect(screen.getByText(/Portfolio Value \(MTM\)/i)).toBeInTheDocument();
    });

    // Verify cash balance is displayed
    expect(screen.getByText(/₹1,00,000.50/i)).toBeInTheDocument();

    // Verify portfolio value is displayed
    expect(screen.getByText(/₹25,200.00/i)).toBeInTheDocument();
  });

  it('displays positions table with correct data', async () => {
    const mockPositions = [
      {
        id: 1,
        qty: 10,
        avgCost: 2500.0,
        lastPx: 2520.0,
        unrealizedPnl: 200.0,
        realizedPnl: 150.0,
        mtm: 25200.0,
        instrument: { symbol: 'RELIANCE', exchange: 'NSE' },
      },
      {
        id: 2,
        qty: 5,
        avgCost: 1800.0,
        lastPx: 1750.0,
        unrealizedPnl: -250.0,
        realizedPnl: 0,
        mtm: 8750.0,
        instrument: { symbol: 'TCS', exchange: 'NSE' },
      },
    ];

    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, mockPositions);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/balance`).reply(200, { balance: 100000 });

    render(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      expect(screen.getByText('RELIANCE')).toBeInTheDocument();
      expect(screen.getByText('TCS')).toBeInTheDocument();
    });

    // Verify position data is displayed correctly
    const relianceRow = screen.getByTestId('position-row-RELIANCE');
    expect(relianceRow).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument(); // Quantity
    expect(screen.getByText('2,500.00')).toBeInTheDocument(); // Avg Cost
    expect(screen.getByText('2,520.00')).toBeInTheDocument(); // Last Price

    // Verify P&L is displayed with correct formatting
    expect(screen.getByText(/\+₹200.00/i)).toBeInTheDocument(); // Unrealized P&L for RELIANCE
    expect(screen.getByText(/\+₹150.00/i)).toBeInTheDocument(); // Realized P&L for RELIANCE
  });

  it('displays ledger entries with clear labels and running balances', async () => {
    const mockLedgerEntries = [
      {
        id: 1,
        type: 'DEBIT',
        amount: 25025.0,
        fee: 25.0,
        description: 'BUY RELIANCE x10 @ 2500',
        createdAt: '2025-01-15T10:00:00Z',
        balanceAfter: 74975.0,
      },
      {
        id: 2,
        type: 'CREDIT',
        amount: 25200.0,
        fee: 25.0,
        description: 'SELL RELIANCE x10 @ 2520, P&L: 200',
        createdAt: '2025-01-15T11:00:00Z',
        balanceAfter: 100175.0,
      },
    ];

    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, mockLedgerEntries);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/cash-balance`).reply(200, { balance: 100175 });

    render(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      expect(screen.getByText(/Recent Transactions \(Ledger\)/i)).toBeInTheDocument();
    });

    // Verify ledger entries are displayed
    expect(screen.getByText('DEBIT')).toBeInTheDocument();
    expect(screen.getByText('CREDIT')).toBeInTheDocument();
    expect(screen.getByText(/BUY RELIANCE x10 @ 2500/i)).toBeInTheDocument();
    expect(screen.getByText(/SELL RELIANCE x10 @ 2520, P&L: 200/i)).toBeInTheDocument();

    // Verify amounts are displayed with correct formatting
    expect(screen.getByText(/₹25,025.00/i)).toBeInTheDocument();
    expect(screen.getByText(/₹25,200.00/i)).toBeInTheDocument();

    // Verify fees are displayed
    expect(screen.getAllByText(/₹25.00/i).length).toBeGreaterThan(0);
  });

  it('displays empty state when no positions exist', async () => {
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/balance`).reply(200, { balance: 100000 });

    render(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      expect(screen.getByText(/No open positions for this account/i)).toBeInTheDocument();
      expect(screen.getByText(/Place a BUY order to open your first position/i)).toBeInTheDocument();
    });
  });

  it('displays empty state when no ledger entries exist', async () => {
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/balance`).reply(200, { balance: 100000 });

    render(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      expect(screen.getByText(/No recent transactions for this account/i)).toBeInTheDocument();
      expect(screen.getByText(/Each BUY or SELL order will create a transaction here/i)).toBeInTheDocument();
    });
  });

  it('displays error message on API failure', async () => {
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(500, { message: 'Server error' });
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/cash-balance`).reply(200, {});

    render(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      expect(screen.getByText(/Failed to load portfolio and cash data/i)).toBeInTheDocument();
    });
  });

  it('formats currency values correctly for Indian locale', async () => {
    const mockPositions = [
      {
        id: 1,
        qty: 100,
        avgCost: 1234.56,
        lastPx: 1250.78,
        unrealizedPnl: 1622.0,
        realizedPnl: 0,
        mtm: 125078.0,
        instrument: { symbol: 'RELIANCE', exchange: 'NSE' },
      },
    ];

    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, mockPositions);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/cash-balance`).reply(200, { balance: 1000000.5 });

    render(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      // Verify Indian number formatting (lakhs format)
      expect(screen.getByText(/₹10,00,000.50/i)).toBeInTheDocument();
      expect(screen.getByText(/₹1,25,078.00/i)).toBeInTheDocument();
    });
  });

  it('displays P&L with correct trend indicators and colors', async () => {
    const mockPositions = [
      {
        id: 1,
        qty: 10,
        avgCost: 2500.0,
        lastPx: 2600.0,
        unrealizedPnl: 1000.0, // Positive
        realizedPnl: 0,
        mtm: 26000.0,
        instrument: { symbol: 'RELIANCE', exchange: 'NSE' },
      },
      {
        id: 2,
        qty: 5,
        avgCost: 1800.0,
        lastPx: 1700.0,
        unrealizedPnl: -500.0, // Negative
        realizedPnl: 0,
        mtm: 8500.0,
        instrument: { symbol: 'TCS', exchange: 'NSE' },
      },
    ];

    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, mockPositions);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/balance`).reply(200, { balance: 100000 });

    render(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      const relianceRow = screen.getByTestId('position-row-RELIANCE');
      const tcsRow = screen.getByTestId('position-row-TCS');

      // Verify positive P&L has success styling
      expect(relianceRow.querySelector('.text-success')).toBeInTheDocument();

      // Verify negative P&L has danger styling
      expect(tcsRow.querySelector('.text-danger')).toBeInTheDocument();
    });
  });

  it('displays SELL badge for sell transactions in ledger', async () => {
    const mockLedgerEntries = [
      {
        id: 1,
        type: 'CREDIT',
        amount: 25200.0,
        fee: 25.0,
        description: 'SELL RELIANCE x10 @ 2520, P&L: 200',
        createdAt: '2025-01-15T11:00:00Z',
        balanceAfter: 100175.0,
      },
    ];

    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, mockLedgerEntries);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/cash-balance`).reply(200, { balance: 100175 });

    render(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      expect(screen.getByText('SELL')).toBeInTheDocument();
    });
  });

  it('updates positions and balances when data changes', async () => {
    const initialPositions = [
      {
        id: 1,
        qty: 10,
        avgCost: 2500.0,
        lastPx: 2520.0,
        unrealizedPnl: 200.0,
        realizedPnl: 0,
        mtm: 25200.0,
        instrument: { symbol: 'RELIANCE', exchange: 'NSE' },
      },
    ];

    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, initialPositions);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/ledger-entries`).reply(200, []);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/balance`).reply(200, { balance: 100000 });

    const { rerender } = render(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      expect(screen.getByText('RELIANCE')).toBeInTheDocument();
    });

    // Simulate position update (e.g., after order fill)
    const updatedPositions = [
      {
        id: 1,
        qty: 20, // Increased quantity
        avgCost: 2510.0, // Updated average cost
        lastPx: 2530.0, // Updated price
        unrealizedPnl: 400.0, // Updated P&L
        realizedPnl: 0,
        mtm: 50600.0,
        instrument: { symbol: 'RELIANCE', exchange: 'NSE' },
      },
    ];

    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/positions`).reply(200, updatedPositions);
    mockAxios.onGet(`/api/trading-accounts/${tradingAccountId}/cash-balance`).reply(200, { balance: 75000 }); // Reduced balance

    rerender(<PortfolioCash tradingAccountId={tradingAccountId} />);

    await waitFor(() => {
      expect(screen.getByText('20')).toBeInTheDocument(); // Updated quantity
      expect(screen.getByText('2,510.00')).toBeInTheDocument(); // Updated avg cost
    });
  });
});

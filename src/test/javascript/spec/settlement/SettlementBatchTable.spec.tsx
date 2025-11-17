import '@testing-library/jest-dom';
import React from 'react';
import { render, screen } from '@testing-library/react';
import { SettlementBatchTable } from '../../../../main/webapp/app/modules/exchange/settlement/components/SettlementBatchTable';
import type { SettlementBatchDTO } from '../../../../main/webapp/app/modules/exchange/settlement/services/settlement.service';

describe('SettlementBatchTable', () => {
  const mockBatches: SettlementBatchDTO[] = [
    {
      id: 1,
      refDate: '2025-01-15',
      kind: 'EOD',
      status: 'PROCESSED',
      accountsProcessed: 10,
      positionsProcessed: 25,
      netPnl: 5000.5,
    },
    {
      id: 2,
      refDate: '2025-01-14',
      kind: 'EOD',
      status: 'FAILED',
      accountsProcessed: 0,
      positionsProcessed: 0,
    },
  ];

  it('renders loading state', () => {
    render(<SettlementBatchTable batches={[]} loading={true} />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders empty state message when no batches', () => {
    render(<SettlementBatchTable batches={[]} loading={false} />);
    expect(screen.getByText(/No settlement batches found/)).toBeInTheDocument();
  });

  it('renders batches table with correct data', () => {
    render(<SettlementBatchTable batches={mockBatches} loading={false} />);

    // Check headers
    expect(screen.getByText('Date')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
    expect(screen.getByText('Accounts Processed')).toBeInTheDocument();
    expect(screen.getByText('Positions Processed')).toBeInTheDocument();
    expect(screen.getByText('Net P&L')).toBeInTheDocument();

    // Check first batch data
    expect(screen.getByText('PROCESSED')).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument();
    expect(screen.getByText('25')).toBeInTheDocument();
    expect(screen.getByText(/₹5,000.50/)).toBeInTheDocument();

    // Check second batch (FAILED status)
    expect(screen.getByText('FAILED')).toBeInTheDocument();
  });

  it('formats currency correctly', () => {
    const batchWithPnl: SettlementBatchDTO[] = [
      {
        id: 1,
        refDate: '2025-01-15',
        kind: 'EOD',
        status: 'PROCESSED',
        netPnl: 1234567.89,
      },
    ];
    render(<SettlementBatchTable batches={batchWithPnl} loading={false} />);
    expect(screen.getByText(/₹1,234,567.89/)).toBeInTheDocument();
  });

  it('displays N/A for missing values', () => {
    const batchWithMissingData: SettlementBatchDTO[] = [
      {
        id: 1,
        refDate: '2025-01-15',
        kind: 'EOD',
        status: 'CREATED',
      },
    ];
    render(<SettlementBatchTable batches={batchWithMissingData} loading={false} />);
    const nATexts = screen.getAllByText('N/A');
    expect(nATexts.length).toBeGreaterThan(0);
  });
});

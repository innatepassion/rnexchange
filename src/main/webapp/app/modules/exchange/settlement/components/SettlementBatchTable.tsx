import React from 'react';
import { Table } from 'reactstrap';
import type { SettlementBatchDTO } from '../services/settlement.service';

export interface SettlementBatchTableProps {
  batches: SettlementBatchDTO[];
  loading?: boolean;
}

export const SettlementBatchTable: React.FC<SettlementBatchTableProps> = ({ batches, loading }) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatCurrency = (value?: number) => {
    if (value === undefined || value === null) return 'N/A';
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
    }).format(value);
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'PROCESSED':
        return 'badge bg-success';
      case 'FAILED':
        return 'badge bg-danger';
      case 'CREATED':
        return 'badge bg-warning';
      default:
        return 'badge bg-secondary';
    }
  };

  if (loading) {
    return (
      <div className="text-center p-4">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  if (batches.length === 0) {
    return (
      <div className="alert alert-info">
        <p className="mb-0">No settlement batches found for the selected date range.</p>
      </div>
    );
  }

  return (
    <Table responsive striped hover>
      <thead>
        <tr>
          <th>Date</th>
          <th>Status</th>
          <th className="text-end">Accounts Processed</th>
          <th className="text-end">Positions Processed</th>
          <th className="text-end">Net P&L</th>
        </tr>
      </thead>
      <tbody>
        {batches.map(batch => (
          <tr key={batch.id}>
            <td>{formatDate(batch.refDate)}</td>
            <td>
              <span className={getStatusBadgeClass(batch.status)}>{batch.status}</span>
            </td>
            <td className="text-end">{batch.accountsProcessed ?? 'N/A'}</td>
            <td className="text-end">{batch.positionsProcessed ?? 'N/A'}</td>
            <td className="text-end">{formatCurrency(batch.netPnl)}</td>
          </tr>
        ))}
      </tbody>
    </Table>
  );
};

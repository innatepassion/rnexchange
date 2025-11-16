import React from 'react';

export interface UtilizationRow {
  rank: number;
  traderName: string;
  accountId: number;
  utilizationPct: number;
  stalePrice: boolean;
}

const UtilizationTable: React.FC<{ rows: UtilizationRow[] }> = ({ rows }) => {
  return (
    <div className="table-responsive">
      <table className="table table-striped">
        <thead>
          <tr>
            <th>#</th>
            <th>Trader</th>
            <th>Account</th>
            <th>Utilization</th>
            <th>Stale</th>
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={5} className="text-center">
                No data
              </td>
            </tr>
          ) : (
            rows.map(r => (
              <tr key={r.accountId}>
                <td>{r.rank}</td>
                <td>{r.traderName}</td>
                <td>{r.accountId}</td>
                <td>{r.utilizationPct.toFixed(2)}%</td>
                <td>{r.stalePrice ? '⚠️' : ''}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

export default UtilizationTable;

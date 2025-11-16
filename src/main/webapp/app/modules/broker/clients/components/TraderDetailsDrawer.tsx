import React from 'react';
import { getTraderDetails, TraderDetails } from 'app/modules/broker/services/traders.service';
import JournalForm from './JournalForm';

interface Props {
  traderId?: string;
  open: boolean;
  onClose: () => void;
  onRefreshList?: () => void;
}

const TraderDetailsDrawer: React.FC<Props> = ({ traderId, open, onClose, onRefreshList }) => {
  const [loading, setLoading] = React.useState(false);
  const [data, setData] = React.useState<TraderDetails | null>(null);
  const [refreshTick, setRefreshTick] = React.useState(0);

  React.useEffect(() => {
    if (open && traderId) {
      setLoading(true);
      getTraderDetails(traderId)
        .then(setData)
        .finally(() => setLoading(false));
    }
  }, [open, traderId, refreshTick]);

  if (!open) return null;

  return (
    <div className="drawer">
      <div className="drawer-header">
        <h3>Trader Details</h3>
        <button onClick={onClose}>Close</button>
      </div>
      <div className="drawer-body">
        {loading && <div>Loading...</div>}
        {!loading && data && (
          <>
            <div className="summary">
              <div>Name: {data.summary?.name}</div>
              <div>Login: {data.summary?.login}</div>
              <div>Status: {data.summary?.status}</div>
              <div>Cash: {data.summary?.cash}</div>
              <div>Current P&L: {data.summary?.currentPnl}</div>
            </div>
            <div className="journal">
              <h4>Funds Journal</h4>
              <JournalForm
                tradingAccountId={String(traderId)}
                onSuccess={() => {
                  setRefreshTick(t => t + 1);
                  if (onRefreshList) {
                    onRefreshList();
                  }
                }}
              />
            </div>
            <div className="ledger">
              <h4>Recent Ledger</h4>
              <table>
                <thead>
                  <tr>
                    <th>When</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Reason</th>
                  </tr>
                </thead>
                <tbody>
                  {data.recentLedger?.slice(0, 10).map(le => (
                    <tr key={le.id}>
                      <td>{new Date(le.createdAt).toLocaleString()}</td>
                      <td>{le.type}</td>
                      <td>{le.amount}</td>
                      <td>{le.reason}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default TraderDetailsDrawer;

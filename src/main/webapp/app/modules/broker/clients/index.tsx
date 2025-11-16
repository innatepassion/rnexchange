import React from 'react';
import { getTraders, TraderSummary } from 'app/modules/broker/services/traders.service';
import TraderDetailsDrawer from './components/TraderDetailsDrawer';

const ClientsPage: React.FC = () => {
  const [loading, setLoading] = React.useState(false);
  const [page, setPage] = React.useState(0);
  const [size] = React.useState(20);
  const [total, setTotal] = React.useState(0);
  const [rows, setRows] = React.useState<TraderSummary[]>([]);
  const [selectedId, setSelectedId] = React.useState<string | undefined>(undefined);
  const [drawerOpen, setDrawerOpen] = React.useState(false);

  const load = React.useCallback(() => {
    setLoading(true);
    getTraders(page, size)
      .then(data => {
        setRows(data.content || []);
        setTotal(data.totalElements || 0);
      })
      .finally(() => setLoading(false));
  }, [page, size]);

  React.useEffect(() => {
    load();
  }, [load]);

  return (
    <div className="broker-clients">
      <h2>Clients</h2>
      {loading && <div>Loading...</div>}
      {!loading && (
        <>
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Login</th>
                <th>Status</th>
                <th>Cash</th>
                <th>Current P&L</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {rows.map(r => (
                <tr key={r.traderId}>
                  <td>{r.name}</td>
                  <td>{r.login}</td>
                  <td>{r.status}</td>
                  <td>{r.cash}</td>
                  <td>{r.currentPnl}</td>
                  <td>
                    <button
                      onClick={() => {
                        setSelectedId(r.traderId);
                        setDrawerOpen(true);
                      }}
                    >
                      Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="pagination">
            <button disabled={page === 0} onClick={() => setPage(p => Math.max(0, p - 1))}>
              Prev
            </button>
            <span>
              Page {page + 1} / {Math.max(1, Math.ceil(total / size))}
            </span>
            <button disabled={(page + 1) * size >= total} onClick={() => setPage(p => p + 1)}>
              Next
            </button>
          </div>
          <TraderDetailsDrawer traderId={selectedId} open={drawerOpen} onClose={() => setDrawerOpen(false)} onRefreshList={load} />
        </>
      )}
    </div>
  );
};

export default ClientsPage;

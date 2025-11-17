import React, { useState, useEffect, useCallback } from 'react';
import { Alert, Spinner, Table, Input, Button, Card, CardBody, CardHeader, Row, Col, Badge } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSearch, faRefresh, faFilter } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';
import SimulatedBanner from 'app/shared/components/SimulatedBanner';
import './BrokerJournalEntries.scss';

/**
 * M6 User Story 2: Broker Journal Entries List View
 * Allows brokers to view, search, and filter journal entries (credits/debits) for their traders.
 */
interface JournalEntry {
  id: number;
  createdAt: string;
  type: 'CREDIT' | 'DEBIT' | 'EOD_MTM_CREDIT' | 'EOD_MTM_DEBIT';
  amount: number;
  fee?: number;
  balanceAfter?: number;
  description?: string;
  remarks?: string;
  reference?: string;
  tradingAccount?: {
    id: number;
    trader?: {
      displayName?: string;
      user?: {
        login: string;
      };
    };
  };
}

interface JournalEntriesPage {
  content: JournalEntry[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

const BrokerJournalEntries: React.FC = () => {
  const [entries, setEntries] = useState<JournalEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState<string>('ALL');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 20;

  const fetchEntries = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params: any = {
        page,
        size: pageSize,
        sort: 'createdAt,desc',
      };

      // Filter by type if not ALL
      // Map frontend filter values to backend enum values
      if (typeFilter !== 'ALL') {
        const backendType = typeFilter === 'JOURNAL_CREDIT' ? 'CREDIT' : typeFilter === 'JOURNAL_DEBIT' ? 'DEBIT' : typeFilter;
        params['type.equals'] = backendType;
      }

      // Search by description, remarks, or reference
      if (searchTerm) {
        params['description.contains'] = searchTerm;
      }

      const response = await axios.get<JournalEntry[]>('/api/ledger-entries', { params });
      const responseEntries = response.data || [];
      setEntries(responseEntries);

      // Extract pagination info from headers (JHipster uses 'x-total-count')
      // Headers are accessed as lowercase in axios responses
      const totalElementsHeader = response.headers['x-total-count'];
      const responseTotalElements = totalElementsHeader ? parseInt(totalElementsHeader, 10) : 0;
      const calculatedTotalPages = responseTotalElements > 0 ? Math.ceil(responseTotalElements / pageSize) : 0;
      setTotalElements(responseTotalElements);
      setTotalPages(calculatedTotalPages);
    } catch (err: any) {
      setError(err?.response?.data?.detail || 'Failed to load journal entries');
      console.error('Error fetching journal entries:', err);
    } finally {
      setLoading(false);
    }
  }, [page, typeFilter, searchTerm, pageSize]);

  useEffect(() => {
    fetchEntries();
  }, [fetchEntries]);

  const handleSearch = () => {
    setPage(0);
    fetchEntries();
  };

  const handleClear = () => {
    setSearchTerm('');
    setTypeFilter('ALL');
    setPage(0);
    fetchEntries();
  };

  const getTypeBadge = (type: string) => {
    // Backend returns CREDIT, DEBIT, EOD_MTM_CREDIT, EOD_MTM_DEBIT
    // Map to display labels
    if (type === 'CREDIT') {
      return <Badge color="success">JOURNAL CREDIT</Badge>;
    } else if (type === 'DEBIT') {
      return <Badge color="danger">JOURNAL DEBIT</Badge>;
    } else if (type === 'EOD_MTM_CREDIT') {
      return <Badge color="info">EOD MTM CREDIT</Badge>;
    } else if (type === 'EOD_MTM_DEBIT') {
      return <Badge color="warning">EOD MTM DEBIT</Badge>;
    } else {
      return <Badge color="secondary">{type}</Badge>;
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
    }).format(amount);
  };

  return (
    <div className="broker-journal-entries-page" data-cy="broker-journal-entries-page">
      <SimulatedBanner />
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2>Journal Entries</h2>
        <Button color="primary" onClick={fetchEntries} disabled={loading}>
          <FontAwesomeIcon icon={faRefresh} className="me-2" />
          Refresh
        </Button>
      </div>

      <Card className="mb-4">
        <CardHeader>
          <h5>
            <FontAwesomeIcon icon={faFilter} className="me-2" />
            Search & Filter
          </h5>
        </CardHeader>
        <CardBody>
          <Row>
            <Col md="6">
              <div className="mb-3">
                <label htmlFor="search-input" className="form-label">
                  Search (Description, Remarks, Reference)
                </label>
                <div className="input-group">
                  <Input
                    id="search-input"
                    type="text"
                    value={searchTerm}
                    onChange={e => setSearchTerm(e.target.value)}
                    onKeyPress={e => e.key === 'Enter' && handleSearch()}
                    placeholder="Enter search term..."
                    data-cy="journal-entries-search"
                  />
                  <Button color="primary" onClick={handleSearch} disabled={loading}>
                    <FontAwesomeIcon icon={faSearch} />
                  </Button>
                </div>
              </div>
            </Col>
            <Col md="4">
              <div className="mb-3">
                <label htmlFor="type-filter" className="form-label">
                  Entry Type
                </label>
                <Input
                  id="type-filter"
                  type="select"
                  value={typeFilter}
                  onChange={e => {
                    setTypeFilter(e.target.value);
                    setPage(0);
                  }}
                  data-cy="journal-entries-type-filter"
                >
                  <option value="ALL">All Types</option>
                  <option value="JOURNAL_CREDIT">Journal Credit</option>
                  <option value="JOURNAL_DEBIT">Journal Debit</option>
                  <option value="TRADE_CREDIT">Trade Credit</option>
                  <option value="TRADE_DEBIT">Trade Debit</option>
                  <option value="FEE">Fee</option>
                </Input>
              </div>
            </Col>
            <Col md="2" className="d-flex align-items-end">
              <Button color="secondary" onClick={handleClear} outline>
                Clear
              </Button>
            </Col>
          </Row>
        </CardBody>
      </Card>

      {error && (
        <Alert color="danger" data-cy="journal-entries-error">
          <strong>Error:</strong> {error}
        </Alert>
      )}

      {loading && (
        <div className="text-center py-4">
          <Spinner color="primary" />
          <p className="mt-2">Loading journal entries...</p>
        </div>
      )}

      {!loading && !error && (
        <>
          <div className="mb-3">
            <p className="text-muted">
              Showing {entries.length} of {totalElements} entries
            </p>
          </div>

          {entries.length === 0 ? (
            <Alert color="info" data-cy="journal-entries-empty">
              No journal entries found. {searchTerm || typeFilter !== 'ALL' ? 'Try adjusting your search or filters.' : ''}
            </Alert>
          ) : (
            <div className="table-responsive">
              <Table hover striped>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Date & Time</th>
                    <th>Type</th>
                    <th>Trader</th>
                    <th>Amount</th>
                    <th>Fee</th>
                    <th>Balance After</th>
                    <th>Description</th>
                    <th>Remarks</th>
                  </tr>
                </thead>
                <tbody>
                  {entries.map(entry => (
                    <tr key={entry.id} data-cy={`journal-entry-row-${entry.id}`}>
                      <td>#{entry.id}</td>
                      <td>{new Date(entry.createdAt).toLocaleString()}</td>
                      <td>{getTypeBadge(entry.type)}</td>
                      <td>{entry.tradingAccount?.trader?.user?.login || entry.tradingAccount?.trader?.displayName || 'N/A'}</td>
                      <td>
                        <strong className={entry.type === 'DEBIT' || entry.type === 'EOD_MTM_DEBIT' ? 'text-danger' : 'text-success'}>
                          {entry.type === 'DEBIT' || entry.type === 'EOD_MTM_DEBIT' ? '-' : '+'} {formatCurrency(entry.amount)}
                        </strong>
                      </td>
                      <td>{entry.fee ? formatCurrency(entry.fee) : '-'}</td>
                      <td>{entry.balanceAfter ? formatCurrency(entry.balanceAfter) : '-'}</td>
                      <td>{entry.description || '-'}</td>
                      <td>{entry.remarks || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          )}

          {totalPages > 1 && (
            <div className="d-flex justify-content-between align-items-center mt-3">
              <div>
                <Button color="primary" size="sm" onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0 || loading}>
                  Previous
                </Button>
                <span className="mx-3">
                  Page {page + 1} of {totalPages}
                </span>
                <Button
                  color="primary"
                  size="sm"
                  onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1 || loading}
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default BrokerJournalEntries;

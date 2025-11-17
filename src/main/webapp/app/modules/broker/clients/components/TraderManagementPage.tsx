import React, { useState, useEffect, useCallback } from 'react';
import { Button, Table, Modal, ModalHeader, ModalBody, ModalFooter, Form, FormGroup, Label, Input, Col, Row } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getTraderDetails, TraderDetails, TraderSummary, getTraders } from 'app/modules/broker/services/traders.service';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { createEntity, updateEntity, deleteEntity, getEntity, getEntities } from 'app/entities/trader-profile/trader-profile.reducer';
import { getEntity as getTradingAccount } from 'app/entities/trading-account/trading-account.reducer';
import { ITraderProfile } from 'app/shared/model/trader-profile.model';
import { AccountStatus } from 'app/shared/model/enumerations/account-status.model';
import { KycStatus } from 'app/shared/model/enumerations/kyc-status.model';
import JournalForm from './JournalForm';

const TraderManagementPage: React.FC = () => {
  const dispatch = useAppDispatch();

  // State for trader list
  const [traders, setTraders] = useState<TraderSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [total, setTotal] = useState(0);

  // State for selected trader details
  const [selectedTraderId, setSelectedTraderId] = useState<string | null>(null);
  const [traderDetails, setTraderDetails] = useState<TraderDetails | null>(null);
  const [detailsLoading, setDetailsLoading] = useState(false);

  // State for modals
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);

  // State for form
  const [formData, setFormData] = useState<Partial<ITraderProfile>>({
    displayName: '',
    email: '',
    mobile: '',
    kycStatus: 'PENDING',
    status: 'ACTIVE',
  });

  const users = useAppSelector(state => state.userManagement.users);
  const traderProfileEntity = useAppSelector(state => state.traderProfile.entity);
  const tradingAccountEntity = useAppSelector(state => state.tradingAccount.entity);
  const updating = useAppSelector(state => state.traderProfile.updating);
  const updateSuccess = useAppSelector(state => state.traderProfile.updateSuccess);

  // Helper function to get trader profile ID from trading account ID
  const getTraderProfileIdFromTradingAccount = useCallback(
    async (tradingAccountId: string): Promise<number | null> => {
      try {
        const result = await dispatch(getTradingAccount(tradingAccountId));
        const tradingAccount = result.payload?.data || tradingAccountEntity;
        return tradingAccount?.trader?.id || null;
      } catch (err) {
        console.error('Failed to get trading account:', err);
        return null;
      }
    },
    [dispatch, tradingAccountEntity],
  );

  // Load traders list
  const loadTraders = useCallback(() => {
    setLoading(true);
    getTraders(page, size)
      .then(data => {
        setTraders(data.content || []);
        setTotal(data.totalElements || 0);
      })
      .catch(err => console.error('Failed to load traders:', err))
      .finally(() => setLoading(false));
  }, [page, size]);

  // Load users for form
  useEffect(() => {
    dispatch(getUsers({}));
  }, [dispatch]);

  // Load traders on mount and page change
  useEffect(() => {
    loadTraders();
  }, [loadTraders]);

  // Refresh after successful update
  useEffect(() => {
    if (updateSuccess) {
      loadTraders();
      setShowAddModal(false);
      setShowEditModal(false);
      setShowDeleteModal(false);
    }
  }, [updateSuccess, loadTraders]);

  // Load trader details
  const loadTraderDetails = useCallback((traderId: string) => {
    setDetailsLoading(true);
    getTraderDetails(traderId)
      .then(setTraderDetails)
      .catch(err => console.error('Failed to load trader details:', err))
      .finally(() => setDetailsLoading(false));
  }, []);

  // Handle add trader
  const handleAdd = () => {
    setFormData({
      displayName: '',
      email: '',
      mobile: '',
      kycStatus: 'PENDING',
      status: 'ACTIVE',
    });
    setShowAddModal(true);
  };

  // State for editing trader ID (trading account ID)
  const [editingTraderId, setEditingTraderId] = useState<string | null>(null);
  const [editingTraderProfileId, setEditingTraderProfileId] = useState<number | null>(null);

  // Load trading account and then trader profile when editing trader ID changes
  useEffect(() => {
    if (editingTraderId) {
      getTraderProfileIdFromTradingAccount(editingTraderId).then(profileId => {
        if (profileId) {
          setEditingTraderProfileId(profileId);
          dispatch(getEntity(profileId));
        }
      });
    }
  }, [editingTraderId, dispatch, getTraderProfileIdFromTradingAccount]);

  // Update form data when entity is loaded
  useEffect(() => {
    if (
      editingTraderProfileId &&
      traderProfileEntity &&
      traderProfileEntity.id &&
      showEditModal &&
      editingTraderProfileId === traderProfileEntity.id
    ) {
      setFormData({
        id: traderProfileEntity.id,
        displayName: traderProfileEntity.displayName || '',
        email: traderProfileEntity.email || '',
        mobile: traderProfileEntity.mobile || '',
        kycStatus: traderProfileEntity.kycStatus || 'PENDING',
        status: traderProfileEntity.status || 'ACTIVE',
        user: traderProfileEntity.user,
      });
    }
  }, [traderProfileEntity, editingTraderProfileId, showEditModal]);

  // Handle edit trader
  const handleEdit = (traderId: string) => {
    setEditingTraderId(traderId);
    setShowEditModal(true);
  };

  // State for deleting trader ID (trading account ID)
  const [deletingTraderId, setDeletingTraderId] = useState<string | null>(null);
  const [deletingTraderProfileId, setDeletingTraderProfileId] = useState<number | null>(null);

  // Load trading account and then trader profile when deleting trader ID changes
  useEffect(() => {
    if (deletingTraderId) {
      getTraderProfileIdFromTradingAccount(deletingTraderId).then(profileId => {
        if (profileId) {
          setDeletingTraderProfileId(profileId);
          dispatch(getEntity(profileId));
        }
      });
    }
  }, [deletingTraderId, dispatch, getTraderProfileIdFromTradingAccount]);

  // Handle delete trader
  const handleDelete = (traderId: string) => {
    setDeletingTraderId(traderId);
    setShowDeleteModal(true);
  };

  // Handle activate trader
  const handleActivate = async (traderId: string) => {
    try {
      const profileId = await getTraderProfileIdFromTradingAccount(traderId);
      if (profileId) {
        const result = await dispatch(getEntity(profileId));
        const entity = result.payload?.data || traderProfileEntity;
        if (entity && entity.id) {
          const updatedEntity: ITraderProfile = {
            ...entity,
            status: 'ACTIVE',
          };
          await dispatch(updateEntity(updatedEntity));
          loadTraders();
        }
      }
    } catch (err) {
      console.error('Failed to activate trader:', err);
    }
  };

  // Handle inactivate trader
  const handleInactivate = async (traderId: string) => {
    try {
      const profileId = await getTraderProfileIdFromTradingAccount(traderId);
      if (profileId) {
        const result = await dispatch(getEntity(profileId));
        const entity = result.payload?.data || traderProfileEntity;
        if (entity && entity.id) {
          const updatedEntity: ITraderProfile = {
            ...entity,
            status: 'INACTIVE',
          };
          await dispatch(updateEntity(updatedEntity));
          loadTraders();
        }
      }
    } catch (err) {
      console.error('Failed to inactivate trader:', err);
    }
  };

  // Handle view details
  const handleViewDetails = (traderId: string) => {
    setSelectedTraderId(traderId);
    loadTraderDetails(traderId);
    setShowDetailsModal(true);
  };

  // Handle save (add or edit)
  const handleSave = () => {
    const entity: ITraderProfile = {
      ...formData,
      user: formData.user?.id ? users.find(it => it.id === formData.user?.id) || null : null,
    } as ITraderProfile;

    if (formData.id) {
      dispatch(updateEntity(entity));
    } else {
      dispatch(createEntity(entity));
    }
  };

  // Handle confirm delete
  const handleConfirmDelete = () => {
    if (traderProfileEntity.id) {
      dispatch(deleteEntity(traderProfileEntity.id));
    }
  };

  // Refresh details after journal entry
  const handleJournalSuccess = () => {
    if (selectedTraderId) {
      loadTraderDetails(selectedTraderId);
      loadTraders();
    }
  };

  return (
    <div className="trader-management-page">
      <div className="page-header">
        <h2>Manage Traders</h2>
        <Button color="primary" onClick={handleAdd}>
          <FontAwesomeIcon icon="plus" /> Add Trader
        </Button>
      </div>

      {loading && <div>Loading traders...</div>}

      {!loading && (
        <>
          <Table responsive striped>
            <thead>
              <tr>
                <th>Name</th>
                <th>Login</th>
                <th>Status</th>
                <th>Cash</th>
                <th>Current P&L</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {traders.map(trader => (
                <tr key={trader.traderId}>
                  <td>{trader.name}</td>
                  <td>{trader.login}</td>
                  <td>
                    <span className={`badge ${trader.status === 'active' ? 'bg-success' : 'bg-secondary'}`}>{trader.status}</span>
                  </td>
                  <td>{typeof trader.cash === 'number' ? trader.cash.toFixed(2) : trader.cash}</td>
                  <td>{typeof trader.currentPnl === 'number' ? trader.currentPnl.toFixed(2) : trader.currentPnl}</td>
                  <td>
                    <div className="btn-group">
                      <Button size="sm" color="info" onClick={() => handleViewDetails(trader.traderId)}>
                        <FontAwesomeIcon icon="eye" /> View
                      </Button>
                      <Button size="sm" color="primary" onClick={() => handleEdit(trader.traderId)}>
                        <FontAwesomeIcon icon="pencil-alt" /> Edit
                      </Button>
                      {trader.status === 'active' ? (
                        <Button size="sm" color="warning" onClick={() => handleInactivate(trader.traderId)}>
                          <FontAwesomeIcon icon="ban" /> Inactivate
                        </Button>
                      ) : (
                        <Button size="sm" color="success" onClick={() => handleActivate(trader.traderId)}>
                          <FontAwesomeIcon icon="check" /> Activate
                        </Button>
                      )}
                      <Button size="sm" color="danger" onClick={() => handleDelete(trader.traderId)}>
                        <FontAwesomeIcon icon="trash" /> Remove
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>

          <div className="pagination">
            <Button disabled={page === 0} onClick={() => setPage(p => Math.max(0, p - 1))}>
              Previous
            </Button>
            <span className="mx-3">
              Page {page + 1} of {Math.max(1, Math.ceil(total / size))}
            </span>
            <Button disabled={(page + 1) * size >= total} onClick={() => setPage(p => p + 1)}>
              Next
            </Button>
          </div>
        </>
      )}

      {/* Add Trader Modal */}
      <Modal isOpen={showAddModal} toggle={() => setShowAddModal(false)} size="lg">
        <ModalHeader toggle={() => setShowAddModal(false)}>Add New Trader</ModalHeader>
        <ModalBody>
          <Form>
            <Row>
              <Col md="6">
                <FormGroup>
                  <Label>Display Name *</Label>
                  <Input
                    type="text"
                    value={formData.displayName || ''}
                    onChange={e => setFormData({ ...formData, displayName: e.target.value })}
                    required
                  />
                </FormGroup>
              </Col>
              <Col md="6">
                <FormGroup>
                  <Label>Email *</Label>
                  <Input
                    type="email"
                    value={formData.email || ''}
                    onChange={e => setFormData({ ...formData, email: e.target.value })}
                    required
                  />
                </FormGroup>
              </Col>
            </Row>
            <Row>
              <Col md="6">
                <FormGroup>
                  <Label>Mobile</Label>
                  <Input type="text" value={formData.mobile || ''} onChange={e => setFormData({ ...formData, mobile: e.target.value })} />
                </FormGroup>
              </Col>
              <Col md="6">
                <FormGroup>
                  <Label>KYC Status *</Label>
                  <Input
                    type="select"
                    value={formData.kycStatus || 'PENDING'}
                    onChange={e => setFormData({ ...formData, kycStatus: e.target.value as keyof typeof KycStatus })}
                  >
                    {Object.keys(KycStatus).map(status => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </Input>
                </FormGroup>
              </Col>
            </Row>
            <Row>
              <Col md="6">
                <FormGroup>
                  <Label>Status *</Label>
                  <Input
                    type="select"
                    value={formData.status || 'ACTIVE'}
                    onChange={e => setFormData({ ...formData, status: e.target.value as keyof typeof AccountStatus })}
                  >
                    {Object.keys(AccountStatus).map(status => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </Input>
                </FormGroup>
              </Col>
              <Col md="6">
                <FormGroup>
                  <Label>User</Label>
                  <Input
                    type="select"
                    value={formData.user?.id?.toString() || ''}
                    onChange={e => setFormData({ ...formData, user: users.find(u => u.id.toString() === e.target.value) || null })}
                  >
                    <option value="">Select a user</option>
                    {users.map(user => (
                      <option key={user.id} value={user.id}>
                        {user.login}
                      </option>
                    ))}
                  </Input>
                </FormGroup>
              </Col>
            </Row>
          </Form>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={() => setShowAddModal(false)}>
            Cancel
          </Button>
          <Button color="primary" onClick={handleSave} disabled={updating || !formData.displayName || !formData.email}>
            {updating ? 'Saving...' : 'Save'}
          </Button>
        </ModalFooter>
      </Modal>

      {/* Edit Trader Modal */}
      <Modal isOpen={showEditModal} toggle={() => setShowEditModal(false)} size="lg">
        <ModalHeader toggle={() => setShowEditModal(false)}>Edit Trader</ModalHeader>
        <ModalBody>
          <Form>
            <Row>
              <Col md="6">
                <FormGroup>
                  <Label>Display Name *</Label>
                  <Input
                    type="text"
                    value={formData.displayName || ''}
                    onChange={e => setFormData({ ...formData, displayName: e.target.value })}
                    required
                  />
                </FormGroup>
              </Col>
              <Col md="6">
                <FormGroup>
                  <Label>Email *</Label>
                  <Input
                    type="email"
                    value={formData.email || ''}
                    onChange={e => setFormData({ ...formData, email: e.target.value })}
                    required
                  />
                </FormGroup>
              </Col>
            </Row>
            <Row>
              <Col md="6">
                <FormGroup>
                  <Label>Mobile</Label>
                  <Input type="text" value={formData.mobile || ''} onChange={e => setFormData({ ...formData, mobile: e.target.value })} />
                </FormGroup>
              </Col>
              <Col md="6">
                <FormGroup>
                  <Label>KYC Status *</Label>
                  <Input
                    type="select"
                    value={formData.kycStatus || 'PENDING'}
                    onChange={e => setFormData({ ...formData, kycStatus: e.target.value as keyof typeof KycStatus })}
                  >
                    {Object.keys(KycStatus).map(status => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </Input>
                </FormGroup>
              </Col>
            </Row>
            <Row>
              <Col md="6">
                <FormGroup>
                  <Label>Status *</Label>
                  <Input
                    type="select"
                    value={formData.status || 'ACTIVE'}
                    onChange={e => setFormData({ ...formData, status: e.target.value as keyof typeof AccountStatus })}
                  >
                    {Object.keys(AccountStatus).map(status => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </Input>
                </FormGroup>
              </Col>
              <Col md="6">
                <FormGroup>
                  <Label>User</Label>
                  <Input
                    type="select"
                    value={formData.user?.id?.toString() || ''}
                    onChange={e => setFormData({ ...formData, user: users.find(u => u.id.toString() === e.target.value) || null })}
                  >
                    <option value="">Select a user</option>
                    {users.map(user => (
                      <option key={user.id} value={user.id}>
                        {user.login}
                      </option>
                    ))}
                  </Input>
                </FormGroup>
              </Col>
            </Row>
          </Form>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={() => setShowEditModal(false)}>
            Cancel
          </Button>
          <Button color="primary" onClick={handleSave} disabled={updating || !formData.displayName || !formData.email}>
            {updating ? 'Saving...' : 'Save'}
          </Button>
        </ModalFooter>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal isOpen={showDeleteModal} toggle={() => setShowDeleteModal(false)}>
        <ModalHeader toggle={() => setShowDeleteModal(false)}>Confirm Delete</ModalHeader>
        <ModalBody>Are you sure you want to delete trader profile {traderProfileEntity.id}? This action cannot be undone.</ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={() => setShowDeleteModal(false)}>
            Cancel
          </Button>
          <Button color="danger" onClick={handleConfirmDelete} disabled={updating}>
            {updating ? 'Deleting...' : 'Delete'}
          </Button>
        </ModalFooter>
      </Modal>

      {/* Trader Details Modal */}
      <Modal isOpen={showDetailsModal} toggle={() => setShowDetailsModal(false)} size="lg">
        <ModalHeader toggle={() => setShowDetailsModal(false)}>Trader Details</ModalHeader>
        <ModalBody>
          {detailsLoading && <div>Loading...</div>}
          {!detailsLoading && traderDetails && (
            <>
              <div className="mb-4">
                <h5>Summary</h5>
                <Row>
                  <Col md="6">
                    <p>
                      <strong>Name:</strong> {traderDetails.summary?.name}
                    </p>
                    <p>
                      <strong>Login:</strong> {traderDetails.summary?.login}
                    </p>
                    <p>
                      <strong>Status:</strong> {traderDetails.summary?.status}
                    </p>
                  </Col>
                  <Col md="6">
                    <p>
                      <strong>Cash:</strong>{' '}
                      {typeof traderDetails.summary?.cash === 'number'
                        ? traderDetails.summary.cash.toFixed(2)
                        : traderDetails.summary?.cash}
                    </p>
                    <p>
                      <strong>Current P&L:</strong>{' '}
                      {typeof traderDetails.summary?.currentPnl === 'number'
                        ? traderDetails.summary.currentPnl.toFixed(2)
                        : traderDetails.summary?.currentPnl}
                    </p>
                  </Col>
                </Row>
              </div>

              <div className="mb-4">
                <h5>Funds Journal</h5>
                {selectedTraderId && <JournalForm tradingAccountId={selectedTraderId} onSuccess={handleJournalSuccess} />}
              </div>

              <div>
                <h5>Recent Ledger</h5>
                <Table responsive size="sm">
                  <thead>
                    <tr>
                      <th>When</th>
                      <th>Type</th>
                      <th>Amount</th>
                      <th>Reason</th>
                    </tr>
                  </thead>
                  <tbody>
                    {traderDetails.recentLedger?.slice(0, 10).map(le => (
                      <tr key={le.id}>
                        <td>{new Date(le.createdAt).toLocaleString()}</td>
                        <td>{le.type}</td>
                        <td>{typeof le.amount === 'number' ? le.amount.toFixed(2) : le.amount}</td>
                        <td>{le.reason}</td>
                      </tr>
                    ))}
                    {(!traderDetails.recentLedger || traderDetails.recentLedger.length === 0) && (
                      <tr>
                        <td colSpan={4} className="text-center">
                          No ledger entries found
                        </td>
                      </tr>
                    )}
                  </tbody>
                </Table>
              </div>
            </>
          )}
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={() => setShowDetailsModal(false)}>
            Close
          </Button>
        </ModalFooter>
      </Modal>
    </div>
  );
};

export default TraderManagementPage;

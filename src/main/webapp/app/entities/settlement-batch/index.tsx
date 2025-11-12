import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import SettlementBatch from './settlement-batch';
import SettlementBatchDetail from './settlement-batch-detail';
import SettlementBatchUpdate from './settlement-batch-update';
import SettlementBatchDeleteDialog from './settlement-batch-delete-dialog';

const SettlementBatchRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<SettlementBatch />} />
    <Route path="new" element={<SettlementBatchUpdate />} />
    <Route path=":id">
      <Route index element={<SettlementBatchDetail />} />
      <Route path="edit" element={<SettlementBatchUpdate />} />
      <Route path="delete" element={<SettlementBatchDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default SettlementBatchRoutes;

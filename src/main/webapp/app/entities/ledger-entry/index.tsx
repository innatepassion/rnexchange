import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import LedgerEntry from './ledger-entry';
import LedgerEntryDetail from './ledger-entry-detail';
import LedgerEntryUpdate from './ledger-entry-update';
import LedgerEntryDeleteDialog from './ledger-entry-delete-dialog';

const LedgerEntryRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<LedgerEntry />} />
    <Route path="new" element={<LedgerEntryUpdate />} />
    <Route path=":id">
      <Route index element={<LedgerEntryDetail />} />
      <Route path="edit" element={<LedgerEntryUpdate />} />
      <Route path="delete" element={<LedgerEntryDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default LedgerEntryRoutes;

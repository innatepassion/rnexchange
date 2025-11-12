import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Lot from './lot';
import LotDetail from './lot-detail';
import LotUpdate from './lot-update';
import LotDeleteDialog from './lot-delete-dialog';

const LotRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Lot />} />
    <Route path="new" element={<LotUpdate />} />
    <Route path=":id">
      <Route index element={<LotDetail />} />
      <Route path="edit" element={<LotUpdate />} />
      <Route path="delete" element={<LotDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default LotRoutes;

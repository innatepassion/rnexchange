import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Exchange from './exchange';
import ExchangeDetail from './exchange-detail';
import ExchangeUpdate from './exchange-update';
import ExchangeDeleteDialog from './exchange-delete-dialog';

const ExchangeRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Exchange />} />
    <Route path="new" element={<ExchangeUpdate />} />
    <Route path=":id">
      <Route index element={<ExchangeDetail />} />
      <Route path="edit" element={<ExchangeUpdate />} />
      <Route path="delete" element={<ExchangeDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ExchangeRoutes;

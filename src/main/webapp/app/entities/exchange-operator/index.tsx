import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import ExchangeOperator from './exchange-operator';
import ExchangeOperatorDetail from './exchange-operator-detail';
import ExchangeOperatorUpdate from './exchange-operator-update';
import ExchangeOperatorDeleteDialog from './exchange-operator-delete-dialog';

const ExchangeOperatorRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<ExchangeOperator />} />
    <Route path="new" element={<ExchangeOperatorUpdate />} />
    <Route path=":id">
      <Route index element={<ExchangeOperatorDetail />} />
      <Route path="edit" element={<ExchangeOperatorUpdate />} />
      <Route path="delete" element={<ExchangeOperatorDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ExchangeOperatorRoutes;

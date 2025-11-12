import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import ExchangeIntegration from './exchange-integration';
import ExchangeIntegrationDetail from './exchange-integration-detail';
import ExchangeIntegrationUpdate from './exchange-integration-update';
import ExchangeIntegrationDeleteDialog from './exchange-integration-delete-dialog';

const ExchangeIntegrationRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<ExchangeIntegration />} />
    <Route path="new" element={<ExchangeIntegrationUpdate />} />
    <Route path=":id">
      <Route index element={<ExchangeIntegrationDetail />} />
      <Route path="edit" element={<ExchangeIntegrationUpdate />} />
      <Route path="delete" element={<ExchangeIntegrationDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ExchangeIntegrationRoutes;

import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import RiskAlert from './risk-alert';
import RiskAlertDetail from './risk-alert-detail';
import RiskAlertUpdate from './risk-alert-update';
import RiskAlertDeleteDialog from './risk-alert-delete-dialog';

const RiskAlertRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<RiskAlert />} />
    <Route path="new" element={<RiskAlertUpdate />} />
    <Route path=":id">
      <Route index element={<RiskAlertDetail />} />
      <Route path="edit" element={<RiskAlertUpdate />} />
      <Route path="delete" element={<RiskAlertDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default RiskAlertRoutes;

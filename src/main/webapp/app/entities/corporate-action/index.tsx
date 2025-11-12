import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CorporateAction from './corporate-action';
import CorporateActionDetail from './corporate-action-detail';
import CorporateActionUpdate from './corporate-action-update';
import CorporateActionDeleteDialog from './corporate-action-delete-dialog';

const CorporateActionRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CorporateAction />} />
    <Route path="new" element={<CorporateActionUpdate />} />
    <Route path=":id">
      <Route index element={<CorporateActionDetail />} />
      <Route path="edit" element={<CorporateActionUpdate />} />
      <Route path="delete" element={<CorporateActionDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CorporateActionRoutes;

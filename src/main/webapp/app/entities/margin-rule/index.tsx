import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import MarginRule from './margin-rule';
import MarginRuleDetail from './margin-rule-detail';
import MarginRuleUpdate from './margin-rule-update';
import MarginRuleDeleteDialog from './margin-rule-delete-dialog';

const MarginRuleRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<MarginRule />} />
    <Route path="new" element={<MarginRuleUpdate />} />
    <Route path=":id">
      <Route index element={<MarginRuleDetail />} />
      <Route path="edit" element={<MarginRuleUpdate />} />
      <Route path="delete" element={<MarginRuleDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default MarginRuleRoutes;

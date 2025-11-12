import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Execution from './execution';
import ExecutionDetail from './execution-detail';
import ExecutionUpdate from './execution-update';
import ExecutionDeleteDialog from './execution-delete-dialog';

const ExecutionRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Execution />} />
    <Route path="new" element={<ExecutionUpdate />} />
    <Route path=":id">
      <Route index element={<ExecutionDetail />} />
      <Route path="edit" element={<ExecutionUpdate />} />
      <Route path="delete" element={<ExecutionDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ExecutionRoutes;

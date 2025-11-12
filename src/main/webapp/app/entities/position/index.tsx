import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Position from './position';
import PositionDetail from './position-detail';
import PositionUpdate from './position-update';
import PositionDeleteDialog from './position-delete-dialog';

const PositionRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Position />} />
    <Route path="new" element={<PositionUpdate />} />
    <Route path=":id">
      <Route index element={<PositionDetail />} />
      <Route path="edit" element={<PositionUpdate />} />
      <Route path="delete" element={<PositionDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PositionRoutes;

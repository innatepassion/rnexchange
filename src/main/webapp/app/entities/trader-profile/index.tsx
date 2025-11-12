import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import TraderProfile from './trader-profile';
import TraderProfileDetail from './trader-profile-detail';
import TraderProfileUpdate from './trader-profile-update';
import TraderProfileDeleteDialog from './trader-profile-delete-dialog';

const TraderProfileRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<TraderProfile />} />
    <Route path="new" element={<TraderProfileUpdate />} />
    <Route path=":id">
      <Route index element={<TraderProfileDetail />} />
      <Route path="edit" element={<TraderProfileUpdate />} />
      <Route path="delete" element={<TraderProfileDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default TraderProfileRoutes;

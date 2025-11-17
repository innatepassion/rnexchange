import React from 'react';
import { Route } from 'react-router';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import TraderDashboard from './trader-dashboard';
import TraderStatementsModule from './statements';

const TraderRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<TraderDashboard />} />
    <Route path="statements" element={<TraderStatementsModule />} />
    {/* Additional trader routes can be added here */}
  </ErrorBoundaryRoutes>
);

export default TraderRoutes;

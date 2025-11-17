import React from 'react';
import { Route } from 'react-router';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import BrokerPortfolio from './broker-portfolio';
import BrokerSettlementsModule from '../broker/settlements';

const BrokerAdminRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<BrokerPortfolio />} />
    <Route path="settlements" element={<BrokerSettlementsModule />} />
    {/* Additional broker admin routes can be added here */}
  </ErrorBoundaryRoutes>
);

export default BrokerAdminRoutes;

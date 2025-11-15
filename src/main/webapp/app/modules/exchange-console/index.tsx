import React from 'react';
import { Route } from 'react-router-dom';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import MarketDataPanel from './market-data-panel';

const ExchangeConsoleRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<MarketDataPanel />} />
  </ErrorBoundaryRoutes>
);

export default ExchangeConsoleRoutes;

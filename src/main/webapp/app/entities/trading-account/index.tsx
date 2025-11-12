import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import TradingAccount from './trading-account';
import TradingAccountDetail from './trading-account-detail';
import TradingAccountUpdate from './trading-account-update';
import TradingAccountDeleteDialog from './trading-account-delete-dialog';

const TradingAccountRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<TradingAccount />} />
    <Route path="new" element={<TradingAccountUpdate />} />
    <Route path=":id">
      <Route index element={<TradingAccountDetail />} />
      <Route path="edit" element={<TradingAccountUpdate />} />
      <Route path="delete" element={<TradingAccountDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default TradingAccountRoutes;

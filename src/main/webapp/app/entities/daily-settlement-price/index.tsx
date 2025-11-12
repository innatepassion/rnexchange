import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import DailySettlementPrice from './daily-settlement-price';
import DailySettlementPriceDetail from './daily-settlement-price-detail';
import DailySettlementPriceUpdate from './daily-settlement-price-update';
import DailySettlementPriceDeleteDialog from './daily-settlement-price-delete-dialog';

const DailySettlementPriceRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<DailySettlementPrice />} />
    <Route path="new" element={<DailySettlementPriceUpdate />} />
    <Route path=":id">
      <Route index element={<DailySettlementPriceDetail />} />
      <Route path="edit" element={<DailySettlementPriceUpdate />} />
      <Route path="delete" element={<DailySettlementPriceDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DailySettlementPriceRoutes;

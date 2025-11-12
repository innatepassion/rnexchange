import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import MarketHoliday from './market-holiday';
import MarketHolidayDetail from './market-holiday-detail';
import MarketHolidayUpdate from './market-holiday-update';
import MarketHolidayDeleteDialog from './market-holiday-delete-dialog';

const MarketHolidayRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<MarketHoliday />} />
    <Route path="new" element={<MarketHolidayUpdate />} />
    <Route path=":id">
      <Route index element={<MarketHolidayDetail />} />
      <Route path="edit" element={<MarketHolidayUpdate />} />
      <Route path="delete" element={<MarketHolidayDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default MarketHolidayRoutes;

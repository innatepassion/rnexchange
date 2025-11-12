import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Instrument from './instrument';
import InstrumentDetail from './instrument-detail';
import InstrumentUpdate from './instrument-update';
import InstrumentDeleteDialog from './instrument-delete-dialog';

const InstrumentRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Instrument />} />
    <Route path="new" element={<InstrumentUpdate />} />
    <Route path=":id">
      <Route index element={<InstrumentDetail />} />
      <Route path="edit" element={<InstrumentUpdate />} />
      <Route path="delete" element={<InstrumentDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default InstrumentRoutes;

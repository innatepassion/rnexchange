import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import BrokerDesk from './broker-desk';
import BrokerDeskDetail from './broker-desk-detail';
import BrokerDeskUpdate from './broker-desk-update';
import BrokerDeskDeleteDialog from './broker-desk-delete-dialog';

const BrokerDeskRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<BrokerDesk />} />
    <Route path="new" element={<BrokerDeskUpdate />} />
    <Route path=":id">
      <Route index element={<BrokerDeskDetail />} />
      <Route path="edit" element={<BrokerDeskUpdate />} />
      <Route path="delete" element={<BrokerDeskDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default BrokerDeskRoutes;

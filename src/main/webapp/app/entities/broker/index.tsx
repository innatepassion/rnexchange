import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Broker from './broker';
import BrokerDetail from './broker-detail';
import BrokerUpdate from './broker-update';
import BrokerDeleteDialog from './broker-delete-dialog';

const BrokerRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Broker />} />
    <Route path="new" element={<BrokerUpdate />} />
    <Route path=":id">
      <Route index element={<BrokerDetail />} />
      <Route path="edit" element={<BrokerUpdate />} />
      <Route path="delete" element={<BrokerDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default BrokerRoutes;

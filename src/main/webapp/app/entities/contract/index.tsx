import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Contract from './contract';
import ContractDetail from './contract-detail';
import ContractUpdate from './contract-update';
import ContractDeleteDialog from './contract-delete-dialog';

const ContractRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Contract />} />
    <Route path="new" element={<ContractUpdate />} />
    <Route path=":id">
      <Route index element={<ContractDetail />} />
      <Route path="edit" element={<ContractUpdate />} />
      <Route path="delete" element={<ContractDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ContractRoutes;

import React, { useEffect, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { login } from 'app/shared/reducers/authentication';
import { resolveRoleLandingRoute } from 'app/shared/auth/role-landing-resolver';
import LoginModal from './login-modal';

export const Login = () => {
  const dispatch = useAppDispatch();
  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
  const loginError = useAppSelector(state => state.authentication.loginError);
  const showModalLogin = useAppSelector(state => state.authentication.showModalLogin);
  const account = useAppSelector(state => state.authentication.account);
  const [showModal, setShowModal] = useState(showModalLogin);
  const navigate = useNavigate();
  const pageLocation = useLocation();

  useEffect(() => {
    setShowModal(true);
  }, []);

  const handleLogin = (username, password, rememberMe = false) => dispatch(login(username, password, rememberMe));

  const handleClose = () => {
    setShowModal(false);
    navigate('/');
  };

  // M6 Phase 2: Use role-based landing route if no specific redirect target
  const { from } = pageLocation.state || { from: null };
  if (isAuthenticated) {
    // If there's a specific redirect target, use it; otherwise use role-based landing
    if (from && from.pathname && from.pathname !== '/') {
      return <Navigate to={from} replace />;
    }
    // Resolve role-based landing route
    const authorities = account?.authorities || [];
    const landingRoute = resolveRoleLandingRoute(authorities);
    return <Navigate to={landingRoute} replace />;
  }
  return <LoginModal showModal={showModal} handleLogin={handleLogin} handleClose={handleClose} loginError={loginError} />;
};

export default Login;

import './home.scss';

import React from 'react';
import { Link } from 'react-router-dom';
import { Translate } from 'react-jhipster';
import { Alert, Button, Col, Row, Spinner } from 'reactstrap';

import { useAppSelector } from 'app/config/store';

/**
 * M6 User Story 4, Task T042: RNExchange-branded landing page.
 * Replaces default JHipster home page with RNExchange branding, logo, description, and role CTAs.
 */
export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);

  return (
    <div className="rnexchange-landing">
      <Row className="justify-content-center">
        <Col md="10" lg="8">
          <div className="text-center mb-5">
            {/* RNExchange Logo */}
            <div className="rnexchange-logo mb-4">
              <img
                src="/content/images/rnexchange-logo.png"
                alt="RNExchange Logo"
                className="img-fluid"
                style={{ maxWidth: '600px', height: 'auto' }}
                onError={e => {
                  // Fallback if logo doesn't exist - show text logo
                  e.currentTarget.style.display = 'none';
                  const parent = e.currentTarget.parentElement;
                  if (parent && !parent.querySelector('.rnexchange-text-logo')) {
                    const textLogo = document.createElement('div');
                    textLogo.className = 'rnexchange-text-logo';
                    textLogo.innerHTML =
                      '<h1 style="font-size: 3rem; font-weight: bold; color: #0066cc;">RNX</h1><h2 style="color: #333;">RNExchange</h2>';
                    parent.appendChild(textLogo);
                  }
                }}
              />
            </div>

            {/* Application Name and Description */}
            <h1 className="display-3 mb-3">
              <Translate contentKey="home.title">RNExchange</Translate>
            </h1>
            <p className="lead mb-4">
              <Translate contentKey="home.subtitle">
                A simulated stock exchange platform for learning and training. Practice trading with virtual money in a realistic market
                environment.
              </Translate>
            </p>

            {/* Simulation Disclaimer */}
            <Alert color="warning" className="mb-4">
              <strong>⚠️ SIMULATED / NOT REAL MONEY</strong>
              <br />
              <Translate contentKey="home.disclaimer">
                This is a simulated trading environment. No real money is involved. All prices, trades, and balances are for educational
                purposes only.
              </Translate>
            </Alert>
          </div>

          {account?.login ? (
            <div className="text-center">
              <Alert color="success">
                <Translate contentKey="home.logged.message" interpolate={{ username: account.login }}>
                  You are logged in as {account.login}.
                </Translate>
              </Alert>
              <div className="mt-3">
                <Link to="/market-watch">
                  <Button color="primary" size="lg">
                    <Translate contentKey="home.goToDashboard">Go to Dashboard</Translate>
                  </Button>
                </Link>
              </div>
            </div>
          ) : account === null ? (
            <div className="text-center">
              <Spinner color="primary" className="mb-3" />
              <p className="text-muted">Loading...</p>
            </div>
          ) : (
            <div className="text-center">
              <h3 className="mb-4">
                <Translate contentKey="home.getStarted">Get Started</Translate>
              </h3>
              <p className="mb-4">
                <Translate contentKey="home.chooseRole">Choose your role to begin:</Translate>
              </p>
              <Row className="justify-content-center">
                <Col md="4" className="mb-3">
                  <div className="card h-100">
                    <div className="card-body text-center">
                      <h4 className="card-title">
                        <Translate contentKey="home.role.trader">Trader</Translate>
                      </h4>
                      <p className="card-text">
                        <Translate contentKey="home.role.trader.desc">
                          Place orders, manage your portfolio, and track your positions.
                        </Translate>
                      </p>
                      <Link to="/login">
                        <Button color="primary" outline>
                          <Translate contentKey="home.loginAsTrader">Login as Trader</Translate>
                        </Button>
                      </Link>
                    </div>
                  </div>
                </Col>
                <Col md="4" className="mb-3">
                  <div className="card h-100">
                    <div className="card-body text-center">
                      <h4 className="card-title">
                        <Translate contentKey="home.role.broker">Broker Admin</Translate>
                      </h4>
                      <p className="card-text">
                        <Translate contentKey="home.role.broker.desc">
                          Manage trader accounts, adjust funds, and review settlements.
                        </Translate>
                      </p>
                      <Link to="/login">
                        <Button color="primary" outline>
                          <Translate contentKey="home.loginAsBroker">Login as Broker</Translate>
                        </Button>
                      </Link>
                    </div>
                  </div>
                </Col>
                <Col md="4" className="mb-3">
                  <div className="card h-100">
                    <div className="card-body text-center">
                      <h4 className="card-title">
                        <Translate contentKey="home.role.exchange">Exchange Operator</Translate>
                      </h4>
                      <p className="card-text">
                        <Translate contentKey="home.role.exchange.desc">
                          Run end-of-day settlement and monitor exchange operations.
                        </Translate>
                      </p>
                      <Link to="/login">
                        <Button color="primary" outline>
                          <Translate contentKey="home.loginAsExchange">Login as Exchange</Translate>
                        </Button>
                      </Link>
                    </div>
                  </div>
                </Col>
              </Row>
            </div>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default Home;

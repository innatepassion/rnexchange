import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './trading-account.reducer';

export const TradingAccountDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const tradingAccountEntity = useAppSelector(state => state.tradingAccount.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="tradingAccountDetailsHeading">
          <Translate contentKey="rnexchangeApp.tradingAccount.detail.title">TradingAccount</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{tradingAccountEntity.id}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="rnexchangeApp.tradingAccount.type">Type</Translate>
            </span>
          </dt>
          <dd>{tradingAccountEntity.type}</dd>
          <dt>
            <span id="baseCcy">
              <Translate contentKey="rnexchangeApp.tradingAccount.baseCcy">Base Ccy</Translate>
            </span>
          </dt>
          <dd>{tradingAccountEntity.baseCcy}</dd>
          <dt>
            <span id="balance">
              <Translate contentKey="rnexchangeApp.tradingAccount.balance">Balance</Translate>
            </span>
          </dt>
          <dd>{tradingAccountEntity.balance}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="rnexchangeApp.tradingAccount.status">Status</Translate>
            </span>
          </dt>
          <dd>{tradingAccountEntity.status}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.tradingAccount.broker">Broker</Translate>
          </dt>
          <dd>{tradingAccountEntity.broker ? tradingAccountEntity.broker.code : ''}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.tradingAccount.trader">Trader</Translate>
          </dt>
          <dd>{tradingAccountEntity.trader ? tradingAccountEntity.trader.displayName : ''}</dd>
        </dl>
        <Button tag={Link} to="/trading-account" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/trading-account/${tradingAccountEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default TradingAccountDetail;

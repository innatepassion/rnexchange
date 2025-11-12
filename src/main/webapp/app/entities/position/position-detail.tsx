import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './position.reducer';

export const PositionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const positionEntity = useAppSelector(state => state.position.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="positionDetailsHeading">
          <Translate contentKey="rnexchangeApp.position.detail.title">Position</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{positionEntity.id}</dd>
          <dt>
            <span id="qty">
              <Translate contentKey="rnexchangeApp.position.qty">Qty</Translate>
            </span>
          </dt>
          <dd>{positionEntity.qty}</dd>
          <dt>
            <span id="avgCost">
              <Translate contentKey="rnexchangeApp.position.avgCost">Avg Cost</Translate>
            </span>
          </dt>
          <dd>{positionEntity.avgCost}</dd>
          <dt>
            <span id="lastPx">
              <Translate contentKey="rnexchangeApp.position.lastPx">Last Px</Translate>
            </span>
          </dt>
          <dd>{positionEntity.lastPx}</dd>
          <dt>
            <span id="unrealizedPnl">
              <Translate contentKey="rnexchangeApp.position.unrealizedPnl">Unrealized Pnl</Translate>
            </span>
          </dt>
          <dd>{positionEntity.unrealizedPnl}</dd>
          <dt>
            <span id="realizedPnl">
              <Translate contentKey="rnexchangeApp.position.realizedPnl">Realized Pnl</Translate>
            </span>
          </dt>
          <dd>{positionEntity.realizedPnl}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.position.tradingAccount">Trading Account</Translate>
          </dt>
          <dd>{positionEntity.tradingAccount ? positionEntity.tradingAccount.id : ''}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.position.instrument">Instrument</Translate>
          </dt>
          <dd>{positionEntity.instrument ? positionEntity.instrument.symbol : ''}</dd>
        </dl>
        <Button tag={Link} to="/position" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/position/${positionEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default PositionDetail;

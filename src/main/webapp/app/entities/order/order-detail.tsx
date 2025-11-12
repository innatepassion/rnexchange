import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './order.reducer';

export const OrderDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const orderEntity = useAppSelector(state => state.order.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="orderDetailsHeading">
          <Translate contentKey="rnexchangeApp.order.detail.title">Order</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{orderEntity.id}</dd>
          <dt>
            <span id="side">
              <Translate contentKey="rnexchangeApp.order.side">Side</Translate>
            </span>
          </dt>
          <dd>{orderEntity.side}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="rnexchangeApp.order.type">Type</Translate>
            </span>
          </dt>
          <dd>{orderEntity.type}</dd>
          <dt>
            <span id="qty">
              <Translate contentKey="rnexchangeApp.order.qty">Qty</Translate>
            </span>
          </dt>
          <dd>{orderEntity.qty}</dd>
          <dt>
            <span id="limitPx">
              <Translate contentKey="rnexchangeApp.order.limitPx">Limit Px</Translate>
            </span>
          </dt>
          <dd>{orderEntity.limitPx}</dd>
          <dt>
            <span id="stopPx">
              <Translate contentKey="rnexchangeApp.order.stopPx">Stop Px</Translate>
            </span>
          </dt>
          <dd>{orderEntity.stopPx}</dd>
          <dt>
            <span id="tif">
              <Translate contentKey="rnexchangeApp.order.tif">Tif</Translate>
            </span>
          </dt>
          <dd>{orderEntity.tif}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="rnexchangeApp.order.status">Status</Translate>
            </span>
          </dt>
          <dd>{orderEntity.status}</dd>
          <dt>
            <span id="venue">
              <Translate contentKey="rnexchangeApp.order.venue">Venue</Translate>
            </span>
          </dt>
          <dd>{orderEntity.venue}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="rnexchangeApp.order.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>{orderEntity.createdAt ? <TextFormat value={orderEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="updatedAt">
              <Translate contentKey="rnexchangeApp.order.updatedAt">Updated At</Translate>
            </span>
          </dt>
          <dd>{orderEntity.updatedAt ? <TextFormat value={orderEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.order.tradingAccount">Trading Account</Translate>
          </dt>
          <dd>{orderEntity.tradingAccount ? orderEntity.tradingAccount.id : ''}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.order.instrument">Instrument</Translate>
          </dt>
          <dd>{orderEntity.instrument ? orderEntity.instrument.symbol : ''}</dd>
        </dl>
        <Button tag={Link} to="/order" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/order/${orderEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default OrderDetail;

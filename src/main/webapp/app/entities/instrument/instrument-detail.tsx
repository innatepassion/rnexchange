import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './instrument.reducer';

export const InstrumentDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const instrumentEntity = useAppSelector(state => state.instrument.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="instrumentDetailsHeading">
          <Translate contentKey="rnexchangeApp.instrument.detail.title">Instrument</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{instrumentEntity.id}</dd>
          <dt>
            <span id="symbol">
              <Translate contentKey="rnexchangeApp.instrument.symbol">Symbol</Translate>
            </span>
          </dt>
          <dd>{instrumentEntity.symbol}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="rnexchangeApp.instrument.name">Name</Translate>
            </span>
          </dt>
          <dd>{instrumentEntity.name}</dd>
          <dt>
            <span id="assetClass">
              <Translate contentKey="rnexchangeApp.instrument.assetClass">Asset Class</Translate>
            </span>
          </dt>
          <dd>{instrumentEntity.assetClass}</dd>
          <dt>
            <span id="exchangeCode">
              <Translate contentKey="rnexchangeApp.instrument.exchangeCode">Exchange Code</Translate>
            </span>
          </dt>
          <dd>{instrumentEntity.exchangeCode}</dd>
          <dt>
            <span id="tickSize">
              <Translate contentKey="rnexchangeApp.instrument.tickSize">Tick Size</Translate>
            </span>
          </dt>
          <dd>{instrumentEntity.tickSize}</dd>
          <dt>
            <span id="lotSize">
              <Translate contentKey="rnexchangeApp.instrument.lotSize">Lot Size</Translate>
            </span>
          </dt>
          <dd>{instrumentEntity.lotSize}</dd>
          <dt>
            <span id="currency">
              <Translate contentKey="rnexchangeApp.instrument.currency">Currency</Translate>
            </span>
          </dt>
          <dd>{instrumentEntity.currency}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="rnexchangeApp.instrument.status">Status</Translate>
            </span>
          </dt>
          <dd>{instrumentEntity.status}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.instrument.exchange">Exchange</Translate>
          </dt>
          <dd>{instrumentEntity.exchange ? instrumentEntity.exchange.code : ''}</dd>
        </dl>
        <Button tag={Link} to="/instrument" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/instrument/${instrumentEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default InstrumentDetail;

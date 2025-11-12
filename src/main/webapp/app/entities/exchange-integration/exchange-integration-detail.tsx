import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './exchange-integration.reducer';

export const ExchangeIntegrationDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const exchangeIntegrationEntity = useAppSelector(state => state.exchangeIntegration.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="exchangeIntegrationDetailsHeading">
          <Translate contentKey="rnexchangeApp.exchangeIntegration.detail.title">ExchangeIntegration</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{exchangeIntegrationEntity.id}</dd>
          <dt>
            <span id="provider">
              <Translate contentKey="rnexchangeApp.exchangeIntegration.provider">Provider</Translate>
            </span>
          </dt>
          <dd>{exchangeIntegrationEntity.provider}</dd>
          <dt>
            <span id="apiKey">
              <Translate contentKey="rnexchangeApp.exchangeIntegration.apiKey">Api Key</Translate>
            </span>
          </dt>
          <dd>{exchangeIntegrationEntity.apiKey}</dd>
          <dt>
            <span id="apiSecret">
              <Translate contentKey="rnexchangeApp.exchangeIntegration.apiSecret">Api Secret</Translate>
            </span>
          </dt>
          <dd>{exchangeIntegrationEntity.apiSecret}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="rnexchangeApp.exchangeIntegration.status">Status</Translate>
            </span>
          </dt>
          <dd>{exchangeIntegrationEntity.status}</dd>
          <dt>
            <span id="lastHeartbeat">
              <Translate contentKey="rnexchangeApp.exchangeIntegration.lastHeartbeat">Last Heartbeat</Translate>
            </span>
          </dt>
          <dd>
            {exchangeIntegrationEntity.lastHeartbeat ? (
              <TextFormat value={exchangeIntegrationEntity.lastHeartbeat} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <Translate contentKey="rnexchangeApp.exchangeIntegration.exchange">Exchange</Translate>
          </dt>
          <dd>{exchangeIntegrationEntity.exchange ? exchangeIntegrationEntity.exchange.code : ''}</dd>
        </dl>
        <Button tag={Link} to="/exchange-integration" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/exchange-integration/${exchangeIntegrationEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ExchangeIntegrationDetail;

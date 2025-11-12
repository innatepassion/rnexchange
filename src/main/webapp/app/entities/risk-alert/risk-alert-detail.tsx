import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './risk-alert.reducer';

export const RiskAlertDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const riskAlertEntity = useAppSelector(state => state.riskAlert.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="riskAlertDetailsHeading">
          <Translate contentKey="rnexchangeApp.riskAlert.detail.title">RiskAlert</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{riskAlertEntity.id}</dd>
          <dt>
            <span id="alertType">
              <Translate contentKey="rnexchangeApp.riskAlert.alertType">Alert Type</Translate>
            </span>
          </dt>
          <dd>{riskAlertEntity.alertType}</dd>
          <dt>
            <span id="description">
              <Translate contentKey="rnexchangeApp.riskAlert.description">Description</Translate>
            </span>
          </dt>
          <dd>{riskAlertEntity.description}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="rnexchangeApp.riskAlert.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>
            {riskAlertEntity.createdAt ? <TextFormat value={riskAlertEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <Translate contentKey="rnexchangeApp.riskAlert.tradingAccount">Trading Account</Translate>
          </dt>
          <dd>{riskAlertEntity.tradingAccount ? riskAlertEntity.tradingAccount.id : ''}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.riskAlert.trader">Trader</Translate>
          </dt>
          <dd>{riskAlertEntity.trader ? riskAlertEntity.trader.displayName : ''}</dd>
        </dl>
        <Button tag={Link} to="/risk-alert" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/risk-alert/${riskAlertEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default RiskAlertDetail;

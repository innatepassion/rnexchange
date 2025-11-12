import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './corporate-action.reducer';

export const CorporateActionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const corporateActionEntity = useAppSelector(state => state.corporateAction.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="corporateActionDetailsHeading">
          <Translate contentKey="rnexchangeApp.corporateAction.detail.title">CorporateAction</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{corporateActionEntity.id}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="rnexchangeApp.corporateAction.type">Type</Translate>
            </span>
          </dt>
          <dd>{corporateActionEntity.type}</dd>
          <dt>
            <span id="instrumentSymbol">
              <Translate contentKey="rnexchangeApp.corporateAction.instrumentSymbol">Instrument Symbol</Translate>
            </span>
          </dt>
          <dd>{corporateActionEntity.instrumentSymbol}</dd>
          <dt>
            <span id="exDate">
              <Translate contentKey="rnexchangeApp.corporateAction.exDate">Ex Date</Translate>
            </span>
          </dt>
          <dd>
            {corporateActionEntity.exDate ? (
              <TextFormat value={corporateActionEntity.exDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="payDate">
              <Translate contentKey="rnexchangeApp.corporateAction.payDate">Pay Date</Translate>
            </span>
          </dt>
          <dd>
            {corporateActionEntity.payDate ? (
              <TextFormat value={corporateActionEntity.payDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="ratio">
              <Translate contentKey="rnexchangeApp.corporateAction.ratio">Ratio</Translate>
            </span>
          </dt>
          <dd>{corporateActionEntity.ratio}</dd>
          <dt>
            <span id="cashAmount">
              <Translate contentKey="rnexchangeApp.corporateAction.cashAmount">Cash Amount</Translate>
            </span>
          </dt>
          <dd>{corporateActionEntity.cashAmount}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.corporateAction.instrument">Instrument</Translate>
          </dt>
          <dd>{corporateActionEntity.instrument ? corporateActionEntity.instrument.symbol : ''}</dd>
        </dl>
        <Button tag={Link} to="/corporate-action" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/corporate-action/${corporateActionEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default CorporateActionDetail;

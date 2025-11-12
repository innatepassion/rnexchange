import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './daily-settlement-price.reducer';

export const DailySettlementPriceDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const dailySettlementPriceEntity = useAppSelector(state => state.dailySettlementPrice.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="dailySettlementPriceDetailsHeading">
          <Translate contentKey="rnexchangeApp.dailySettlementPrice.detail.title">DailySettlementPrice</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{dailySettlementPriceEntity.id}</dd>
          <dt>
            <span id="refDate">
              <Translate contentKey="rnexchangeApp.dailySettlementPrice.refDate">Ref Date</Translate>
            </span>
          </dt>
          <dd>
            {dailySettlementPriceEntity.refDate ? (
              <TextFormat value={dailySettlementPriceEntity.refDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="instrumentSymbol">
              <Translate contentKey="rnexchangeApp.dailySettlementPrice.instrumentSymbol">Instrument Symbol</Translate>
            </span>
          </dt>
          <dd>{dailySettlementPriceEntity.instrumentSymbol}</dd>
          <dt>
            <span id="settlePrice">
              <Translate contentKey="rnexchangeApp.dailySettlementPrice.settlePrice">Settle Price</Translate>
            </span>
          </dt>
          <dd>{dailySettlementPriceEntity.settlePrice}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.dailySettlementPrice.instrument">Instrument</Translate>
          </dt>
          <dd>{dailySettlementPriceEntity.instrument ? dailySettlementPriceEntity.instrument.symbol : ''}</dd>
        </dl>
        <Button tag={Link} to="/daily-settlement-price" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/daily-settlement-price/${dailySettlementPriceEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default DailySettlementPriceDetail;

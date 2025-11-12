import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './market-holiday.reducer';

export const MarketHolidayDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const marketHolidayEntity = useAppSelector(state => state.marketHoliday.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="marketHolidayDetailsHeading">
          <Translate contentKey="rnexchangeApp.marketHoliday.detail.title">MarketHoliday</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{marketHolidayEntity.id}</dd>
          <dt>
            <span id="tradeDate">
              <Translate contentKey="rnexchangeApp.marketHoliday.tradeDate">Trade Date</Translate>
            </span>
          </dt>
          <dd>
            {marketHolidayEntity.tradeDate ? (
              <TextFormat value={marketHolidayEntity.tradeDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="reason">
              <Translate contentKey="rnexchangeApp.marketHoliday.reason">Reason</Translate>
            </span>
          </dt>
          <dd>{marketHolidayEntity.reason}</dd>
          <dt>
            <span id="isHoliday">
              <Translate contentKey="rnexchangeApp.marketHoliday.isHoliday">Is Holiday</Translate>
            </span>
          </dt>
          <dd>{marketHolidayEntity.isHoliday ? 'true' : 'false'}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.marketHoliday.exchange">Exchange</Translate>
          </dt>
          <dd>{marketHolidayEntity.exchange ? marketHolidayEntity.exchange.code : ''}</dd>
        </dl>
        <Button tag={Link} to="/market-holiday" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/market-holiday/${marketHolidayEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default MarketHolidayDetail;

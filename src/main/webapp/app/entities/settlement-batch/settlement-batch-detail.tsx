import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './settlement-batch.reducer';

export const SettlementBatchDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const settlementBatchEntity = useAppSelector(state => state.settlementBatch.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="settlementBatchDetailsHeading">
          <Translate contentKey="rnexchangeApp.settlementBatch.detail.title">SettlementBatch</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{settlementBatchEntity.id}</dd>
          <dt>
            <span id="refDate">
              <Translate contentKey="rnexchangeApp.settlementBatch.refDate">Ref Date</Translate>
            </span>
          </dt>
          <dd>
            {settlementBatchEntity.refDate ? (
              <TextFormat value={settlementBatchEntity.refDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="kind">
              <Translate contentKey="rnexchangeApp.settlementBatch.kind">Kind</Translate>
            </span>
          </dt>
          <dd>{settlementBatchEntity.kind}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="rnexchangeApp.settlementBatch.status">Status</Translate>
            </span>
          </dt>
          <dd>{settlementBatchEntity.status}</dd>
          <dt>
            <span id="remarks">
              <Translate contentKey="rnexchangeApp.settlementBatch.remarks">Remarks</Translate>
            </span>
          </dt>
          <dd>{settlementBatchEntity.remarks}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.settlementBatch.exchange">Exchange</Translate>
          </dt>
          <dd>{settlementBatchEntity.exchange ? settlementBatchEntity.exchange.code : ''}</dd>
        </dl>
        <Button tag={Link} to="/settlement-batch" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/settlement-batch/${settlementBatchEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default SettlementBatchDetail;

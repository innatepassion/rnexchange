import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './execution.reducer';

export const ExecutionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const executionEntity = useAppSelector(state => state.execution.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="executionDetailsHeading">
          <Translate contentKey="rnexchangeApp.execution.detail.title">Execution</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{executionEntity.id}</dd>
          <dt>
            <span id="execTs">
              <Translate contentKey="rnexchangeApp.execution.execTs">Exec Ts</Translate>
            </span>
          </dt>
          <dd>{executionEntity.execTs ? <TextFormat value={executionEntity.execTs} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="px">
              <Translate contentKey="rnexchangeApp.execution.px">Px</Translate>
            </span>
          </dt>
          <dd>{executionEntity.px}</dd>
          <dt>
            <span id="qty">
              <Translate contentKey="rnexchangeApp.execution.qty">Qty</Translate>
            </span>
          </dt>
          <dd>{executionEntity.qty}</dd>
          <dt>
            <span id="liquidity">
              <Translate contentKey="rnexchangeApp.execution.liquidity">Liquidity</Translate>
            </span>
          </dt>
          <dd>{executionEntity.liquidity}</dd>
          <dt>
            <span id="fee">
              <Translate contentKey="rnexchangeApp.execution.fee">Fee</Translate>
            </span>
          </dt>
          <dd>{executionEntity.fee}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.execution.order">Order</Translate>
          </dt>
          <dd>{executionEntity.order ? executionEntity.order.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/execution" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/execution/${executionEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ExecutionDetail;

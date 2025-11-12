import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './broker.reducer';

export const BrokerDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const brokerEntity = useAppSelector(state => state.broker.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="brokerDetailsHeading">
          <Translate contentKey="rnexchangeApp.broker.detail.title">Broker</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.id}</dd>
          <dt>
            <span id="code">
              <Translate contentKey="rnexchangeApp.broker.code">Code</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.code}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="rnexchangeApp.broker.name">Name</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.name}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="rnexchangeApp.broker.status">Status</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.status}</dd>
          <dt>
            <span id="createdDate">
              <Translate contentKey="rnexchangeApp.broker.createdDate">Created Date</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.createdDate ? <TextFormat value={brokerEntity.createdDate} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.broker.exchange">Exchange</Translate>
          </dt>
          <dd>{brokerEntity.exchange ? brokerEntity.exchange.code : ''}</dd>
        </dl>
        <Button tag={Link} to="/broker" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/broker/${brokerEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default BrokerDetail;

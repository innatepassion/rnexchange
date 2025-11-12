import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './exchange.reducer';

export const ExchangeDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const exchangeEntity = useAppSelector(state => state.exchange.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="exchangeDetailsHeading">
          <Translate contentKey="rnexchangeApp.exchange.detail.title">Exchange</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{exchangeEntity.id}</dd>
          <dt>
            <span id="code">
              <Translate contentKey="rnexchangeApp.exchange.code">Code</Translate>
            </span>
          </dt>
          <dd>{exchangeEntity.code}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="rnexchangeApp.exchange.name">Name</Translate>
            </span>
          </dt>
          <dd>{exchangeEntity.name}</dd>
          <dt>
            <span id="timezone">
              <Translate contentKey="rnexchangeApp.exchange.timezone">Timezone</Translate>
            </span>
          </dt>
          <dd>{exchangeEntity.timezone}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="rnexchangeApp.exchange.status">Status</Translate>
            </span>
          </dt>
          <dd>{exchangeEntity.status}</dd>
        </dl>
        <Button tag={Link} to="/exchange" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/exchange/${exchangeEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ExchangeDetail;

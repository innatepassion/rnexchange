import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './exchange-operator.reducer';

export const ExchangeOperatorDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const exchangeOperatorEntity = useAppSelector(state => state.exchangeOperator.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="exchangeOperatorDetailsHeading">
          <Translate contentKey="rnexchangeApp.exchangeOperator.detail.title">ExchangeOperator</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{exchangeOperatorEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="rnexchangeApp.exchangeOperator.name">Name</Translate>
            </span>
          </dt>
          <dd>{exchangeOperatorEntity.name}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.exchangeOperator.user">User</Translate>
          </dt>
          <dd>{exchangeOperatorEntity.user ? exchangeOperatorEntity.user.login : ''}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.exchangeOperator.exchange">Exchange</Translate>
          </dt>
          <dd>{exchangeOperatorEntity.exchange ? exchangeOperatorEntity.exchange.code : ''}</dd>
        </dl>
        <Button tag={Link} to="/exchange-operator" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/exchange-operator/${exchangeOperatorEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ExchangeOperatorDetail;

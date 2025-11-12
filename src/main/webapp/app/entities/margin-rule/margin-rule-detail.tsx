import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './margin-rule.reducer';

export const MarginRuleDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const marginRuleEntity = useAppSelector(state => state.marginRule.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="marginRuleDetailsHeading">
          <Translate contentKey="rnexchangeApp.marginRule.detail.title">MarginRule</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{marginRuleEntity.id}</dd>
          <dt>
            <span id="scope">
              <Translate contentKey="rnexchangeApp.marginRule.scope">Scope</Translate>
            </span>
          </dt>
          <dd>{marginRuleEntity.scope}</dd>
          <dt>
            <span id="initialPct">
              <Translate contentKey="rnexchangeApp.marginRule.initialPct">Initial Pct</Translate>
            </span>
          </dt>
          <dd>{marginRuleEntity.initialPct}</dd>
          <dt>
            <span id="maintPct">
              <Translate contentKey="rnexchangeApp.marginRule.maintPct">Maint Pct</Translate>
            </span>
          </dt>
          <dd>{marginRuleEntity.maintPct}</dd>
          <dt>
            <span id="spanJson">
              <Translate contentKey="rnexchangeApp.marginRule.spanJson">Span Json</Translate>
            </span>
          </dt>
          <dd>{marginRuleEntity.spanJson}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.marginRule.exchange">Exchange</Translate>
          </dt>
          <dd>{marginRuleEntity.exchange ? marginRuleEntity.exchange.code : ''}</dd>
        </dl>
        <Button tag={Link} to="/margin-rule" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/margin-rule/${marginRuleEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default MarginRuleDetail;

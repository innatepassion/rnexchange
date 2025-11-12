import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './contract.reducer';

export const ContractDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const contractEntity = useAppSelector(state => state.contract.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="contractDetailsHeading">
          <Translate contentKey="rnexchangeApp.contract.detail.title">Contract</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{contractEntity.id}</dd>
          <dt>
            <span id="instrumentSymbol">
              <Translate contentKey="rnexchangeApp.contract.instrumentSymbol">Instrument Symbol</Translate>
            </span>
          </dt>
          <dd>{contractEntity.instrumentSymbol}</dd>
          <dt>
            <span id="contractType">
              <Translate contentKey="rnexchangeApp.contract.contractType">Contract Type</Translate>
            </span>
          </dt>
          <dd>{contractEntity.contractType}</dd>
          <dt>
            <span id="expiry">
              <Translate contentKey="rnexchangeApp.contract.expiry">Expiry</Translate>
            </span>
          </dt>
          <dd>{contractEntity.expiry ? <TextFormat value={contractEntity.expiry} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="strike">
              <Translate contentKey="rnexchangeApp.contract.strike">Strike</Translate>
            </span>
          </dt>
          <dd>{contractEntity.strike}</dd>
          <dt>
            <span id="optionType">
              <Translate contentKey="rnexchangeApp.contract.optionType">Option Type</Translate>
            </span>
          </dt>
          <dd>{contractEntity.optionType}</dd>
          <dt>
            <span id="segment">
              <Translate contentKey="rnexchangeApp.contract.segment">Segment</Translate>
            </span>
          </dt>
          <dd>{contractEntity.segment}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.contract.instrument">Instrument</Translate>
          </dt>
          <dd>{contractEntity.instrument ? contractEntity.instrument.symbol : ''}</dd>
        </dl>
        <Button tag={Link} to="/contract" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/contract/${contractEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ContractDetail;

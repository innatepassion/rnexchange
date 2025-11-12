import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './lot.reducer';

export const LotDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const lotEntity = useAppSelector(state => state.lot.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="lotDetailsHeading">
          <Translate contentKey="rnexchangeApp.lot.detail.title">Lot</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{lotEntity.id}</dd>
          <dt>
            <span id="openTs">
              <Translate contentKey="rnexchangeApp.lot.openTs">Open Ts</Translate>
            </span>
          </dt>
          <dd>{lotEntity.openTs ? <TextFormat value={lotEntity.openTs} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="openPx">
              <Translate contentKey="rnexchangeApp.lot.openPx">Open Px</Translate>
            </span>
          </dt>
          <dd>{lotEntity.openPx}</dd>
          <dt>
            <span id="qtyOpen">
              <Translate contentKey="rnexchangeApp.lot.qtyOpen">Qty Open</Translate>
            </span>
          </dt>
          <dd>{lotEntity.qtyOpen}</dd>
          <dt>
            <span id="qtyClosed">
              <Translate contentKey="rnexchangeApp.lot.qtyClosed">Qty Closed</Translate>
            </span>
          </dt>
          <dd>{lotEntity.qtyClosed}</dd>
          <dt>
            <span id="method">
              <Translate contentKey="rnexchangeApp.lot.method">Method</Translate>
            </span>
          </dt>
          <dd>{lotEntity.method}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.lot.position">Position</Translate>
          </dt>
          <dd>{lotEntity.position ? lotEntity.position.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/lot" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/lot/${lotEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default LotDetail;

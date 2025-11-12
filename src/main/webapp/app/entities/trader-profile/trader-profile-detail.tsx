import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './trader-profile.reducer';

export const TraderProfileDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const traderProfileEntity = useAppSelector(state => state.traderProfile.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="traderProfileDetailsHeading">
          <Translate contentKey="rnexchangeApp.traderProfile.detail.title">TraderProfile</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{traderProfileEntity.id}</dd>
          <dt>
            <span id="displayName">
              <Translate contentKey="rnexchangeApp.traderProfile.displayName">Display Name</Translate>
            </span>
          </dt>
          <dd>{traderProfileEntity.displayName}</dd>
          <dt>
            <span id="email">
              <Translate contentKey="rnexchangeApp.traderProfile.email">Email</Translate>
            </span>
          </dt>
          <dd>{traderProfileEntity.email}</dd>
          <dt>
            <span id="mobile">
              <Translate contentKey="rnexchangeApp.traderProfile.mobile">Mobile</Translate>
            </span>
          </dt>
          <dd>{traderProfileEntity.mobile}</dd>
          <dt>
            <span id="kycStatus">
              <Translate contentKey="rnexchangeApp.traderProfile.kycStatus">Kyc Status</Translate>
            </span>
          </dt>
          <dd>{traderProfileEntity.kycStatus}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="rnexchangeApp.traderProfile.status">Status</Translate>
            </span>
          </dt>
          <dd>{traderProfileEntity.status}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.traderProfile.user">User</Translate>
          </dt>
          <dd>{traderProfileEntity.user ? traderProfileEntity.user.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/trader-profile" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/trader-profile/${traderProfileEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default TraderProfileDetail;

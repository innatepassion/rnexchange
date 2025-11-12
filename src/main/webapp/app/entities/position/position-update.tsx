import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getTradingAccounts } from 'app/entities/trading-account/trading-account.reducer';
import { getEntities as getInstruments } from 'app/entities/instrument/instrument.reducer';
import { createEntity, getEntity, reset, updateEntity } from './position.reducer';

export const PositionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const tradingAccounts = useAppSelector(state => state.tradingAccount.entities);
  const instruments = useAppSelector(state => state.instrument.entities);
  const positionEntity = useAppSelector(state => state.position.entity);
  const loading = useAppSelector(state => state.position.loading);
  const updating = useAppSelector(state => state.position.updating);
  const updateSuccess = useAppSelector(state => state.position.updateSuccess);

  const handleClose = () => {
    navigate(`/position${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getTradingAccounts({}));
    dispatch(getInstruments({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }
    if (values.qty !== undefined && typeof values.qty !== 'number') {
      values.qty = Number(values.qty);
    }
    if (values.avgCost !== undefined && typeof values.avgCost !== 'number') {
      values.avgCost = Number(values.avgCost);
    }
    if (values.lastPx !== undefined && typeof values.lastPx !== 'number') {
      values.lastPx = Number(values.lastPx);
    }
    if (values.unrealizedPnl !== undefined && typeof values.unrealizedPnl !== 'number') {
      values.unrealizedPnl = Number(values.unrealizedPnl);
    }
    if (values.realizedPnl !== undefined && typeof values.realizedPnl !== 'number') {
      values.realizedPnl = Number(values.realizedPnl);
    }

    const entity = {
      ...positionEntity,
      ...values,
      tradingAccount: tradingAccounts.find(it => it.id.toString() === values.tradingAccount?.toString()),
      instrument: instruments.find(it => it.id.toString() === values.instrument?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...positionEntity,
          tradingAccount: positionEntity?.tradingAccount?.id,
          instrument: positionEntity?.instrument?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.position.home.createOrEditLabel" data-cy="PositionCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.position.home.createOrEditLabel">Create or edit a Position</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="position-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.position.qty')}
                id="position-qty"
                name="qty"
                data-cy="qty"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.position.avgCost')}
                id="position-avgCost"
                name="avgCost"
                data-cy="avgCost"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.position.lastPx')}
                id="position-lastPx"
                name="lastPx"
                data-cy="lastPx"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.position.unrealizedPnl')}
                id="position-unrealizedPnl"
                name="unrealizedPnl"
                data-cy="unrealizedPnl"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.position.realizedPnl')}
                id="position-realizedPnl"
                name="realizedPnl"
                data-cy="realizedPnl"
                type="text"
              />
              <ValidatedField
                id="position-tradingAccount"
                name="tradingAccount"
                data-cy="tradingAccount"
                label={translate('rnexchangeApp.position.tradingAccount')}
                type="select"
              >
                <option value="" key="0" />
                {tradingAccounts
                  ? tradingAccounts.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="position-instrument"
                name="instrument"
                data-cy="instrument"
                label={translate('rnexchangeApp.position.instrument')}
                type="select"
              >
                <option value="" key="0" />
                {instruments
                  ? instruments.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.symbol}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/position" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default PositionUpdate;

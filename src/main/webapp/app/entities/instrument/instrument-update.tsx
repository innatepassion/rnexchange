import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getExchanges } from 'app/entities/exchange/exchange.reducer';
import { AssetClass } from 'app/shared/model/enumerations/asset-class.model';
import { Currency } from 'app/shared/model/enumerations/currency.model';
import { createEntity, getEntity, reset, updateEntity } from './instrument.reducer';

export const InstrumentUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const exchanges = useAppSelector(state => state.exchange.entities);
  const instrumentEntity = useAppSelector(state => state.instrument.entity);
  const loading = useAppSelector(state => state.instrument.loading);
  const updating = useAppSelector(state => state.instrument.updating);
  const updateSuccess = useAppSelector(state => state.instrument.updateSuccess);
  const assetClassValues = Object.keys(AssetClass);
  const currencyValues = Object.keys(Currency);

  const handleClose = () => {
    navigate(`/instrument${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getExchanges({}));
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
    if (values.tickSize !== undefined && typeof values.tickSize !== 'number') {
      values.tickSize = Number(values.tickSize);
    }
    if (values.lotSize !== undefined && typeof values.lotSize !== 'number') {
      values.lotSize = Number(values.lotSize);
    }

    const entity = {
      ...instrumentEntity,
      ...values,
      exchange: exchanges.find(it => it.id.toString() === values.exchange?.toString()),
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
          assetClass: 'EQUITY',
          currency: 'INR',
          ...instrumentEntity,
          exchange: instrumentEntity?.exchange?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.instrument.home.createOrEditLabel" data-cy="InstrumentCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.instrument.home.createOrEditLabel">Create or edit a Instrument</Translate>
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
                  id="instrument-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.instrument.symbol')}
                id="instrument-symbol"
                name="symbol"
                data-cy="symbol"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.instrument.name')}
                id="instrument-name"
                name="name"
                data-cy="name"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.instrument.assetClass')}
                id="instrument-assetClass"
                name="assetClass"
                data-cy="assetClass"
                type="select"
              >
                {assetClassValues.map(assetClass => (
                  <option value={assetClass} key={assetClass}>
                    {translate(`rnexchangeApp.AssetClass.${assetClass}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.instrument.exchangeCode')}
                id="instrument-exchangeCode"
                name="exchangeCode"
                data-cy="exchangeCode"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.instrument.tickSize')}
                id="instrument-tickSize"
                name="tickSize"
                data-cy="tickSize"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.instrument.lotSize')}
                id="instrument-lotSize"
                name="lotSize"
                data-cy="lotSize"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.instrument.currency')}
                id="instrument-currency"
                name="currency"
                data-cy="currency"
                type="select"
              >
                {currencyValues.map(currency => (
                  <option value={currency} key={currency}>
                    {translate(`rnexchangeApp.Currency.${currency}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.instrument.status')}
                id="instrument-status"
                name="status"
                data-cy="status"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="instrument-exchange"
                name="exchange"
                data-cy="exchange"
                label={translate('rnexchangeApp.instrument.exchange')}
                type="select"
              >
                <option value="" key="0" />
                {exchanges
                  ? exchanges.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.code}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/instrument" replace color="info">
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

export default InstrumentUpdate;

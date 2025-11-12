import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getBrokers } from 'app/entities/broker/broker.reducer';
import { getEntities as getTraderProfiles } from 'app/entities/trader-profile/trader-profile.reducer';
import { AccountType } from 'app/shared/model/enumerations/account-type.model';
import { Currency } from 'app/shared/model/enumerations/currency.model';
import { AccountStatus } from 'app/shared/model/enumerations/account-status.model';
import { createEntity, getEntity, reset, updateEntity } from './trading-account.reducer';

export const TradingAccountUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const brokers = useAppSelector(state => state.broker.entities);
  const traderProfiles = useAppSelector(state => state.traderProfile.entities);
  const tradingAccountEntity = useAppSelector(state => state.tradingAccount.entity);
  const loading = useAppSelector(state => state.tradingAccount.loading);
  const updating = useAppSelector(state => state.tradingAccount.updating);
  const updateSuccess = useAppSelector(state => state.tradingAccount.updateSuccess);
  const accountTypeValues = Object.keys(AccountType);
  const currencyValues = Object.keys(Currency);
  const accountStatusValues = Object.keys(AccountStatus);

  const handleClose = () => {
    navigate(`/trading-account${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getBrokers({}));
    dispatch(getTraderProfiles({}));
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
    if (values.balance !== undefined && typeof values.balance !== 'number') {
      values.balance = Number(values.balance);
    }

    const entity = {
      ...tradingAccountEntity,
      ...values,
      broker: brokers.find(it => it.id.toString() === values.broker?.toString()),
      trader: traderProfiles.find(it => it.id.toString() === values.trader?.toString()),
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
          type: 'CASH',
          baseCcy: 'INR',
          status: 'ACTIVE',
          ...tradingAccountEntity,
          broker: tradingAccountEntity?.broker?.id,
          trader: tradingAccountEntity?.trader?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.tradingAccount.home.createOrEditLabel" data-cy="TradingAccountCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.tradingAccount.home.createOrEditLabel">Create or edit a TradingAccount</Translate>
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
                  id="trading-account-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.tradingAccount.type')}
                id="trading-account-type"
                name="type"
                data-cy="type"
                type="select"
              >
                {accountTypeValues.map(accountType => (
                  <option value={accountType} key={accountType}>
                    {translate(`rnexchangeApp.AccountType.${accountType}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.tradingAccount.baseCcy')}
                id="trading-account-baseCcy"
                name="baseCcy"
                data-cy="baseCcy"
                type="select"
              >
                {currencyValues.map(currency => (
                  <option value={currency} key={currency}>
                    {translate(`rnexchangeApp.Currency.${currency}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.tradingAccount.balance')}
                id="trading-account-balance"
                name="balance"
                data-cy="balance"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.tradingAccount.status')}
                id="trading-account-status"
                name="status"
                data-cy="status"
                type="select"
              >
                {accountStatusValues.map(accountStatus => (
                  <option value={accountStatus} key={accountStatus}>
                    {translate(`rnexchangeApp.AccountStatus.${accountStatus}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="trading-account-broker"
                name="broker"
                data-cy="broker"
                label={translate('rnexchangeApp.tradingAccount.broker')}
                type="select"
              >
                <option value="" key="0" />
                {brokers
                  ? brokers.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.code}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="trading-account-trader"
                name="trader"
                data-cy="trader"
                label={translate('rnexchangeApp.tradingAccount.trader')}
                type="select"
              >
                <option value="" key="0" />
                {traderProfiles
                  ? traderProfiles.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.displayName}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/trading-account" replace color="info">
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

export default TradingAccountUpdate;

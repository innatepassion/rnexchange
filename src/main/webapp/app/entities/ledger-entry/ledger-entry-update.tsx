import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getTradingAccounts } from 'app/entities/trading-account/trading-account.reducer';
import { Currency } from 'app/shared/model/enumerations/currency.model';
import { createEntity, getEntity, reset, updateEntity } from './ledger-entry.reducer';

export const LedgerEntryUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const tradingAccounts = useAppSelector(state => state.tradingAccount.entities);
  const ledgerEntryEntity = useAppSelector(state => state.ledgerEntry.entity);
  const loading = useAppSelector(state => state.ledgerEntry.loading);
  const updating = useAppSelector(state => state.ledgerEntry.updating);
  const updateSuccess = useAppSelector(state => state.ledgerEntry.updateSuccess);
  const currencyValues = Object.keys(Currency);

  const handleClose = () => {
    navigate(`/ledger-entry${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getTradingAccounts({}));
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
    values.ts = convertDateTimeToServer(values.ts);
    if (values.amount !== undefined && typeof values.amount !== 'number') {
      values.amount = Number(values.amount);
    }
    if (values.balanceAfter !== undefined && typeof values.balanceAfter !== 'number') {
      values.balanceAfter = Number(values.balanceAfter);
    }

    const entity = {
      ...ledgerEntryEntity,
      ...values,
      tradingAccount: tradingAccounts.find(it => it.id.toString() === values.tradingAccount?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          ts: displayDefaultDateTime(),
        }
      : {
          ccy: 'INR',
          ...ledgerEntryEntity,
          ts: convertDateTimeFromServer(ledgerEntryEntity.ts),
          tradingAccount: ledgerEntryEntity?.tradingAccount?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.ledgerEntry.home.createOrEditLabel" data-cy="LedgerEntryCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.ledgerEntry.home.createOrEditLabel">Create or edit a LedgerEntry</Translate>
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
                  id="ledger-entry-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.ledgerEntry.ts')}
                id="ledger-entry-ts"
                name="ts"
                data-cy="ts"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.ledgerEntry.type')}
                id="ledger-entry-type"
                name="type"
                data-cy="type"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.ledgerEntry.amount')}
                id="ledger-entry-amount"
                name="amount"
                data-cy="amount"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.ledgerEntry.ccy')}
                id="ledger-entry-ccy"
                name="ccy"
                data-cy="ccy"
                type="select"
              >
                {currencyValues.map(currency => (
                  <option value={currency} key={currency}>
                    {translate(`rnexchangeApp.Currency.${currency}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.ledgerEntry.balanceAfter')}
                id="ledger-entry-balanceAfter"
                name="balanceAfter"
                data-cy="balanceAfter"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.ledgerEntry.reference')}
                id="ledger-entry-reference"
                name="reference"
                data-cy="reference"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.ledgerEntry.remarks')}
                id="ledger-entry-remarks"
                name="remarks"
                data-cy="remarks"
                type="text"
              />
              <ValidatedField
                id="ledger-entry-tradingAccount"
                name="tradingAccount"
                data-cy="tradingAccount"
                label={translate('rnexchangeApp.ledgerEntry.tradingAccount')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/ledger-entry" replace color="info">
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

export default LedgerEntryUpdate;

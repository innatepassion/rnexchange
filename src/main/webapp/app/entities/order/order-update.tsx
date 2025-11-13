import React, { useEffect, useMemo } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getTradingAccounts } from 'app/entities/trading-account/trading-account.reducer';
import { getEntities as getInstruments } from 'app/entities/instrument/instrument.reducer';
import { OrderSide } from 'app/shared/model/enumerations/order-side.model';
import { OrderType } from 'app/shared/model/enumerations/order-type.model';
import { Tif } from 'app/shared/model/enumerations/tif.model';
import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';
import { createEntity, getEntity, reset, updateEntity } from './order.reducer';

export const OrderUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const tradingAccounts = useAppSelector(state => state.tradingAccount.entities);
  const instruments = useAppSelector(state => state.instrument.entities);
  const orderEntity = useAppSelector(state => state.order.entity);
  const loading = useAppSelector(state => state.order.loading);
  const updating = useAppSelector(state => state.order.updating);
  const updateSuccess = useAppSelector(state => state.order.updateSuccess);
  const currentUserLogin = useAppSelector(state => state.authentication.account?.login);

  const seededTradingAccount = useMemo(() => {
    if (!tradingAccounts || tradingAccounts.length === 0) {
      return undefined;
    }
    const matchByLogin = tradingAccounts.find(account => account.trader?.user?.login === currentUserLogin);
    if (matchByLogin) {
      return matchByLogin;
    }
    const matchDemoBroker = tradingAccounts.find(account => account.broker?.code === 'RN_DEMO');
    return matchDemoBroker ?? tradingAccounts[0];
  }, [tradingAccounts, currentUserLogin]);

  const seededInstrument = useMemo(() => {
    if (!instruments || instruments.length === 0) {
      return undefined;
    }
    const matchReliance = instruments.find(instrument => instrument.symbol === 'RELIANCE' && instrument.exchangeCode === 'NSE');
    return matchReliance ?? instruments[0];
  }, [instruments]);

  const seededInstrumentPrice = useMemo(() => {
    if (!seededInstrument) {
      return undefined;
    }
    const priceDictionary: Record<string, number> = {
      'RELIANCE@NSE': 2200,
      'HDFCBANK@NSE': 1500,
      'INFY@NSE': 1400,
      'TCS@NSE': 3400,
    };
    const key = `${seededInstrument.symbol}@${seededInstrument.exchangeCode}`;
    if (priceDictionary[key]) {
      return priceDictionary[key];
    }
    const tickSize = seededInstrument.tickSize ?? 1;
    return Math.max(100, tickSize * 100);
  }, [seededInstrument]);
  const orderSideValues = Object.keys(OrderSide);
  const orderTypeValues = Object.keys(OrderType);
  const tifValues = Object.keys(Tif);
  const orderStatusValues = Object.keys(OrderStatus);

  const handleClose = () => {
    navigate(`/order${location.search}`);
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
    if (values.limitPx !== undefined && typeof values.limitPx !== 'number') {
      values.limitPx = Number(values.limitPx);
    }
    if (values.stopPx !== undefined && typeof values.stopPx !== 'number') {
      values.stopPx = Number(values.stopPx);
    }
    values.createdAt = convertDateTimeToServer(values.createdAt);
    values.updatedAt = convertDateTimeToServer(values.updatedAt);

    if (!values.tradingAccount && seededTradingAccount?.id) {
      values.tradingAccount = seededTradingAccount.id;
    }

    const selectedInstrument = instruments.find(instrument => instrument.id?.toString() === values.instrument?.toString());
    if (selectedInstrument) {
      values.venue = selectedInstrument.exchangeCode;
      if (values.limitPx === undefined || Number.isNaN(values.limitPx)) {
        const fallbackPriceDictionary: Record<string, number> = {
          'RELIANCE@NSE': 2200,
          'HDFCBANK@NSE': 1500,
          'INFY@NSE': 1400,
          'TCS@NSE': 3400,
        };
        const instrumentKey = `${selectedInstrument.symbol}@${selectedInstrument.exchangeCode}`;
        const fallbackPrice = fallbackPriceDictionary[instrumentKey] ?? Math.max(100, (selectedInstrument.tickSize ?? 1) * 100);
        values.limitPx = Number(fallbackPrice.toFixed(2));
      }
    }

    values.status = 'NEW';

    const entity = {
      ...orderEntity,
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

  const seededPriceFormatted = useMemo(
    () => (seededInstrumentPrice !== undefined ? seededInstrumentPrice.toFixed(2) : undefined),
    [seededInstrumentPrice],
  );

  const defaultValues = () => {
    if (isNew) {
      const lotSize = seededInstrument?.lotSize ?? 1;
      return {
        side: 'BUY',
        type: 'MARKET',
        tif: 'DAY',
        status: 'NEW',
        qty: lotSize.toString(),
        limitPx: seededPriceFormatted,
        venue: seededInstrument?.exchangeCode ?? 'NSE',
        tradingAccount: seededTradingAccount?.id?.toString(),
        instrument: seededInstrument?.id?.toString(),
        createdAt: displayDefaultDateTime(),
        updatedAt: displayDefaultDateTime(),
      };
    }
    return {
      side: 'BUY',
      type: 'MARKET',
      tif: 'DAY',
      status: 'NEW',
      ...orderEntity,
      createdAt: convertDateTimeFromServer(orderEntity.createdAt),
      updatedAt: convertDateTimeFromServer(orderEntity.updatedAt),
      tradingAccount: orderEntity?.tradingAccount?.id?.toString(),
      instrument: orderEntity?.instrument?.id?.toString(),
    };
  };

  const computedDefaults = useMemo(
    () => defaultValues(),
    [isNew, orderEntity, seededTradingAccount, seededInstrument, seededPriceFormatted],
  );

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.order.home.createOrEditLabel" data-cy="OrderCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.order.home.createOrEditLabel">Create or edit a Order</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm
              key={isNew ? `order-new-${computedDefaults.tradingAccount ?? 'pending'}` : `${orderEntity.id ?? 'existing'}`}
              defaultValues={computedDefaults}
              onSubmit={saveEntity}
            >
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="order-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField label={translate('rnexchangeApp.order.side')} id="order-side" name="side" data-cy="side" type="select">
                {orderSideValues.map(orderSide => (
                  <option value={orderSide} key={orderSide}>
                    {translate(`rnexchangeApp.OrderSide.${orderSide}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField label={translate('rnexchangeApp.order.type')} id="order-type" name="type" data-cy="type" type="select">
                {orderTypeValues.map(orderType => (
                  <option value={orderType} key={orderType}>
                    {translate(`rnexchangeApp.OrderType.${orderType}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.order.qty')}
                id="order-qty"
                name="qty"
                data-cy="qty"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.order.limitPx')}
                id="order-limitPx"
                name="limitPx"
                data-cy="limitPx"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.order.stopPx')}
                id="order-stopPx"
                name="stopPx"
                data-cy="stopPx"
                type="text"
              />
              <ValidatedField label={translate('rnexchangeApp.order.tif')} id="order-tif" name="tif" data-cy="tif" type="select">
                {tifValues.map(tif => (
                  <option value={tif} key={tif}>
                    {translate(`rnexchangeApp.Tif.${tif}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.order.status')}
                id="order-status"
                name="status"
                data-cy="status"
                type="select"
                disabled
              >
                {orderStatusValues.map(orderStatus => (
                  <option value={orderStatus} key={orderStatus}>
                    {translate(`rnexchangeApp.OrderStatus.${orderStatus}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.order.venue')}
                id="order-venue"
                name="venue"
                data-cy="venue"
                type="text"
                readOnly
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.order.createdAt')}
                id="order-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label={translate('rnexchangeApp.order.updatedAt')}
                id="order-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                id="order-tradingAccount"
                name="tradingAccount"
                data-cy="tradingAccount"
                label={translate('rnexchangeApp.order.tradingAccount')}
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
                id="order-instrument"
                name="instrument"
                data-cy="instrument"
                label={translate('rnexchangeApp.order.instrument')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/order" replace color="info">
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

export default OrderUpdate;

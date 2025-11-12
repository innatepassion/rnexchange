import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getExchanges } from 'app/entities/exchange/exchange.reducer';
import { createEntity, getEntity, reset, updateEntity } from './market-holiday.reducer';

export const MarketHolidayUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const exchanges = useAppSelector(state => state.exchange.entities);
  const marketHolidayEntity = useAppSelector(state => state.marketHoliday.entity);
  const loading = useAppSelector(state => state.marketHoliday.loading);
  const updating = useAppSelector(state => state.marketHoliday.updating);
  const updateSuccess = useAppSelector(state => state.marketHoliday.updateSuccess);

  const handleClose = () => {
    navigate(`/market-holiday${location.search}`);
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

    const entity = {
      ...marketHolidayEntity,
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
          ...marketHolidayEntity,
          exchange: marketHolidayEntity?.exchange?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.marketHoliday.home.createOrEditLabel" data-cy="MarketHolidayCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.marketHoliday.home.createOrEditLabel">Create or edit a MarketHoliday</Translate>
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
                  id="market-holiday-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.marketHoliday.tradeDate')}
                id="market-holiday-tradeDate"
                name="tradeDate"
                data-cy="tradeDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.marketHoliday.reason')}
                id="market-holiday-reason"
                name="reason"
                data-cy="reason"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.marketHoliday.isHoliday')}
                id="market-holiday-isHoliday"
                name="isHoliday"
                data-cy="isHoliday"
                check
                type="checkbox"
              />
              <ValidatedField
                id="market-holiday-exchange"
                name="exchange"
                data-cy="exchange"
                label={translate('rnexchangeApp.marketHoliday.exchange')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/market-holiday" replace color="info">
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

export default MarketHolidayUpdate;

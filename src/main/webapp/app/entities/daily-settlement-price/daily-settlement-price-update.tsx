import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getInstruments } from 'app/entities/instrument/instrument.reducer';
import { createEntity, getEntity, reset, updateEntity } from './daily-settlement-price.reducer';

export const DailySettlementPriceUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const instruments = useAppSelector(state => state.instrument.entities);
  const dailySettlementPriceEntity = useAppSelector(state => state.dailySettlementPrice.entity);
  const loading = useAppSelector(state => state.dailySettlementPrice.loading);
  const updating = useAppSelector(state => state.dailySettlementPrice.updating);
  const updateSuccess = useAppSelector(state => state.dailySettlementPrice.updateSuccess);

  const handleClose = () => {
    navigate(`/daily-settlement-price${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

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
    if (values.settlePrice !== undefined && typeof values.settlePrice !== 'number') {
      values.settlePrice = Number(values.settlePrice);
    }

    const entity = {
      ...dailySettlementPriceEntity,
      ...values,
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
          ...dailySettlementPriceEntity,
          instrument: dailySettlementPriceEntity?.instrument?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.dailySettlementPrice.home.createOrEditLabel" data-cy="DailySettlementPriceCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.dailySettlementPrice.home.createOrEditLabel">
              Create or edit a DailySettlementPrice
            </Translate>
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
                  id="daily-settlement-price-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.dailySettlementPrice.refDate')}
                id="daily-settlement-price-refDate"
                name="refDate"
                data-cy="refDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.dailySettlementPrice.instrumentSymbol')}
                id="daily-settlement-price-instrumentSymbol"
                name="instrumentSymbol"
                data-cy="instrumentSymbol"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.dailySettlementPrice.settlePrice')}
                id="daily-settlement-price-settlePrice"
                name="settlePrice"
                data-cy="settlePrice"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                id="daily-settlement-price-instrument"
                name="instrument"
                data-cy="instrument"
                label={translate('rnexchangeApp.dailySettlementPrice.instrument')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/daily-settlement-price" replace color="info">
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

export default DailySettlementPriceUpdate;

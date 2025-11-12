import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getInstruments } from 'app/entities/instrument/instrument.reducer';
import { CorporateActionType } from 'app/shared/model/enumerations/corporate-action-type.model';
import { createEntity, getEntity, reset, updateEntity } from './corporate-action.reducer';

export const CorporateActionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const instruments = useAppSelector(state => state.instrument.entities);
  const corporateActionEntity = useAppSelector(state => state.corporateAction.entity);
  const loading = useAppSelector(state => state.corporateAction.loading);
  const updating = useAppSelector(state => state.corporateAction.updating);
  const updateSuccess = useAppSelector(state => state.corporateAction.updateSuccess);
  const corporateActionTypeValues = Object.keys(CorporateActionType);

  const handleClose = () => {
    navigate(`/corporate-action${location.search}`);
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
    if (values.ratio !== undefined && typeof values.ratio !== 'number') {
      values.ratio = Number(values.ratio);
    }
    if (values.cashAmount !== undefined && typeof values.cashAmount !== 'number') {
      values.cashAmount = Number(values.cashAmount);
    }

    const entity = {
      ...corporateActionEntity,
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
          type: 'SPLIT',
          ...corporateActionEntity,
          instrument: corporateActionEntity?.instrument?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.corporateAction.home.createOrEditLabel" data-cy="CorporateActionCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.corporateAction.home.createOrEditLabel">Create or edit a CorporateAction</Translate>
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
                  id="corporate-action-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.corporateAction.type')}
                id="corporate-action-type"
                name="type"
                data-cy="type"
                type="select"
              >
                {corporateActionTypeValues.map(corporateActionType => (
                  <option value={corporateActionType} key={corporateActionType}>
                    {translate(`rnexchangeApp.CorporateActionType.${corporateActionType}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.corporateAction.instrumentSymbol')}
                id="corporate-action-instrumentSymbol"
                name="instrumentSymbol"
                data-cy="instrumentSymbol"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.corporateAction.exDate')}
                id="corporate-action-exDate"
                name="exDate"
                data-cy="exDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.corporateAction.payDate')}
                id="corporate-action-payDate"
                name="payDate"
                data-cy="payDate"
                type="date"
              />
              <ValidatedField
                label={translate('rnexchangeApp.corporateAction.ratio')}
                id="corporate-action-ratio"
                name="ratio"
                data-cy="ratio"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.corporateAction.cashAmount')}
                id="corporate-action-cashAmount"
                name="cashAmount"
                data-cy="cashAmount"
                type="text"
              />
              <ValidatedField
                id="corporate-action-instrument"
                name="instrument"
                data-cy="instrument"
                label={translate('rnexchangeApp.corporateAction.instrument')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/corporate-action" replace color="info">
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

export default CorporateActionUpdate;

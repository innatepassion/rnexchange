import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getInstruments } from 'app/entities/instrument/instrument.reducer';
import { ContractType } from 'app/shared/model/enumerations/contract-type.model';
import { OptionType } from 'app/shared/model/enumerations/option-type.model';
import { createEntity, getEntity, reset, updateEntity } from './contract.reducer';

export const ContractUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const instruments = useAppSelector(state => state.instrument.entities);
  const contractEntity = useAppSelector(state => state.contract.entity);
  const loading = useAppSelector(state => state.contract.loading);
  const updating = useAppSelector(state => state.contract.updating);
  const updateSuccess = useAppSelector(state => state.contract.updateSuccess);
  const contractTypeValues = Object.keys(ContractType);
  const optionTypeValues = Object.keys(OptionType);

  const handleClose = () => {
    navigate(`/contract${location.search}`);
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
    if (values.strike !== undefined && typeof values.strike !== 'number') {
      values.strike = Number(values.strike);
    }

    const entity = {
      ...contractEntity,
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
          contractType: 'FUTURE',
          optionType: 'CE',
          ...contractEntity,
          instrument: contractEntity?.instrument?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.contract.home.createOrEditLabel" data-cy="ContractCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.contract.home.createOrEditLabel">Create or edit a Contract</Translate>
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
                  id="contract-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.contract.instrumentSymbol')}
                id="contract-instrumentSymbol"
                name="instrumentSymbol"
                data-cy="instrumentSymbol"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.contract.contractType')}
                id="contract-contractType"
                name="contractType"
                data-cy="contractType"
                type="select"
              >
                {contractTypeValues.map(contractType => (
                  <option value={contractType} key={contractType}>
                    {translate(`rnexchangeApp.ContractType.${contractType}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.contract.expiry')}
                id="contract-expiry"
                name="expiry"
                data-cy="expiry"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.contract.strike')}
                id="contract-strike"
                name="strike"
                data-cy="strike"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.contract.optionType')}
                id="contract-optionType"
                name="optionType"
                data-cy="optionType"
                type="select"
              >
                {optionTypeValues.map(optionType => (
                  <option value={optionType} key={optionType}>
                    {translate(`rnexchangeApp.OptionType.${optionType}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.contract.segment')}
                id="contract-segment"
                name="segment"
                data-cy="segment"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="contract-instrument"
                name="instrument"
                data-cy="instrument"
                label={translate('rnexchangeApp.contract.instrument')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/contract" replace color="info">
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

export default ContractUpdate;

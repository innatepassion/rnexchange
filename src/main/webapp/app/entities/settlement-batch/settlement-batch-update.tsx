import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getExchanges } from 'app/entities/exchange/exchange.reducer';
import { SettlementKind } from 'app/shared/model/enumerations/settlement-kind.model';
import { SettlementStatus } from 'app/shared/model/enumerations/settlement-status.model';
import { createEntity, getEntity, reset, updateEntity } from './settlement-batch.reducer';

export const SettlementBatchUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const exchanges = useAppSelector(state => state.exchange.entities);
  const settlementBatchEntity = useAppSelector(state => state.settlementBatch.entity);
  const loading = useAppSelector(state => state.settlementBatch.loading);
  const updating = useAppSelector(state => state.settlementBatch.updating);
  const updateSuccess = useAppSelector(state => state.settlementBatch.updateSuccess);
  const settlementKindValues = Object.keys(SettlementKind);
  const settlementStatusValues = Object.keys(SettlementStatus);

  const handleClose = () => {
    navigate(`/settlement-batch${location.search}`);
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
      ...settlementBatchEntity,
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
          kind: 'EOD',
          status: 'CREATED',
          ...settlementBatchEntity,
          exchange: settlementBatchEntity?.exchange?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.settlementBatch.home.createOrEditLabel" data-cy="SettlementBatchCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.settlementBatch.home.createOrEditLabel">Create or edit a SettlementBatch</Translate>
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
                  id="settlement-batch-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.settlementBatch.refDate')}
                id="settlement-batch-refDate"
                name="refDate"
                data-cy="refDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.settlementBatch.kind')}
                id="settlement-batch-kind"
                name="kind"
                data-cy="kind"
                type="select"
              >
                {settlementKindValues.map(settlementKind => (
                  <option value={settlementKind} key={settlementKind}>
                    {translate(`rnexchangeApp.SettlementKind.${settlementKind}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.settlementBatch.status')}
                id="settlement-batch-status"
                name="status"
                data-cy="status"
                type="select"
              >
                {settlementStatusValues.map(settlementStatus => (
                  <option value={settlementStatus} key={settlementStatus}>
                    {translate(`rnexchangeApp.SettlementStatus.${settlementStatus}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.settlementBatch.remarks')}
                id="settlement-batch-remarks"
                name="remarks"
                data-cy="remarks"
                type="text"
              />
              <ValidatedField
                id="settlement-batch-exchange"
                name="exchange"
                data-cy="exchange"
                label={translate('rnexchangeApp.settlementBatch.exchange')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/settlement-batch" replace color="info">
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

export default SettlementBatchUpdate;

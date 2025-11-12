import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getOrders } from 'app/entities/order/order.reducer';
import { createEntity, getEntity, reset, updateEntity } from './execution.reducer';

export const ExecutionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const orders = useAppSelector(state => state.order.entities);
  const executionEntity = useAppSelector(state => state.execution.entity);
  const loading = useAppSelector(state => state.execution.loading);
  const updating = useAppSelector(state => state.execution.updating);
  const updateSuccess = useAppSelector(state => state.execution.updateSuccess);

  const handleClose = () => {
    navigate(`/execution${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getOrders({}));
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
    values.execTs = convertDateTimeToServer(values.execTs);
    if (values.px !== undefined && typeof values.px !== 'number') {
      values.px = Number(values.px);
    }
    if (values.qty !== undefined && typeof values.qty !== 'number') {
      values.qty = Number(values.qty);
    }
    if (values.fee !== undefined && typeof values.fee !== 'number') {
      values.fee = Number(values.fee);
    }

    const entity = {
      ...executionEntity,
      ...values,
      order: orders.find(it => it.id.toString() === values.order?.toString()),
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
          execTs: displayDefaultDateTime(),
        }
      : {
          ...executionEntity,
          execTs: convertDateTimeFromServer(executionEntity.execTs),
          order: executionEntity?.order?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.execution.home.createOrEditLabel" data-cy="ExecutionCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.execution.home.createOrEditLabel">Create or edit a Execution</Translate>
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
                  id="execution-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.execution.execTs')}
                id="execution-execTs"
                name="execTs"
                data-cy="execTs"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.execution.px')}
                id="execution-px"
                name="px"
                data-cy="px"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.execution.qty')}
                id="execution-qty"
                name="qty"
                data-cy="qty"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.execution.liquidity')}
                id="execution-liquidity"
                name="liquidity"
                data-cy="liquidity"
                type="text"
              />
              <ValidatedField label={translate('rnexchangeApp.execution.fee')} id="execution-fee" name="fee" data-cy="fee" type="text" />
              <ValidatedField
                id="execution-order"
                name="order"
                data-cy="order"
                label={translate('rnexchangeApp.execution.order')}
                type="select"
              >
                <option value="" key="0" />
                {orders
                  ? orders.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/execution" replace color="info">
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

export default ExecutionUpdate;

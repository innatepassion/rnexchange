import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getPositions } from 'app/entities/position/position.reducer';
import { createEntity, getEntity, reset, updateEntity } from './lot.reducer';

export const LotUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const positions = useAppSelector(state => state.position.entities);
  const lotEntity = useAppSelector(state => state.lot.entity);
  const loading = useAppSelector(state => state.lot.loading);
  const updating = useAppSelector(state => state.lot.updating);
  const updateSuccess = useAppSelector(state => state.lot.updateSuccess);

  const handleClose = () => {
    navigate(`/lot${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPositions({}));
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
    values.openTs = convertDateTimeToServer(values.openTs);
    if (values.openPx !== undefined && typeof values.openPx !== 'number') {
      values.openPx = Number(values.openPx);
    }
    if (values.qtyOpen !== undefined && typeof values.qtyOpen !== 'number') {
      values.qtyOpen = Number(values.qtyOpen);
    }
    if (values.qtyClosed !== undefined && typeof values.qtyClosed !== 'number') {
      values.qtyClosed = Number(values.qtyClosed);
    }

    const entity = {
      ...lotEntity,
      ...values,
      position: positions.find(it => it.id.toString() === values.position?.toString()),
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
          openTs: displayDefaultDateTime(),
        }
      : {
          ...lotEntity,
          openTs: convertDateTimeFromServer(lotEntity.openTs),
          position: lotEntity?.position?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.lot.home.createOrEditLabel" data-cy="LotCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.lot.home.createOrEditLabel">Create or edit a Lot</Translate>
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
                  id="lot-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.lot.openTs')}
                id="lot-openTs"
                name="openTs"
                data-cy="openTs"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.lot.openPx')}
                id="lot-openPx"
                name="openPx"
                data-cy="openPx"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.lot.qtyOpen')}
                id="lot-qtyOpen"
                name="qtyOpen"
                data-cy="qtyOpen"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.lot.qtyClosed')}
                id="lot-qtyClosed"
                name="qtyClosed"
                data-cy="qtyClosed"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField label={translate('rnexchangeApp.lot.method')} id="lot-method" name="method" data-cy="method" type="text" />
              <ValidatedField
                id="lot-position"
                name="position"
                data-cy="position"
                label={translate('rnexchangeApp.lot.position')}
                type="select"
              >
                <option value="" key="0" />
                {positions
                  ? positions.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/lot" replace color="info">
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

export default LotUpdate;

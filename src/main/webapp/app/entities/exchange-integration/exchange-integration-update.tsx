import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getExchanges } from 'app/entities/exchange/exchange.reducer';
import { IntegrationStatus } from 'app/shared/model/enumerations/integration-status.model';
import { createEntity, getEntity, reset, updateEntity } from './exchange-integration.reducer';

export const ExchangeIntegrationUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const exchanges = useAppSelector(state => state.exchange.entities);
  const exchangeIntegrationEntity = useAppSelector(state => state.exchangeIntegration.entity);
  const loading = useAppSelector(state => state.exchangeIntegration.loading);
  const updating = useAppSelector(state => state.exchangeIntegration.updating);
  const updateSuccess = useAppSelector(state => state.exchangeIntegration.updateSuccess);
  const integrationStatusValues = Object.keys(IntegrationStatus);

  const handleClose = () => {
    navigate(`/exchange-integration${location.search}`);
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
    values.lastHeartbeat = convertDateTimeToServer(values.lastHeartbeat);

    const entity = {
      ...exchangeIntegrationEntity,
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
      ? {
          lastHeartbeat: displayDefaultDateTime(),
        }
      : {
          status: 'DISABLED',
          ...exchangeIntegrationEntity,
          lastHeartbeat: convertDateTimeFromServer(exchangeIntegrationEntity.lastHeartbeat),
          exchange: exchangeIntegrationEntity?.exchange?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.exchangeIntegration.home.createOrEditLabel" data-cy="ExchangeIntegrationCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.exchangeIntegration.home.createOrEditLabel">
              Create or edit a ExchangeIntegration
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
                  id="exchange-integration-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.exchangeIntegration.provider')}
                id="exchange-integration-provider"
                name="provider"
                data-cy="provider"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.exchangeIntegration.apiKey')}
                id="exchange-integration-apiKey"
                name="apiKey"
                data-cy="apiKey"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.exchangeIntegration.apiSecret')}
                id="exchange-integration-apiSecret"
                name="apiSecret"
                data-cy="apiSecret"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.exchangeIntegration.status')}
                id="exchange-integration-status"
                name="status"
                data-cy="status"
                type="select"
              >
                {integrationStatusValues.map(integrationStatus => (
                  <option value={integrationStatus} key={integrationStatus}>
                    {translate(`rnexchangeApp.IntegrationStatus.${integrationStatus}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.exchangeIntegration.lastHeartbeat')}
                id="exchange-integration-lastHeartbeat"
                name="lastHeartbeat"
                data-cy="lastHeartbeat"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                id="exchange-integration-exchange"
                name="exchange"
                data-cy="exchange"
                label={translate('rnexchangeApp.exchangeIntegration.exchange')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/exchange-integration" replace color="info">
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

export default ExchangeIntegrationUpdate;

import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getTradingAccounts } from 'app/entities/trading-account/trading-account.reducer';
import { getEntities as getTraderProfiles } from 'app/entities/trader-profile/trader-profile.reducer';
import { AlertType } from 'app/shared/model/enumerations/alert-type.model';
import { createEntity, getEntity, reset, updateEntity } from './risk-alert.reducer';

export const RiskAlertUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const tradingAccounts = useAppSelector(state => state.tradingAccount.entities);
  const traderProfiles = useAppSelector(state => state.traderProfile.entities);
  const riskAlertEntity = useAppSelector(state => state.riskAlert.entity);
  const loading = useAppSelector(state => state.riskAlert.loading);
  const updating = useAppSelector(state => state.riskAlert.updating);
  const updateSuccess = useAppSelector(state => state.riskAlert.updateSuccess);
  const alertTypeValues = Object.keys(AlertType);

  const handleClose = () => {
    navigate(`/risk-alert${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getTradingAccounts({}));
    dispatch(getTraderProfiles({}));
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
    values.createdAt = convertDateTimeToServer(values.createdAt);

    const entity = {
      ...riskAlertEntity,
      ...values,
      tradingAccount: tradingAccounts.find(it => it.id.toString() === values.tradingAccount?.toString()),
      trader: traderProfiles.find(it => it.id.toString() === values.trader?.toString()),
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
          createdAt: displayDefaultDateTime(),
        }
      : {
          alertType: 'MARGIN_BREACH',
          ...riskAlertEntity,
          createdAt: convertDateTimeFromServer(riskAlertEntity.createdAt),
          tradingAccount: riskAlertEntity?.tradingAccount?.id,
          trader: riskAlertEntity?.trader?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.riskAlert.home.createOrEditLabel" data-cy="RiskAlertCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.riskAlert.home.createOrEditLabel">Create or edit a RiskAlert</Translate>
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
                  id="risk-alert-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.riskAlert.alertType')}
                id="risk-alert-alertType"
                name="alertType"
                data-cy="alertType"
                type="select"
              >
                {alertTypeValues.map(alertType => (
                  <option value={alertType} key={alertType}>
                    {translate(`rnexchangeApp.AlertType.${alertType}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.riskAlert.description')}
                id="risk-alert-description"
                name="description"
                data-cy="description"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.riskAlert.createdAt')}
                id="risk-alert-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                id="risk-alert-tradingAccount"
                name="tradingAccount"
                data-cy="tradingAccount"
                label={translate('rnexchangeApp.riskAlert.tradingAccount')}
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
                id="risk-alert-trader"
                name="trader"
                data-cy="trader"
                label={translate('rnexchangeApp.riskAlert.trader')}
                type="select"
              >
                <option value="" key="0" />
                {traderProfiles
                  ? traderProfiles.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.displayName}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/risk-alert" replace color="info">
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

export default RiskAlertUpdate;

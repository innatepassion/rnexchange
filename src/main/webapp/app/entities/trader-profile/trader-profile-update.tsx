import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { KycStatus } from 'app/shared/model/enumerations/kyc-status.model';
import { AccountStatus } from 'app/shared/model/enumerations/account-status.model';
import { createEntity, getEntity, reset, updateEntity } from './trader-profile.reducer';

export const TraderProfileUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const users = useAppSelector(state => state.userManagement.users);
  const traderProfileEntity = useAppSelector(state => state.traderProfile.entity);
  const loading = useAppSelector(state => state.traderProfile.loading);
  const updating = useAppSelector(state => state.traderProfile.updating);
  const updateSuccess = useAppSelector(state => state.traderProfile.updateSuccess);
  const kycStatusValues = Object.keys(KycStatus);
  const accountStatusValues = Object.keys(AccountStatus);

  const handleClose = () => {
    navigate(`/trader-profile${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUsers({}));
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
      ...traderProfileEntity,
      ...values,
      user: users.find(it => it.id.toString() === values.user?.toString()),
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
          kycStatus: 'PENDING',
          status: 'ACTIVE',
          ...traderProfileEntity,
          user: traderProfileEntity?.user?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.traderProfile.home.createOrEditLabel" data-cy="TraderProfileCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.traderProfile.home.createOrEditLabel">Create or edit a TraderProfile</Translate>
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
                  id="trader-profile-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.traderProfile.displayName')}
                id="trader-profile-displayName"
                name="displayName"
                data-cy="displayName"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.traderProfile.email')}
                id="trader-profile-email"
                name="email"
                data-cy="email"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.traderProfile.mobile')}
                id="trader-profile-mobile"
                name="mobile"
                data-cy="mobile"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.traderProfile.kycStatus')}
                id="trader-profile-kycStatus"
                name="kycStatus"
                data-cy="kycStatus"
                type="select"
              >
                {kycStatusValues.map(kycStatus => (
                  <option value={kycStatus} key={kycStatus}>
                    {translate(`rnexchangeApp.KycStatus.${kycStatus}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('rnexchangeApp.traderProfile.status')}
                id="trader-profile-status"
                name="status"
                data-cy="status"
                type="select"
              >
                {accountStatusValues.map(accountStatus => (
                  <option value={accountStatus} key={accountStatus}>
                    {translate(`rnexchangeApp.AccountStatus.${accountStatus}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="trader-profile-user"
                name="user"
                data-cy="user"
                label={translate('rnexchangeApp.traderProfile.user')}
                type="select"
              >
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/trader-profile" replace color="info">
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

export default TraderProfileUpdate;

import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getExchanges } from 'app/entities/exchange/exchange.reducer';
import { createEntity, getEntity, reset, updateEntity } from './margin-rule.reducer';

export const MarginRuleUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const exchanges = useAppSelector(state => state.exchange.entities);
  const marginRuleEntity = useAppSelector(state => state.marginRule.entity);
  const loading = useAppSelector(state => state.marginRule.loading);
  const updating = useAppSelector(state => state.marginRule.updating);
  const updateSuccess = useAppSelector(state => state.marginRule.updateSuccess);

  const handleClose = () => {
    navigate(`/margin-rule${location.search}`);
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
    if (values.initialPct !== undefined && typeof values.initialPct !== 'number') {
      values.initialPct = Number(values.initialPct);
    }
    if (values.maintPct !== undefined && typeof values.maintPct !== 'number') {
      values.maintPct = Number(values.maintPct);
    }

    const entity = {
      ...marginRuleEntity,
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
          ...marginRuleEntity,
          exchange: marginRuleEntity?.exchange?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rnexchangeApp.marginRule.home.createOrEditLabel" data-cy="MarginRuleCreateUpdateHeading">
            <Translate contentKey="rnexchangeApp.marginRule.home.createOrEditLabel">Create or edit a MarginRule</Translate>
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
                  id="margin-rule-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rnexchangeApp.marginRule.scope')}
                id="margin-rule-scope"
                name="scope"
                data-cy="scope"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rnexchangeApp.marginRule.initialPct')}
                id="margin-rule-initialPct"
                name="initialPct"
                data-cy="initialPct"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.marginRule.maintPct')}
                id="margin-rule-maintPct"
                name="maintPct"
                data-cy="maintPct"
                type="text"
              />
              <ValidatedField
                label={translate('rnexchangeApp.marginRule.spanJson')}
                id="margin-rule-spanJson"
                name="spanJson"
                data-cy="spanJson"
                type="textarea"
              />
              <ValidatedField
                id="margin-rule-exchange"
                name="exchange"
                data-cy="exchange"
                label={translate('rnexchangeApp.marginRule.exchange')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/margin-rule" replace color="info">
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

export default MarginRuleUpdate;

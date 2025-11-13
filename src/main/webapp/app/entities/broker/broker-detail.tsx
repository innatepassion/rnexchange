import React, { useEffect, useMemo } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Badge, Button, Col, Row, Table } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getBaseline, getEntity } from './broker.reducer';
import { IBrokerInstrument } from 'app/shared/model/broker.model';

export const BrokerDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
    dispatch(getBaseline(id));
  }, [dispatch, id]);

  const brokerEntity = useAppSelector(state => state.broker.entity);
  const baseline = useAppSelector(state => state.broker.baseline);

  const formatDecimal = (value?: number | null) => {
    if (value === undefined || value === null) {
      return '';
    }
    return Number(value).toFixed(2);
  };

  const instrumentCatalog = useMemo(() => baseline?.instrumentCatalog ?? [], [baseline]);

  const renderInstrumentRow = (instrument: IBrokerInstrument) => (
    <tr key={instrument.symbol} data-cy="broker-instrument-row">
      <td>{instrument.symbol}</td>
      <td>{instrument.name}</td>
      <td>{instrument.exchangeCode}</td>
      <td>{instrument.assetClass}</td>
      <td>{formatDecimal(instrument.tickSize)}</td>
      <td>{instrument.lotSize}</td>
      <td>{instrument.currency}</td>
    </tr>
  );

  return (
    <Row>
      <Col md="8">
        <h2 data-cy="brokerDetailsHeading">
          <Translate contentKey="rnexchangeApp.broker.detail.title">Broker</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.id}</dd>
          <dt>
            <span id="code">
              <Translate contentKey="rnexchangeApp.broker.code">Code</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.code}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="rnexchangeApp.broker.name">Name</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.name}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="rnexchangeApp.broker.status">Status</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.status}</dd>
          <dt>
            <span id="createdDate">
              <Translate contentKey="rnexchangeApp.broker.createdDate">Created Date</Translate>
            </span>
          </dt>
          <dd>{brokerEntity.createdDate ? <TextFormat value={brokerEntity.createdDate} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.broker.exchange">Exchange</Translate>
          </dt>
          <dd>{brokerEntity.exchange ? brokerEntity.exchange.code : ''}</dd>
        </dl>
        {baseline && (
          <section className="mt-4">
            <h3 className="h5">
              <Translate contentKey="rnexchangeApp.broker.baseline.title">Baseline Overview</Translate>
            </h3>
            <dl className="row">
              <dt className="col-sm-4">
                <Translate contentKey="rnexchangeApp.broker.baseline.name">Broker Name</Translate>
              </dt>
              <dd className="col-sm-8" data-cy="broker-baseline-name">
                {baseline.name}
              </dd>
              <dt className="col-sm-4">
                <Translate contentKey="rnexchangeApp.broker.baseline.code">Broker Code</Translate>
              </dt>
              <dd className="col-sm-8" data-cy="broker-baseline-code">
                {baseline.code}
              </dd>
              <dt className="col-sm-4">
                <Translate contentKey="rnexchangeApp.broker.baseline.exchange">Primary Exchange</Translate>
              </dt>
              <dd className="col-sm-8">
                {baseline.exchangeCode}
                {baseline.exchangeName ? <span className="ms-2 text-muted">({baseline.exchangeName})</span> : null}
              </dd>
              <dt className="col-sm-4">
                <Translate contentKey="rnexchangeApp.broker.baseline.brokerAdmin">Broker Admin</Translate>
              </dt>
              <dd className="col-sm-8" data-cy="broker-admin-login">
                {baseline.brokerAdminLogin}
              </dd>
            </dl>
            <div className="mb-3">
              <Translate contentKey="rnexchangeApp.broker.baseline.memberships">Exchange Memberships</Translate>
              <div data-cy="broker-exchange-membership" className="mt-2">
                {baseline.exchangeMemberships?.map(code => (
                  <Badge key={code} color="info" pill className="me-2 mb-2">
                    {code}
                  </Badge>
                ))}
              </div>
            </div>
            <div>
              <Translate contentKey="rnexchangeApp.broker.baseline.instrumentCatalog">Instrument Catalog</Translate>
              <div className="text-muted mb-2">
                <Translate
                  contentKey="rnexchangeApp.broker.baseline.instrumentCount"
                  interpolate={{ count: baseline.instrumentCount ?? instrumentCatalog.length }}
                >
                  Instrument count
                </Translate>
              </div>
              <div className="table-responsive">
                <Table bordered hover size="sm">
                  <thead>
                    <tr>
                      <th>
                        <Translate contentKey="rnexchangeApp.broker.baseline.instrument.symbol">Symbol</Translate>
                      </th>
                      <th>
                        <Translate contentKey="rnexchangeApp.broker.baseline.instrument.name">Name</Translate>
                      </th>
                      <th>
                        <Translate contentKey="rnexchangeApp.broker.baseline.instrument.exchange">Exchange</Translate>
                      </th>
                      <th>
                        <Translate contentKey="rnexchangeApp.broker.baseline.instrument.assetClass">Asset Class</Translate>
                      </th>
                      <th>
                        <Translate contentKey="rnexchangeApp.broker.baseline.instrument.tickSize">Tick Size</Translate>
                      </th>
                      <th>
                        <Translate contentKey="rnexchangeApp.broker.baseline.instrument.lotSize">Lot Size</Translate>
                      </th>
                      <th>
                        <Translate contentKey="rnexchangeApp.broker.baseline.instrument.currency">Currency</Translate>
                      </th>
                    </tr>
                  </thead>
                  <tbody>{instrumentCatalog.map(renderInstrumentRow)}</tbody>
                </Table>
              </div>
            </div>
          </section>
        )}
        <Button tag={Link} to="/broker" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/broker/${brokerEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default BrokerDetail;

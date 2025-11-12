import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './ledger-entry.reducer';

export const LedgerEntryDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const ledgerEntryEntity = useAppSelector(state => state.ledgerEntry.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="ledgerEntryDetailsHeading">
          <Translate contentKey="rnexchangeApp.ledgerEntry.detail.title">LedgerEntry</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{ledgerEntryEntity.id}</dd>
          <dt>
            <span id="ts">
              <Translate contentKey="rnexchangeApp.ledgerEntry.ts">Ts</Translate>
            </span>
          </dt>
          <dd>{ledgerEntryEntity.ts ? <TextFormat value={ledgerEntryEntity.ts} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="rnexchangeApp.ledgerEntry.type">Type</Translate>
            </span>
          </dt>
          <dd>{ledgerEntryEntity.type}</dd>
          <dt>
            <span id="amount">
              <Translate contentKey="rnexchangeApp.ledgerEntry.amount">Amount</Translate>
            </span>
          </dt>
          <dd>{ledgerEntryEntity.amount}</dd>
          <dt>
            <span id="ccy">
              <Translate contentKey="rnexchangeApp.ledgerEntry.ccy">Ccy</Translate>
            </span>
          </dt>
          <dd>{ledgerEntryEntity.ccy}</dd>
          <dt>
            <span id="balanceAfter">
              <Translate contentKey="rnexchangeApp.ledgerEntry.balanceAfter">Balance After</Translate>
            </span>
          </dt>
          <dd>{ledgerEntryEntity.balanceAfter}</dd>
          <dt>
            <span id="reference">
              <Translate contentKey="rnexchangeApp.ledgerEntry.reference">Reference</Translate>
            </span>
          </dt>
          <dd>{ledgerEntryEntity.reference}</dd>
          <dt>
            <span id="remarks">
              <Translate contentKey="rnexchangeApp.ledgerEntry.remarks">Remarks</Translate>
            </span>
          </dt>
          <dd>{ledgerEntryEntity.remarks}</dd>
          <dt>
            <Translate contentKey="rnexchangeApp.ledgerEntry.tradingAccount">Trading Account</Translate>
          </dt>
          <dd>{ledgerEntryEntity.tradingAccount ? ledgerEntryEntity.tradingAccount.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/ledger-entry" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/ledger-entry/${ledgerEntryEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default LedgerEntryDetail;

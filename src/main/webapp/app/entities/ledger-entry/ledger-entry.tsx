import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './ledger-entry.reducer';

export const LedgerEntry = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const ledgerEntryList = useAppSelector(state => state.ledgerEntry.entities);
  const loading = useAppSelector(state => state.ledgerEntry.loading);
  const totalItems = useAppSelector(state => state.ledgerEntry.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const order = paginationState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="ledger-entry-heading" data-cy="LedgerEntryHeading">
        <Translate contentKey="rnexchangeApp.ledgerEntry.home.title">Ledger Entries</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="rnexchangeApp.ledgerEntry.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/ledger-entry/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="rnexchangeApp.ledgerEntry.home.createLabel">Create new Ledger Entry</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {ledgerEntryList && ledgerEntryList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="rnexchangeApp.ledgerEntry.id">ID</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('ts')}>
                  <Translate contentKey="rnexchangeApp.ledgerEntry.ts">Ts</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('ts')} />
                </th>
                <th className="hand" onClick={sort('type')}>
                  <Translate contentKey="rnexchangeApp.ledgerEntry.type">Type</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('type')} />
                </th>
                <th className="hand" onClick={sort('amount')}>
                  <Translate contentKey="rnexchangeApp.ledgerEntry.amount">Amount</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('amount')} />
                </th>
                <th className="hand" onClick={sort('ccy')}>
                  <Translate contentKey="rnexchangeApp.ledgerEntry.ccy">Ccy</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('ccy')} />
                </th>
                <th className="hand" onClick={sort('balanceAfter')}>
                  <Translate contentKey="rnexchangeApp.ledgerEntry.balanceAfter">Balance After</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('balanceAfter')} />
                </th>
                <th className="hand" onClick={sort('reference')}>
                  <Translate contentKey="rnexchangeApp.ledgerEntry.reference">Reference</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('reference')} />
                </th>
                <th className="hand" onClick={sort('remarks')}>
                  <Translate contentKey="rnexchangeApp.ledgerEntry.remarks">Remarks</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('remarks')} />
                </th>
                <th>
                  <Translate contentKey="rnexchangeApp.ledgerEntry.tradingAccount">Trading Account</Translate>{' '}
                  <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {ledgerEntryList.map((ledgerEntry, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/ledger-entry/${ledgerEntry.id}`} color="link" size="sm">
                      {ledgerEntry.id}
                    </Button>
                  </td>
                  <td>{ledgerEntry.ts ? <TextFormat type="date" value={ledgerEntry.ts} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{ledgerEntry.type}</td>
                  <td>{ledgerEntry.amount}</td>
                  <td>
                    <Translate contentKey={`rnexchangeApp.Currency.${ledgerEntry.ccy}`} />
                  </td>
                  <td>{ledgerEntry.balanceAfter}</td>
                  <td>{ledgerEntry.reference}</td>
                  <td>{ledgerEntry.remarks}</td>
                  <td>
                    {ledgerEntry.tradingAccount ? (
                      <Link to={`/trading-account/${ledgerEntry.tradingAccount.id}`}>{ledgerEntry.tradingAccount.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/ledger-entry/${ledgerEntry.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/ledger-entry/${ledgerEntry.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() =>
                          (window.location.href = `/ledger-entry/${ledgerEntry.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="rnexchangeApp.ledgerEntry.home.notFound">No Ledger Entries found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={ledgerEntryList && ledgerEntryList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default LedgerEntry;

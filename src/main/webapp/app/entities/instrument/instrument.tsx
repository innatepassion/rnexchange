import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, Translate, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './instrument.reducer';

export const Instrument = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const instrumentList = useAppSelector(state => state.instrument.entities);
  const loading = useAppSelector(state => state.instrument.loading);
  const totalItems = useAppSelector(state => state.instrument.totalItems);

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
      <h2 id="instrument-heading" data-cy="InstrumentHeading">
        <Translate contentKey="rnexchangeApp.instrument.home.title">Instruments</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="rnexchangeApp.instrument.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/instrument/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="rnexchangeApp.instrument.home.createLabel">Create new Instrument</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {instrumentList && instrumentList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="rnexchangeApp.instrument.id">ID</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('symbol')}>
                  <Translate contentKey="rnexchangeApp.instrument.symbol">Symbol</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('symbol')} />
                </th>
                <th className="hand" onClick={sort('name')}>
                  <Translate contentKey="rnexchangeApp.instrument.name">Name</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                </th>
                <th className="hand" onClick={sort('assetClass')}>
                  <Translate contentKey="rnexchangeApp.instrument.assetClass">Asset Class</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('assetClass')} />
                </th>
                <th className="hand" onClick={sort('exchangeCode')}>
                  <Translate contentKey="rnexchangeApp.instrument.exchangeCode">Exchange Code</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('exchangeCode')} />
                </th>
                <th className="hand" onClick={sort('tickSize')}>
                  <Translate contentKey="rnexchangeApp.instrument.tickSize">Tick Size</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('tickSize')} />
                </th>
                <th className="hand" onClick={sort('lotSize')}>
                  <Translate contentKey="rnexchangeApp.instrument.lotSize">Lot Size</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('lotSize')} />
                </th>
                <th className="hand" onClick={sort('currency')}>
                  <Translate contentKey="rnexchangeApp.instrument.currency">Currency</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('currency')} />
                </th>
                <th className="hand" onClick={sort('status')}>
                  <Translate contentKey="rnexchangeApp.instrument.status">Status</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                </th>
                <th>
                  <Translate contentKey="rnexchangeApp.instrument.exchange">Exchange</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {instrumentList.map((instrument, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/instrument/${instrument.id}`} color="link" size="sm">
                      {instrument.id}
                    </Button>
                  </td>
                  <td>{instrument.symbol}</td>
                  <td>{instrument.name}</td>
                  <td>
                    <Translate contentKey={`rnexchangeApp.AssetClass.${instrument.assetClass}`} />
                  </td>
                  <td>{instrument.exchangeCode}</td>
                  <td>{instrument.tickSize}</td>
                  <td>{instrument.lotSize}</td>
                  <td>
                    <Translate contentKey={`rnexchangeApp.Currency.${instrument.currency}`} />
                  </td>
                  <td>{instrument.status}</td>
                  <td>{instrument.exchange ? <Link to={`/exchange/${instrument.exchange.id}`}>{instrument.exchange.code}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/instrument/${instrument.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/instrument/${instrument.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                          (window.location.href = `/instrument/${instrument.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
              <Translate contentKey="rnexchangeApp.instrument.home.notFound">No Instruments found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={instrumentList && instrumentList.length > 0 ? '' : 'd-none'}>
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

export default Instrument;

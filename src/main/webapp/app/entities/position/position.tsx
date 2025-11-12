import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, Translate, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './position.reducer';

export const Position = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const positionList = useAppSelector(state => state.position.entities);
  const loading = useAppSelector(state => state.position.loading);
  const totalItems = useAppSelector(state => state.position.totalItems);

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
      <h2 id="position-heading" data-cy="PositionHeading">
        <Translate contentKey="rnexchangeApp.position.home.title">Positions</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="rnexchangeApp.position.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/position/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="rnexchangeApp.position.home.createLabel">Create new Position</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {positionList && positionList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="rnexchangeApp.position.id">ID</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('qty')}>
                  <Translate contentKey="rnexchangeApp.position.qty">Qty</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('qty')} />
                </th>
                <th className="hand" onClick={sort('avgCost')}>
                  <Translate contentKey="rnexchangeApp.position.avgCost">Avg Cost</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('avgCost')} />
                </th>
                <th className="hand" onClick={sort('lastPx')}>
                  <Translate contentKey="rnexchangeApp.position.lastPx">Last Px</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('lastPx')} />
                </th>
                <th className="hand" onClick={sort('unrealizedPnl')}>
                  <Translate contentKey="rnexchangeApp.position.unrealizedPnl">Unrealized Pnl</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('unrealizedPnl')} />
                </th>
                <th className="hand" onClick={sort('realizedPnl')}>
                  <Translate contentKey="rnexchangeApp.position.realizedPnl">Realized Pnl</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('realizedPnl')} />
                </th>
                <th>
                  <Translate contentKey="rnexchangeApp.position.tradingAccount">Trading Account</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  <Translate contentKey="rnexchangeApp.position.instrument">Instrument</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {positionList.map((position, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/position/${position.id}`} color="link" size="sm">
                      {position.id}
                    </Button>
                  </td>
                  <td>{position.qty}</td>
                  <td>{position.avgCost}</td>
                  <td>{position.lastPx}</td>
                  <td>{position.unrealizedPnl}</td>
                  <td>{position.realizedPnl}</td>
                  <td>
                    {position.tradingAccount ? (
                      <Link to={`/trading-account/${position.tradingAccount.id}`}>{position.tradingAccount.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td>
                    {position.instrument ? <Link to={`/instrument/${position.instrument.id}`}>{position.instrument.symbol}</Link> : ''}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/position/${position.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/position/${position.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                          (window.location.href = `/position/${position.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
              <Translate contentKey="rnexchangeApp.position.home.notFound">No Positions found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={positionList && positionList.length > 0 ? '' : 'd-none'}>
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

export default Position;

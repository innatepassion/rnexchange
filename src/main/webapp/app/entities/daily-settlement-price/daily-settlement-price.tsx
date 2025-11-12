import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './daily-settlement-price.reducer';

export const DailySettlementPrice = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const dailySettlementPriceList = useAppSelector(state => state.dailySettlementPrice.entities);
  const loading = useAppSelector(state => state.dailySettlementPrice.loading);
  const totalItems = useAppSelector(state => state.dailySettlementPrice.totalItems);

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
      <h2 id="daily-settlement-price-heading" data-cy="DailySettlementPriceHeading">
        <Translate contentKey="rnexchangeApp.dailySettlementPrice.home.title">Daily Settlement Prices</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="rnexchangeApp.dailySettlementPrice.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link
            to="/daily-settlement-price/new"
            className="btn btn-primary jh-create-entity"
            id="jh-create-entity"
            data-cy="entityCreateButton"
          >
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="rnexchangeApp.dailySettlementPrice.home.createLabel">Create new Daily Settlement Price</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {dailySettlementPriceList && dailySettlementPriceList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="rnexchangeApp.dailySettlementPrice.id">ID</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('refDate')}>
                  <Translate contentKey="rnexchangeApp.dailySettlementPrice.refDate">Ref Date</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('refDate')} />
                </th>
                <th className="hand" onClick={sort('instrumentSymbol')}>
                  <Translate contentKey="rnexchangeApp.dailySettlementPrice.instrumentSymbol">Instrument Symbol</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('instrumentSymbol')} />
                </th>
                <th className="hand" onClick={sort('settlePrice')}>
                  <Translate contentKey="rnexchangeApp.dailySettlementPrice.settlePrice">Settle Price</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('settlePrice')} />
                </th>
                <th>
                  <Translate contentKey="rnexchangeApp.dailySettlementPrice.instrument">Instrument</Translate>{' '}
                  <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {dailySettlementPriceList.map((dailySettlementPrice, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/daily-settlement-price/${dailySettlementPrice.id}`} color="link" size="sm">
                      {dailySettlementPrice.id}
                    </Button>
                  </td>
                  <td>
                    {dailySettlementPrice.refDate ? (
                      <TextFormat type="date" value={dailySettlementPrice.refDate} format={APP_LOCAL_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{dailySettlementPrice.instrumentSymbol}</td>
                  <td>{dailySettlementPrice.settlePrice}</td>
                  <td>
                    {dailySettlementPrice.instrument ? (
                      <Link to={`/instrument/${dailySettlementPrice.instrument.id}`}>{dailySettlementPrice.instrument.symbol}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/daily-settlement-price/${dailySettlementPrice.id}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/daily-settlement-price/${dailySettlementPrice.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                          (window.location.href = `/daily-settlement-price/${dailySettlementPrice.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
              <Translate contentKey="rnexchangeApp.dailySettlementPrice.home.notFound">No Daily Settlement Prices found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={dailySettlementPriceList && dailySettlementPriceList.length > 0 ? '' : 'd-none'}>
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

export default DailySettlementPrice;

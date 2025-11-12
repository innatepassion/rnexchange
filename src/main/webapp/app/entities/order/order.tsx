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

import { getEntities } from './order.reducer';

export const Order = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const orderList = useAppSelector(state => state.order.entities);
  const loading = useAppSelector(state => state.order.loading);
  const totalItems = useAppSelector(state => state.order.totalItems);

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
      <h2 id="order-heading" data-cy="OrderHeading">
        <Translate contentKey="rnexchangeApp.order.home.title">Orders</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="rnexchangeApp.order.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/order/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="rnexchangeApp.order.home.createLabel">Create new Order</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {orderList && orderList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="rnexchangeApp.order.id">ID</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('side')}>
                  <Translate contentKey="rnexchangeApp.order.side">Side</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('side')} />
                </th>
                <th className="hand" onClick={sort('type')}>
                  <Translate contentKey="rnexchangeApp.order.type">Type</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('type')} />
                </th>
                <th className="hand" onClick={sort('qty')}>
                  <Translate contentKey="rnexchangeApp.order.qty">Qty</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('qty')} />
                </th>
                <th className="hand" onClick={sort('limitPx')}>
                  <Translate contentKey="rnexchangeApp.order.limitPx">Limit Px</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('limitPx')} />
                </th>
                <th className="hand" onClick={sort('stopPx')}>
                  <Translate contentKey="rnexchangeApp.order.stopPx">Stop Px</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('stopPx')} />
                </th>
                <th className="hand" onClick={sort('tif')}>
                  <Translate contentKey="rnexchangeApp.order.tif">Tif</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('tif')} />
                </th>
                <th className="hand" onClick={sort('status')}>
                  <Translate contentKey="rnexchangeApp.order.status">Status</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                </th>
                <th className="hand" onClick={sort('venue')}>
                  <Translate contentKey="rnexchangeApp.order.venue">Venue</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('venue')} />
                </th>
                <th className="hand" onClick={sort('createdAt')}>
                  <Translate contentKey="rnexchangeApp.order.createdAt">Created At</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('createdAt')} />
                </th>
                <th className="hand" onClick={sort('updatedAt')}>
                  <Translate contentKey="rnexchangeApp.order.updatedAt">Updated At</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('updatedAt')} />
                </th>
                <th>
                  <Translate contentKey="rnexchangeApp.order.tradingAccount">Trading Account</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  <Translate contentKey="rnexchangeApp.order.instrument">Instrument</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {orderList.map((order, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/order/${order.id}`} color="link" size="sm">
                      {order.id}
                    </Button>
                  </td>
                  <td>
                    <Translate contentKey={`rnexchangeApp.OrderSide.${order.side}`} />
                  </td>
                  <td>
                    <Translate contentKey={`rnexchangeApp.OrderType.${order.type}`} />
                  </td>
                  <td>{order.qty}</td>
                  <td>{order.limitPx}</td>
                  <td>{order.stopPx}</td>
                  <td>
                    <Translate contentKey={`rnexchangeApp.Tif.${order.tif}`} />
                  </td>
                  <td>
                    <Translate contentKey={`rnexchangeApp.OrderStatus.${order.status}`} />
                  </td>
                  <td>{order.venue}</td>
                  <td>{order.createdAt ? <TextFormat type="date" value={order.createdAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{order.updatedAt ? <TextFormat type="date" value={order.updatedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>
                    {order.tradingAccount ? <Link to={`/trading-account/${order.tradingAccount.id}`}>{order.tradingAccount.id}</Link> : ''}
                  </td>
                  <td>{order.instrument ? <Link to={`/instrument/${order.instrument.id}`}>{order.instrument.symbol}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/order/${order.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/order/${order.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                          (window.location.href = `/order/${order.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
              <Translate contentKey="rnexchangeApp.order.home.notFound">No Orders found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={orderList && orderList.length > 0 ? '' : 'd-none'}>
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

export default Order;

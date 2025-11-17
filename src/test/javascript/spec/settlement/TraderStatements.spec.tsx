import '@testing-library/jest-dom';
import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import TraderStatementsModule from '../../../../main/webapp/app/modules/trader/statements/index';

const mockAxios = new MockAdapter(axios);

const createStore = () =>
  configureStore({
    reducer: {
      authentication: () => ({
        isAuthenticated: true,
        account: { login: 'trader1', authorities: ['TRADER'] },
      }),
    },
  });

const renderWithStore = (store = createStore()) =>
  render(
    <Provider store={store}>
      <TraderStatementsModule />
    </Provider>,
  );

describe('TraderStatements', () => {
  beforeEach(() => {
    mockAxios.reset();
  });

  it('renders loading state initially', () => {
    mockAxios.onGet('/api/statements').reply(200, []);
    renderWithStore();
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });

  it('displays statements list when loaded', async () => {
    const mockStatements = [
      {
        id: 1,
        refDate: '2025-01-15',
        tradingAccountId: 1,
        openingBalance: 10000,
        eodMtmPnl: 500,
        closingBalance: 10500,
        htmlUrl: '/api/statements/1/html',
      },
    ];
    mockAxios.onGet('/api/statements').reply(200, mockStatements);
    renderWithStore();

    await waitFor(() => {
      expect(screen.getByText(/Statements/i)).toBeInTheDocument();
    });
  });

  it('handles view statement click', async () => {
    const mockStatements = [
      {
        id: 1,
        refDate: '2025-01-15',
        tradingAccountId: 1,
        openingBalance: 10000,
        eodMtmPnl: 500,
        closingBalance: 10500,
        htmlUrl: '/api/statements/1/html',
      },
    ];
    mockAxios.onGet('/api/statements').reply(200, mockStatements);

    const windowOpenSpy = jest.spyOn(window, 'open').mockImplementation(() => null);

    renderWithStore();

    await waitFor(() => {
      const viewButton = screen.getByText(/View/i);
      fireEvent.click(viewButton);
      expect(windowOpenSpy).toHaveBeenCalledWith('/api/statements/1/html', '_blank');
    });

    windowOpenSpy.mockRestore();
  });

  it('displays error message on API failure', async () => {
    mockAxios.onGet('/api/statements').reply(500, { message: 'Server error' });
    renderWithStore();

    await waitFor(() => {
      expect(screen.getByText(/Failed to load statements/i)).toBeInTheDocument();
    });
  });

  it('formats currency correctly', async () => {
    const mockStatements = [
      {
        id: 1,
        refDate: '2025-01-15',
        tradingAccountId: 1,
        openingBalance: 1234567.89,
        eodMtmPnl: 500.5,
        closingBalance: 1235068.39,
        htmlUrl: '/api/statements/1/html',
      },
    ];
    mockAxios.onGet('/api/statements').reply(200, mockStatements);
    renderWithStore();

    await waitFor(() => {
      expect(screen.getByText(/₹1,234,567.89/i)).toBeInTheDocument();
    });
  });
});

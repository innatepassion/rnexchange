import '@testing-library/jest-dom';
import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import BrokerSettlementsModule from '../../../../main/webapp/app/modules/broker/settlements/index';

const mockAxios = new MockAdapter(axios);

const createStore = () =>
  configureStore({
    reducer: {
      authentication: () => ({
        isAuthenticated: true,
        account: { login: 'broker1', authorities: ['BROKER_ADMIN'] },
      }),
    },
  });

const renderWithStore = (store = createStore()) =>
  render(
    <Provider store={store}>
      <BrokerSettlementsModule />
    </Provider>,
  );

describe('BrokerSettlements', () => {
  beforeEach(() => {
    mockAxios.reset();
  });

  it('renders loading state initially', () => {
    mockAxios.onGet('/api/broker/settlements').reply(200, []);
    renderWithStore();
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });

  it('displays settlements list when loaded', async () => {
    const mockSettlements = [
      {
        refDate: '2025-01-15',
        brokerId: 1,
        brokerName: 'Test Broker',
        totalClientCount: 10,
        totalOpeningBalance: 100000,
        totalClosingBalance: 105000,
        totalEodMtmPnl: 5000,
        summaryUrl: '/api/broker/settlements/1/summary',
      },
    ];
    mockAxios.onGet('/api/broker/settlements').reply(200, mockSettlements);
    renderWithStore();

    await waitFor(() => {
      expect(screen.getByText(/Settlements/i)).toBeInTheDocument();
    });
  });

  it('handles view summary click', async () => {
    const mockSettlements = [
      {
        refDate: '2025-01-15',
        brokerId: 1,
        brokerName: 'Test Broker',
        totalClientCount: 10,
        totalOpeningBalance: 100000,
        totalClosingBalance: 105000,
        totalEodMtmPnl: 5000,
        summaryUrl: '/api/broker/settlements/1/summary',
      },
    ];
    mockAxios.onGet('/api/broker/settlements').reply(200, mockSettlements);

    const windowOpenSpy = jest.spyOn(window, 'open').mockImplementation(() => null);

    renderWithStore();

    await waitFor(() => {
      const viewButton = screen.getByText(/View Summary/i);
      fireEvent.click(viewButton);
      expect(windowOpenSpy).toHaveBeenCalledWith('/api/broker/settlements/1/summary', '_blank');
    });

    windowOpenSpy.mockRestore();
  });

  it('displays error message on API failure', async () => {
    mockAxios.onGet('/api/broker/settlements').reply(500, { message: 'Server error' });
    renderWithStore();

    await waitFor(() => {
      expect(screen.getByText(/Failed to load settlements/i)).toBeInTheDocument();
    });
  });

  it('formats currency correctly', async () => {
    const mockSettlements = [
      {
        refDate: '2025-01-15',
        brokerId: 1,
        brokerName: 'Test Broker',
        totalClientCount: 10,
        totalOpeningBalance: 1234567.89,
        totalClosingBalance: 1239567.89,
        totalEodMtmPnl: 5000,
        summaryUrl: '/api/broker/settlements/1/summary',
      },
    ];
    mockAxios.onGet('/api/broker/settlements').reply(200, mockSettlements);
    renderWithStore();

    await waitFor(() => {
      expect(screen.getByText(/₹1,234,567.89/i)).toBeInTheDocument();
    });
  });
});

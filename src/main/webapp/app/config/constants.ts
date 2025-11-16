export const AUTHORITIES = {
  ADMIN: 'ROLE_ADMIN',
  USER: 'ROLE_USER',
  EXCHANGE_OPERATOR: 'ROLE_EXCHANGE_OPERATOR',
  TRADER: 'ROLE_TRADER',
  BROKER_ADMIN: 'ROLE_BROKER_ADMIN',
};

export const messages = {
  DATA_ERROR_ALERT: 'Internal Error',
};

// Temporary default trading account ID used for demo/training traders (e.g., trader-one).
// TODO: Replace with dynamic lookup based on the logged-in trader's linked trading accounts.
export const DEFAULT_TRADING_ACCOUNT_ID = 1;

export const APP_DATE_FORMAT = 'DD/MM/YY HH:mm';
export const APP_TIMESTAMP_FORMAT = 'DD/MM/YY HH:mm:ss';
export const APP_LOCAL_DATE_FORMAT = 'DD/MM/YYYY';
export const APP_LOCAL_DATETIME_FORMAT = 'YYYY-MM-DDTHH:mm';
export const APP_WHOLE_NUMBER_FORMAT = '0,0';
export const APP_TWO_DIGITS_AFTER_POINT_NUMBER_FORMAT = '0,0.[00]';

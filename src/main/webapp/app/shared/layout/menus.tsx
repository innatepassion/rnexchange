/**
 * M6 Phase 2: Centralized role-aware menu configuration.
 *
 * This module provides a single source of truth for role-based navigation menus.
 * It defines which menu items should be visible for each role, ensuring consistent
 * navigation behavior across the application.
 *
 * @module menus
 */

import { AUTHORITIES } from 'app/config/constants';

/**
 * Menu visibility configuration by role.
 *
 * Each role has a set of menu keys that should be visible. Menu keys correspond
 * to menu components rendered in the Header component.
 */
export interface RoleMenuConfig {
  /** Show the generic Entities menu (JHipster-generated entity CRUD) */
  showEntities: boolean;
  /** Show the Administration menu (user management, metrics, health, etc.) */
  showAdmin: boolean;
  /** Show the Exchange Console menu */
  showExchangeConsole: boolean;
  /** Show the Trader menu */
  showTrader: boolean;
  /** Show the Broker Admin menu */
  showBrokerAdmin: boolean;
  /** Show the Market Watch direct link (for traders) */
  showMarketWatch: boolean;
}

/**
 * Default menu configuration for each role.
 *
 * M6 Requirements:
 * - Trader: Only trading-related menus (Market Watch, Trader menu), no Entities/Admin
 * - Broker Admin: Only broker-related menus (Broker Admin menu), no Entities/Admin
 * - Exchange Operator: Exchange Console and relevant admin items, no generic Entities
 * - Admin: All menus (full access)
 */
export const ROLE_MENU_CONFIG: Record<string, RoleMenuConfig> = {
  [AUTHORITIES.TRADER]: {
    showEntities: false,
    showAdmin: false,
    showExchangeConsole: false,
    showTrader: true,
    showBrokerAdmin: false,
    showMarketWatch: true,
  },
  [AUTHORITIES.BROKER_ADMIN]: {
    showEntities: false,
    showAdmin: false,
    showExchangeConsole: false,
    showTrader: false,
    showBrokerAdmin: true,
    showMarketWatch: false,
  },
  [AUTHORITIES.EXCHANGE_OPERATOR]: {
    showEntities: false, // Hide generic JHipster entities
    showAdmin: true, // Show admin items for system management
    showExchangeConsole: true,
    showTrader: false,
    showBrokerAdmin: false,
    showMarketWatch: false,
  },
  [AUTHORITIES.ADMIN]: {
    showEntities: true,
    showAdmin: true,
    showExchangeConsole: true,
    showTrader: false,
    showBrokerAdmin: false,
    showMarketWatch: false,
  },
  // Default for USER role (fallback)
  [AUTHORITIES.USER]: {
    showEntities: true,
    showAdmin: false,
    showExchangeConsole: false,
    showTrader: false,
    showBrokerAdmin: false,
    showMarketWatch: false,
  },
};

/**
 * Resolves menu configuration for a user based on their authorities.
 *
 * The function checks authorities in priority order and returns the configuration
 * for the highest-priority role. If multiple roles are present, the first matching
 * role in priority order is used.
 *
 * Priority order:
 * 1. TRADER
 * 2. BROKER_ADMIN
 * 3. EXCHANGE_OPERATOR
 * 4. ADMIN
 * 5. USER (fallback)
 *
 * @param authorities - Array of user authority strings
 * @returns Menu configuration for the user's primary role
 */
export const resolveRoleMenuConfig = (authorities: string[] = []): RoleMenuConfig => {
  if (!authorities || authorities.length === 0) {
    return ROLE_MENU_CONFIG[AUTHORITIES.USER];
  }

  // Check in priority order
  if (authorities.includes(AUTHORITIES.TRADER)) {
    return ROLE_MENU_CONFIG[AUTHORITIES.TRADER];
  }

  if (authorities.includes(AUTHORITIES.BROKER_ADMIN)) {
    return ROLE_MENU_CONFIG[AUTHORITIES.BROKER_ADMIN];
  }

  if (authorities.includes(AUTHORITIES.EXCHANGE_OPERATOR)) {
    return ROLE_MENU_CONFIG[AUTHORITIES.EXCHANGE_OPERATOR];
  }

  if (authorities.includes(AUTHORITIES.ADMIN)) {
    return ROLE_MENU_CONFIG[AUTHORITIES.ADMIN];
  }

  // Default fallback
  return ROLE_MENU_CONFIG[AUTHORITIES.USER];
};

/**
 * Gets menu configuration for a specific role.
 *
 * @param role - The authority constant (e.g., AUTHORITIES.TRADER)
 * @returns Menu configuration for that role, or USER config if not found
 */
export const getRoleMenuConfig = (role: string): RoleMenuConfig => {
  return ROLE_MENU_CONFIG[role] || ROLE_MENU_CONFIG[AUTHORITIES.USER];
};

/**
 * M6 Phase 2: Role-based default landing route resolution.
 *
 * This module provides a centralized mapping of user roles to their default
 * landing routes after login. This ensures consistent role-based navigation
 * and simplifies maintenance of landing page logic.
 *
 * @module role-landing-resolver
 */

import { AUTHORITIES } from 'app/config/constants';

/**
 * Default landing routes for each role.
 *
 * These routes are used when a user logs in and no specific redirect target
 * is provided (e.g., from the login page or after session restoration).
 */
export const ROLE_LANDING_ROUTES: Record<string, string> = {
  [AUTHORITIES.TRADER]: '/market-watch',
  [AUTHORITIES.BROKER_ADMIN]: '/broker/dashboard',
  [AUTHORITIES.EXCHANGE_OPERATOR]: '/exchange/overview',
  // Fallback for admin and other roles
  [AUTHORITIES.ADMIN]: '/admin',
  [AUTHORITIES.USER]: '/',
};

/**
 * Resolves the default landing route for a user based on their authorities.
 *
 * The function checks authorities in priority order:
 * 1. TRADER → /market-watch
 * 2. BROKER_ADMIN → /broker/dashboard
 * 3. EXCHANGE_OPERATOR → /exchange-console
 * 4. ADMIN → /admin
 * 5. Default → / (home)
 *
 * @param authorities - Array of user authority strings (e.g., ['ROLE_TRADER'])
 * @returns The default landing route path for the user's primary role
 *
 * @example
 * ```typescript
 * const route = resolveRoleLandingRoute(['ROLE_TRADER']);
 * // Returns: '/market-watch'
 *
 * const route = resolveRoleLandingRoute(['ROLE_BROKER_ADMIN', 'ROLE_USER']);
 * // Returns: '/broker/dashboard'
 * ```
 */
export const resolveRoleLandingRoute = (authorities: string[] = []): string => {
  if (!authorities || authorities.length === 0) {
    return '/';
  }

  // Check in priority order: TRADER, BROKER_ADMIN, EXCHANGE_OPERATOR, ADMIN
  if (authorities.includes(AUTHORITIES.TRADER)) {
    return ROLE_LANDING_ROUTES[AUTHORITIES.TRADER];
  }

  if (authorities.includes(AUTHORITIES.BROKER_ADMIN)) {
    return ROLE_LANDING_ROUTES[AUTHORITIES.BROKER_ADMIN];
  }

  if (authorities.includes(AUTHORITIES.EXCHANGE_OPERATOR)) {
    return ROLE_LANDING_ROUTES[AUTHORITIES.EXCHANGE_OPERATOR];
  }

  if (authorities.includes(AUTHORITIES.ADMIN)) {
    return ROLE_LANDING_ROUTES[AUTHORITIES.ADMIN];
  }

  // Default fallback
  return ROLE_LANDING_ROUTES[AUTHORITIES.USER] || '/';
};

/**
 * Gets the landing route for a specific role.
 *
 * @param role - The authority constant (e.g., AUTHORITIES.TRADER)
 * @returns The landing route for that role, or '/' if not found
 */
export const getRoleLandingRoute = (role: string): string => {
  return ROLE_LANDING_ROUTES[role] || '/';
};

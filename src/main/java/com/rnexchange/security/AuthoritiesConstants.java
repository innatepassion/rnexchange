package com.rnexchange.security;

/**
 * Constants for Spring Security authorities.
 *
 * <p>These constants define the role-based access control (RBAC) authorities used throughout
 * the RNExchange application. All authorities follow the Spring Security convention of
 * the "ROLE_" prefix.</p>
 *
 * <p><strong>M6 QA Hardening:</strong> The three primary demo roles (TRADER, BROKER_ADMIN,
 * EXCHANGE_OPERATOR) are used consistently by new controllers and services to enforce
 * role-based access control and navigation.</p>
 *
 * @see com.rnexchange.security.SecurityConfiguration
 */
public final class AuthoritiesConstants {

    /** Administrator role with full system access. */
    public static final String ADMIN = "ROLE_ADMIN";

    /** Standard user role with basic authenticated access. */
    public static final String USER = "ROLE_USER";

    /** Anonymous/unauthenticated user role. */
    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    /**
     * Exchange Operator role.
     *
     * <p>Exchange operators have system-wide visibility and control, including:
     * <ul>
     *   <li>Managing brokers (create, activate, suspend)</li>
     *   <li>Controlling trading calendar and holidays</li>
     *   <li>Running/overriding EOD settlements</li>
     *   <li>System-wide monitoring and control</li>
     * </ul>
     * </p>
     *
     * <p>Demo user: {@code exchange_demo}</p>
     */
    public static final String EXCHANGE_OPERATOR = "ROLE_EXCHANGE_OPERATOR";

    /**
     * Broker Admin role.
     *
     * <p>Broker administrators manage traders under their broker, including:
     * <ul>
     *   <li>Managing traders under their broker</li>
     *   <li>Viewing trade blotter and client balances</li>
     *   <li>Posting fund journals (deposits/withdrawals)</li>
     *   <li>Monitoring risk and margin utilization</li>
     *   <li>Initiating EOD for broker scope</li>
     * </ul>
     * </p>
     *
     * <p>Demo user: {@code broker_demo}</p>
     */
    public static final String BROKER_ADMIN = "ROLE_BROKER_ADMIN";

    /**
     * Trader role.
     *
     * <p>Traders can:
     * <ul>
     *   <li>Place orders (Market, Limit, Stop, Stop-Limit)</li>
     *   <li>Manage watchlists and portfolios</li>
     *   <li>View positions, MTM, and P&L</li>
     *   <li>Access ledger and download statements</li>
     * </ul>
     * </p>
     *
     * <p>Demo user: {@code trader_demo}</p>
     */
    public static final String TRADER = "ROLE_TRADER";

    private AuthoritiesConstants() {}
}

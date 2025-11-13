package com.rnexchange.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String EXCHANGE_OPERATOR = "ROLE_EXCHANGE_OPERATOR";

    public static final String BROKER_ADMIN = "ROLE_BROKER_ADMIN";

    public static final String TRADER = "ROLE_TRADER";

    private AuthoritiesConstants() {}
}

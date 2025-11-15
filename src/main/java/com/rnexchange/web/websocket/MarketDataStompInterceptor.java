package com.rnexchange.web.websocket;

import com.rnexchange.service.marketdata.WatchlistAuthorizationService;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MarketDataStompInterceptor implements ChannelInterceptor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(MarketDataStompInterceptor.class);
    private static final int MAX_SUBSCRIPTIONS_PER_SESSION = 50;

    private final JwtDecoder jwtDecoder;
    private final WatchlistAuthorizationService authorizationService;
    private final ConcurrentMap<String, AtomicInteger> subscriptionCounts = new ConcurrentHashMap<>();

    public MarketDataStompInterceptor(JwtDecoder jwtDecoder, WatchlistAuthorizationService authorizationService) {
        this.jwtDecoder = Objects.requireNonNull(jwtDecoder, "jwtDecoder must not be null");
        this.authorizationService = Objects.requireNonNull(authorizationService, "authorizationService must not be null");
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            handleConnect(accessor);
        } else if (command == StompCommand.SUBSCRIBE) {
            handleSubscribe(accessor);
            enforceSubscriptionLimit(accessor);
        } else if (command == StompCommand.UNSUBSCRIBE) {
            decrementSubscriptionCount(accessor);
        } else if (command == StompCommand.DISCONNECT) {
            clearSessionSubscriptions(accessor);
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        if (log.isDebugEnabled()) {
            log.debug("Processing WebSocket CONNECT for session {}", accessor.getSessionId());
        }
        String authorizationHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorizationHeader)) {
            // Some STOMP clients normalize header names to lowercase; fall back to a
            // case-insensitive lookup before rejecting the connection.
            authorizationHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION.toLowerCase(Locale.ROOT));
        }
        if (!StringUtils.hasText(authorizationHeader)) {
            if (log.isDebugEnabled()) {
                log.debug("Available STOMP CONNECT headers for session {}: {}", accessor.getSessionId(), accessor.toNativeHeaderMap());
            }
            log.warn("Rejecting WebSocket CONNECT with missing Authorization header (session={})", accessor.getSessionId());
            throw new AuthenticationCredentialsNotFoundException("WebSocket CONNECT requires Authorization header");
        }
        if (!authorizationHeader.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            log.warn("Rejecting WebSocket CONNECT with non-bearer Authorization header (session={})", accessor.getSessionId());
            throw new AuthenticationCredentialsNotFoundException("Authorization header must use Bearer scheme");
        }
        String token = authorizationHeader.substring(7);
        if (!StringUtils.hasText(token)) {
            log.warn("Rejecting WebSocket CONNECT due to blank bearer token (session={})", accessor.getSessionId());
            throw new AuthenticationCredentialsNotFoundException("Bearer token is empty");
        }
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtException ex) {
            log.warn("Rejecting WebSocket CONNECT due to invalid JWT (session={}): {}", accessor.getSessionId(), ex.getMessage());
            throw new AuthenticationCredentialsNotFoundException("Invalid JWT token", ex);
        }
        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(jwt);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jwt.getSubject(), token, authorities);
        authentication.setDetails(jwt);
        accessor.setUser(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        Authentication authentication = (Authentication) accessor.getUser();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("WebSocket SUBSCRIBE requires authenticated user");
        }
        String destination = accessor.getDestination();
        if (!StringUtils.hasText(destination)) {
            throw new AccessDeniedException("Destination must be provided");
        }
        if (destination.startsWith("/topic/quotes/") || destination.startsWith("/topic/bars/")) {
            String symbol = destination.substring(destination.lastIndexOf('/') + 1);
            if (log.isDebugEnabled()) {
                log.debug("Evaluating subscription for user {} to {}", authentication.getName(), symbol);
            }
            if (!authorizationService.isSymbolAuthorized(authentication.getName(), symbol)) {
                log.warn("Rejecting subscription for user {} to unauthorized symbol {}", authentication.getName(), symbol);
                throw new AccessDeniedException("User not authorized for symbol " + symbol);
            }
        }
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("auth");
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream().filter(StringUtils::hasText).map(SimpleGrantedAuthority::new).toList();
    }

    private void enforceSubscriptionLimit(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        AtomicInteger counter = subscriptionCounts.computeIfAbsent(sessionId, key -> new AtomicInteger());
        int current = counter.incrementAndGet();
        if (current > MAX_SUBSCRIPTIONS_PER_SESSION) {
            counter.decrementAndGet();
            String message = "Subscription limit of %d exceeded for session %s".formatted(MAX_SUBSCRIPTIONS_PER_SESSION, sessionId);
            log.warn(message);
            throw new IllegalStateException(message);
        }
    }

    private void decrementSubscriptionCount(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        AtomicInteger counter = subscriptionCounts.get(sessionId);
        if (counter == null) {
            return;
        }
        int remaining = counter.decrementAndGet();
        if (remaining <= 0) {
            subscriptionCounts.remove(sessionId);
        }
    }

    private void clearSessionSubscriptions(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        if (StringUtils.hasText(sessionId)) {
            subscriptionCounts.remove(sessionId);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

package com.rnexchange.web.websocket;

import com.rnexchange.service.marketdata.WatchlistAuthorizationService;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

    private final JwtDecoder jwtDecoder;
    private final WatchlistAuthorizationService authorizationService;

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
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authorizationHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new AuthenticationCredentialsNotFoundException("WebSocket CONNECT requires Authorization header");
        }
        if (!authorizationHeader.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            throw new AuthenticationCredentialsNotFoundException("Authorization header must use Bearer scheme");
        }
        String token = authorizationHeader.substring(7);
        if (!StringUtils.hasText(token)) {
            throw new AuthenticationCredentialsNotFoundException("Bearer token is empty");
        }
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtException ex) {
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
            if (!authorizationService.isSymbolAuthorized(authentication.getName(), symbol)) {
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

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

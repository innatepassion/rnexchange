package com.rnexchange.service.marketdata;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class WatchlistAuthorizationService {

    private final ConcurrentMap<String, Set<String>> userSymbolPermissions = new ConcurrentHashMap<>();

    public boolean isSymbolAuthorized(String username, String symbol) {
        if (!StringUtils.hasText(symbol)) {
            return false;
        }
        if (!StringUtils.hasText(username)) {
            return false;
        }
        Set<String> allowedSymbols = userSymbolPermissions.get(username);
        if (allowedSymbols == null || allowedSymbols.isEmpty()) {
            return true;
        }
        return allowedSymbols.contains(symbol);
    }

    public void grantSymbols(String username, Collection<String> symbols) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        if (symbols == null || symbols.isEmpty()) {
            userSymbolPermissions.remove(username);
            return;
        }
        Set<String> normalizedSymbols = symbols.stream().filter(StringUtils::hasText).collect(Collectors.toUnmodifiableSet());
        if (normalizedSymbols.isEmpty()) {
            userSymbolPermissions.remove(username);
            return;
        }
        userSymbolPermissions.put(username, normalizedSymbols);
    }

    public void clearPermissions(String username) {
        if (StringUtils.hasText(username)) {
            userSymbolPermissions.remove(username);
        }
    }

    public void reset() {
        userSymbolPermissions.clear();
    }

    public Set<String> getGrantedSymbols(String username) {
        return userSymbolPermissions.getOrDefault(username, Collections.emptySet());
    }
}

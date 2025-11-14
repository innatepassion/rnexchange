package com.rnexchange.service;

import com.rnexchange.domain.Watchlist;
import com.rnexchange.domain.WatchlistItem;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.repository.WatchlistItemRepository;
import com.rnexchange.repository.WatchlistRepository;
import com.rnexchange.security.SecurityUtils;
import com.rnexchange.service.dto.WatchlistDTO;
import com.rnexchange.service.dto.WatchlistSummaryDTO;
import com.rnexchange.service.mapper.WatchlistMapper;
import com.rnexchange.service.marketdata.WatchlistAuthorizationService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Business service for trader watchlists.
 */
@Service
@Transactional
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository watchlistItemRepository;
    private final InstrumentRepository instrumentRepository;
    private final WatchlistMapper watchlistMapper;
    private final WatchlistAuthorizationService authorizationService;

    public WatchlistService(
        WatchlistRepository watchlistRepository,
        WatchlistItemRepository watchlistItemRepository,
        InstrumentRepository instrumentRepository,
        WatchlistMapper watchlistMapper,
        WatchlistAuthorizationService authorizationService
    ) {
        this.watchlistRepository = watchlistRepository;
        this.watchlistItemRepository = watchlistItemRepository;
        this.instrumentRepository = instrumentRepository;
        this.watchlistMapper = watchlistMapper;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<WatchlistSummaryDTO> getCurrentTraderSummaries() {
        String login = currentLogin();
        List<Watchlist> watchlists = watchlistRepository.findAllByTraderProfileUserLoginOrderByNameAsc(login);
        updateAuthorization(login, watchlists);
        return watchlists.stream().map(watchlistMapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public WatchlistDTO getWatchlist(Long id) {
        Watchlist watchlist = loadOwnedWatchlist(id);
        updateAuthorization(watchlist.getTraderProfile().getUser().getLogin());
        return watchlistMapper.toDto(watchlist);
    }

    public WatchlistDTO addSymbol(Long watchlistId, String rawSymbol) {
        Watchlist watchlist = loadOwnedWatchlist(watchlistId);
        String normalizedSymbol = normalizeSymbol(rawSymbol);
        instrumentRepository
            .findOneBySymbol(normalizedSymbol)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol not found: " + normalizedSymbol));
        boolean exists = watchlist.getItems().stream().anyMatch(item -> normalizedSymbol.equals(item.getSymbol()));
        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol already present in watchlist");
        }
        int nextOrder = watchlist
            .getItems()
            .stream()
            .map(WatchlistItem::getSortOrder)
            .filter(order -> order != null)
            .max(Integer::compareTo)
            .orElse(watchlist.getItems().size());
        WatchlistItem item = new WatchlistItem().symbol(normalizedSymbol).sortOrder(nextOrder);
        watchlist.addItem(item);
        watchlistRepository.flush();
        updateAuthorization(watchlist.getTraderProfile().getUser().getLogin());
        return watchlistMapper.toDto(watchlist);
    }

    public WatchlistDTO removeSymbol(Long watchlistId, String rawSymbol) {
        Watchlist watchlist = loadOwnedWatchlist(watchlistId);
        String normalizedSymbol = normalizeSymbol(rawSymbol);
        WatchlistItem item = watchlist
            .getItems()
            .stream()
            .filter(existing -> normalizedSymbol.equals(existing.getSymbol()))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Symbol not found in watchlist"));
        boolean removed = watchlist.getItems().removeIf(existing -> Objects.equals(existing.getId(), item.getId()));
        if (!removed) {
            watchlist.removeItem(item);
        }
        watchlistItemRepository.delete(item);
        renumberSortOrder(watchlist);
        watchlistRepository.flush();
        updateAuthorization(watchlist.getTraderProfile().getUser().getLogin());
        return watchlistMapper.toDto(watchlist);
    }

    private Watchlist loadOwnedWatchlist(Long id) {
        String login = currentLogin();
        return watchlistRepository
            .findByIdAndTraderProfileUserLogin(id, login)
            .orElseGet(() -> {
                watchlistRepository
                    .findById(id)
                    .ifPresent(existing -> {
                        String ownerLogin = existing.getTraderProfile() != null && existing.getTraderProfile().getUser() != null
                            ? existing.getTraderProfile().getUser().getLogin()
                            : null;
                        if (ownerLogin != null && !ownerLogin.equals(login)) {
                            throw new AccessDeniedException("Watchlist not owned by current trader");
                        }
                    });
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Watchlist not found");
            });
    }

    private String currentLogin() {
        return SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new AccessDeniedException("Authenticated trader required for watchlist access"));
    }

    private void updateAuthorization(String login) {
        List<Watchlist> watchlists = watchlistRepository.findAllByTraderProfileUserLoginOrderByNameAsc(login);
        updateAuthorization(login, watchlists);
    }

    private void updateAuthorization(String login, List<Watchlist> watchlists) {
        Set<String> symbols = watchlists
            .stream()
            .flatMap(w -> w.getItems().stream())
            .map(WatchlistItem::getSymbol)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        authorizationService.grantSymbols(login, symbols);
    }

    private static String normalizeSymbol(String symbol) {
        if (symbol == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol must not be blank");
        }
        String trimmed = symbol.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol must not be blank");
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private static void renumberSortOrder(Watchlist watchlist) {
        int index = 0;
        for (WatchlistItem item : watchlist.getItems()) {
            item.setSortOrder(index++);
        }
    }
}

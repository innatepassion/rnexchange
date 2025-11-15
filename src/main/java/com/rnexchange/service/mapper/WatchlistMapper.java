package com.rnexchange.service.mapper;

import com.rnexchange.domain.Watchlist;
import com.rnexchange.domain.WatchlistItem;
import com.rnexchange.service.dto.WatchlistDTO;
import com.rnexchange.service.dto.WatchlistItemDTO;
import com.rnexchange.service.dto.WatchlistSummaryDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper for Watchlist domain objects.
 */
@Component
public class WatchlistMapper {

    private static final Comparator<WatchlistItem> ITEM_ORDER = Comparator.comparing((WatchlistItem item) ->
        item.getSortOrder() == null ? Integer.MAX_VALUE : item.getSortOrder()
    ).thenComparing(WatchlistItem::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    public WatchlistDTO toDto(Watchlist watchlist) {
        if (watchlist == null) {
            return null;
        }
        WatchlistDTO dto = new WatchlistDTO();
        dto.setId(watchlist.getId());
        dto.setName(watchlist.getName());
        dto.setItems(watchlist.getItems().stream().sorted(ITEM_ORDER).map(this::toItemDto).collect(Collectors.toList()));
        return dto;
    }

    public WatchlistSummaryDTO toSummary(Watchlist watchlist) {
        if (watchlist == null) {
            return null;
        }
        WatchlistSummaryDTO summary = new WatchlistSummaryDTO();
        summary.setId(watchlist.getId());
        summary.setName(watchlist.getName());
        List<String> symbols = watchlist
            .getItems()
            .stream()
            .sorted(ITEM_ORDER)
            .map(item -> item.getSymbol() == null ? null : item.getSymbol().toUpperCase(Locale.ROOT))
            .collect(Collectors.toList());
        summary.setSymbols(symbols);
        summary.setSymbolCount(symbols.size());
        return summary;
    }

    private WatchlistItemDTO toItemDto(WatchlistItem item) {
        WatchlistItemDTO dto = new WatchlistItemDTO();
        dto.setId(item.getId());
        dto.setSymbol(item.getSymbol());
        dto.setSortOrder(item.getSortOrder());
        return dto;
    }
}

package com.rnexchange.service.seed;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BaselineSeedVerificationService {

    @PersistenceContext
    private EntityManager entityManager;

    public void assertNoDuplicateInstruments() {
        List<Object[]> duplicates = entityManager
            .createQuery(
                "select i.exchangeCode, i.symbol, count(i) from Instrument i group by i.exchangeCode, i.symbol having count(i) > 1",
                Object[].class
            )
            .getResultList();
        if (!duplicates.isEmpty()) {
            Object[] duplicate = duplicates.get(0);
            String exchange = (String) duplicate[0];
            String symbol = (String) duplicate[1];
            throw new BaselineSeedVerificationException("Detected duplicate instrument " + symbol + " for exchange " + exchange);
        }
    }
}

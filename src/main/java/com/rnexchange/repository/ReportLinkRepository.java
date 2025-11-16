package com.rnexchange.repository;

import com.rnexchange.domain.ReportLink;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportLinkRepository extends JpaRepository<ReportLink, Long> {
    List<ReportLink> findByRefDate(LocalDate refDate);
    List<ReportLink> findByRefDateAndReportType(LocalDate refDate, String reportType);
}

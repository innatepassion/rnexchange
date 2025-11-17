package com.rnexchange.service.settlement;

/**
 * Utility class for generating HTML reports (statements and summaries).
 * Provides common HTML structure, CSS, and disclaimer text to reduce duplication.
 */
public final class HtmlReportUtils {

    private HtmlReportUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Common CSS styles for settlement reports.
     */
    public static final String COMMON_CSS =
        """
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { color: #333; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .disclaimer { background-color: #fff3cd; padding: 10px; margin: 20px 0; border-left: 4px solid #ffc107; }
        .totals { font-weight: bold; background-color: #e8f5e9; }
        """;

    /**
     * Simulated environment disclaimer HTML.
     * M6 User Story 3, Task T036A: Prominent disclaimer consistent with FR-018 and Educational Transparency rules.
     */
    public static final String SIMULATED_ENVIRONMENT_DISCLAIMER =
        """
        <div class="disclaimer" style="background-color: #fff3cd; padding: 15px; margin: 20px 0; border-left: 4px solid #ffc107; font-weight: bold;">
        <strong>⚠️ This is a simulated environment — not real trading or money</strong><br/>
        This statement is generated from simulated EOD settlement data. Prices and P&L are from internal mock feeds and are for training purposes only.
        No real money is involved in this simulation.
        </div>
        """;

    /**
     * Generate HTML document header with title and CSS.
     *
     * @param title the page title
     * @return HTML header string
     */
    public static String htmlHeader(String title) {
        return String.format(
            """
            <!DOCTYPE html>
            <html>
            <head>
            <title>%s</title>
            <style>
            %s
            </style>
            </head>
            <body>
            """,
            title,
            COMMON_CSS
        );
    }

    /**
     * Generate HTML document footer.
     *
     * @return HTML footer string
     */
    public static String htmlFooter() {
        return """
        </body>
        </html>
        """;
    }

    /**
     * Generate a table row with header and value.
     *
     * @param header the header text
     * @param value the value (will be converted to string)
     * @param isBold whether to make the value bold
     * @return HTML table row string
     */
    public static String tableRow(String header, Object value, boolean isBold) {
        String valueStr = value != null ? value.toString() : "";
        String valueHtml = isBold ? "<strong>" + valueStr + "</strong>" : valueStr;
        return String.format("<tr><th>%s</th><td>%s</td></tr>\n", header, valueHtml);
    }

    /**
     * Generate a table row with header and value (not bold).
     *
     * @param header the header text
     * @param value the value (will be converted to string)
     * @return HTML table row string
     */
    public static String tableRow(String header, Object value) {
        return tableRow(header, value, false);
    }
}

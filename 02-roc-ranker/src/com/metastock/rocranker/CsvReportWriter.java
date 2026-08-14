package com.metastock.rocranker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDate;

/**
 * Writes the report rows to a comma-separated CSV file, e.g. C:\meta\dr\report\DRYTD.csv
 * All 12 (or up to current) monthly Top-N blocks are written stacked in one file, in order.
 */
public class CsvReportWriter {

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final String[] HEADERS = { "Month","No", "Symbol","YTD(%)", "ForwardMonthROC(%)",
			"BeginOfMonthPrice","NextBeginOfMonthPrice",
			"BeginOfYearDate", "BeginOfYearPrice", "LatestPrice" };

	public void write(String path, List<RocReportRow> rows) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(String.join(",", HEADERS)).append("\n");

        for (RocReportRow r : rows) {
			sb.append(r.getMonth()).append(",")
					.append(r.getNo()).append(",")
					.append(r.getSymbol()).append(",")
					.append(NumberFormatUtil.formatPercent(r.getYtdPercent())).append(",")
					.append(NumberFormatUtil.formatPercent(r.getForwardMonthRocPercent())).append(",")
					.append(NumberFormatUtil.formatPrice(r.getBeginOfMonthPrice())).append(",")
					.append(NumberFormatUtil.formatPrice(r.getNextBeginOfMonthPrice())).append(",")
					.append(r.getBeginOfYearDate().format(DATE_FMT)).append(",")
					.append(NumberFormatUtil.formatPrice(r.getBeginOfYearPrice())).append(",")
					.append(NumberFormatUtil.formatPrice(r.getLatestPrice()))
					.append("\n");
		}

		LocalDate lastDate = rows.isEmpty() ? LocalDate.now() : rows.getLast().getLatestDate();
		sb.append("\n").append(lastDate.format(DATE_FMT));

		Path p = Path.of(path);
		if (p.getParent() != null) {
			Files.createDirectories(p.getParent());
		}
		Files.writeString(p, sb.toString());
	}
}

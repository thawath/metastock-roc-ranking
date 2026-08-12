package com.metastock.rocranker;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Prints the latest month's Top-N rows to the command prompt, padded to a
 * fixed column width so columns line up vertically.
 */
public class ConsoleReportPrinter {

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final int COL_WIDTH = 8;

	private static final String[] HEADERS = { "No", "Month", "Symbol", "BeginYrDt", "BeginYrPx", "YTD%", "FwdROC%",
			"NextMoPx", "BeginMoPx", "LastPx" };

	public void printLatestMonth(List<RocReportRow> allRows) {
		if (allRows == null || allRows.isEmpty()) {
			System.out.println("No data to display.");
			return;
		}

		String latestMonth = allRows.get(allRows.size() - 1).getMonth();
		List<RocReportRow> latest = new ArrayList<>();
		for (RocReportRow r : allRows) {
			if (r.getMonth().equals(latestMonth)) {
				latest.add(r);
			}
		}

		printRow(HEADERS);
		for (RocReportRow r : latest) {
			printRow(new String[] { String.valueOf(r.getNo()), r.getMonth(), r.getSymbol(),
					r.getBeginOfYearDate().format(DATE_FMT), NumberFormatUtil.formatPrice(r.getBeginOfYearPrice()),
					NumberFormatUtil.formatPercent(r.getYtdPercent()),
					NumberFormatUtil.formatPercent(r.getForwardMonthRocPercent()),
					NumberFormatUtil.formatPrice(r.getNextBeginOfMonthPrice()),
					NumberFormatUtil.formatPrice(r.getBeginOfMonthPrice()),
					NumberFormatUtil.formatPrice(r.getLatestPrice()) });
		}
	}

	private void printRow(String[] cols) {
		StringBuilder sb = new StringBuilder();
		for (String c : cols) {
			sb.append(String.format(Locale.US, "%-" + COL_WIDTH + "s", c));
		}
		System.out.println(sb.toString());
	}
}

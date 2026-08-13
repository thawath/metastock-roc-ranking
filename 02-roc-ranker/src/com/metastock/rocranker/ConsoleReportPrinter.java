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
	private static final int COL_WIDTH = 12;

	private static final String[] HEADERS = { "Symbol", "BeginYrDt", "BeginYrPx", "LastPx","YTD%", "FwdROC%"};
			//"NextMoPx", "BeginMoPx", "LastPx" };

	public void printLatestMonth(List<RocReportRow> allRows) {
		if (allRows == null || allRows.isEmpty()) {
			System.out.println("No data to display.");
			return;
		}

		String latestMonth = allRows.getLast().getMonth();
		List<RocReportRow> latest = new ArrayList<>();
		for (RocReportRow r : allRows) {
			if (r.getMonth().equals(latestMonth)) {
				latest.add(r);
			}
		}

		printRow("No",HEADERS);
		for (RocReportRow r : latest) {
			printRow(String.valueOf(r.getNo()),new String[] { r.getSymbol(),
					r.getBeginOfYearDate().format(DATE_FMT), NumberFormatUtil.formatPrice(r.getBeginOfYearPrice()),
					NumberFormatUtil.formatPrice(r.getLatestPrice()),
					NumberFormatUtil.formatPercent(r.getYtdPercent()),
					NumberFormatUtil.formatPercent(r.getForwardMonthRocPercent())
					//NumberFormatUtil.formatPrice(r.getNextBeginOfMonthPrice()),
					//NumberFormatUtil.formatPrice(r.getBeginOfMonthPrice()),
					});
		}
		System.out.println("Month:"+latest.getFirst().getMonth());
		System.out.println("Date:"+latest.getFirst().getLatestDate().format(DATE_FMT));
	}

	private void printRow(String no,String[] cols) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Locale.US, "%-4s", no));
		for (String c : cols) {
			sb.append(String.format(Locale.US, "%-" + COL_WIDTH + "s", c));
		}
		System.out.println(sb);
	}
}

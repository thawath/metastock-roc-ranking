package com.metastock.rocranker;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Orchestrates the ROC computation:
 *  1. Uses a reference symbol (e.g. GOOG80, which has the longest continuous
 *     history) to determine the current year and the last month that has
 *     started trading -> defines how many monthly report blocks to produce.
 *  2. For every DR symbol and every month (Jan..latest), computes:
 *       YTD%            = beginOfMonth.close / beginOfYear.close  - 1
 *       forwardMonthROC%= nextBeginOfMonth.close / beginOfMonth.close - 1
 *     (nextBeginOfMonth falls back to the latest available close when the
 *      following month hasn't started yet - i.e. the current/latest month.)
 *  3. Sorts each month's rows by YTD% descending and keeps the top N.
 */
public class RocReportGenerator {

	private final MetaStock metaStock;
	private final RocCalculator calculator = new RocCalculator();
	private final String referenceSymbol;
	private final int topN;

	public RocReportGenerator(MetaStock metaStock, String referenceSymbol, int topN) {
		this.metaStock = metaStock;
		this.referenceSymbol = referenceSymbol;
		this.topN = topN;
	}

	public List<RocReportRow> generate(List<String> symbols) {
		List<StockRecord> refData = metaStock.createStockList(referenceSymbol);
		if (refData == null || refData.isEmpty()) {
			throw new IllegalStateException(
					"No data found for reference symbol '" + referenceSymbol + "'. Check MASTER/XMASTER loading.");
		}
		LocalDate refLatestDate = refData.getLast().getDate();
		int year = refLatestDate.getYear();
		int lastMonthNum = refLatestDate.getMonthValue();

		// Pre-load each symbol's data + derived boundaries once (avoid re-reading disk per month).
		Map<String, List<StockRecord>> dataBySymbol = new HashMap<>();
		Map<String, Map<YearMonth, RocCalculator.PricePoint>> boundariesBySymbol = new HashMap<>();
		Map<String, RocCalculator.PricePoint> beginOfYearBySymbol = new HashMap<>();

		for (String symbol : symbols) {
			List<StockRecord> data = metaStock.createStockList(symbol);
			if (data == null || data.isEmpty()) {
				System.err.println("[warn] no data found for symbol: " + symbol + " (skipped)");
				continue;
			}
			dataBySymbol.put(symbol, data);
			boundariesBySymbol.put(symbol, calculator.buildMonthBoundaries(data));
			RocCalculator.PricePoint boy = calculator.findBeginOfYear(data, year);
			if (boy != null) {
				beginOfYearBySymbol.put(symbol, boy);
			}
		}

		List<RocReportRow> allRows = new ArrayList<>();

		// เริ่มคำนวณจากเดือนกุมภาพันธ์ (m=2) เพราะข้อมูล January (m=1) ให้ YTD% = 0 เสมอ
		// (begin-of-month ของเดือนแรกก็คือ begin-of-year ตัวเดียวกัน จึงไม่มีความหมายให้แสดงใน report)
		for (int m = 2; m <= lastMonthNum; m++) {
			YearMonth ym = YearMonth.of(year, m);
			List<RocReportRow> monthRows = new ArrayList<>();

			for (String symbol : symbols) {
				List<StockRecord> data = dataBySymbol.get(symbol);
				if (data == null) {
					continue;
				}
				RocCalculator.PricePoint boy = beginOfYearBySymbol.get(symbol);
				if (boy == null) {
					continue; // symbol has no data at all this year
				}

				Map<YearMonth, RocCalculator.PricePoint> boundaries = boundariesBySymbol.get(symbol);
				RocCalculator.PricePoint bom = boundaries.get(ym);
				if (bom == null) {
					continue; // symbol not yet listed / no trades in this month
				}

				StockRecord latestRec = calculator.latest(data);

				YearMonth nextYm = ym.plusMonths(1);
				RocCalculator.PricePoint nextBom = boundaries.get(nextYm);
				double nextPrice;
				if (nextBom != null) {
					nextPrice = nextBom.close;
				} else {
					// next month hasn't started trading yet -> use latest available close
					nextPrice = latestRec.getClose();
				}

				double ytdPercent = (bom.close / boy.close - 1.0) * 100.0;
				double forwardRocPercent = (nextPrice / bom.close - 1.0) * 100.0;

				RocReportRow row = new RocReportRow();
				row.setMonth(Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
				row.setSymbol(symbol);
				row.setBeginOfYearDate(boy.date);
				row.setBeginOfYearPrice(boy.close);
				row.setYtdPercent(ytdPercent);
				row.setForwardMonthRocPercent(forwardRocPercent);
				row.setNextBeginOfMonthPrice(nextPrice);
				row.setBeginOfMonthPrice(bom.close);
				row.setLatestPrice(latestRec.getClose());
				row.setLatestDate(latestRec.getDate());
				monthRows.add(row);
			}

			monthRows.sort((a, b) -> Double.compare(b.getYtdPercent(), a.getYtdPercent()));

			int limit = Math.min(topN, monthRows.size());
			for (int i = 0; i < limit; i++) {
				monthRows.get(i).setNo(i + 1);
			}
			allRows.addAll(monthRows.subList(0, limit));
		}

		return allRows;
	}
}

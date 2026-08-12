package com.metastock.rocranker;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes the "begin of year" and "begin of month" reference prices used by
 * the YTD and forward-month ROC formulas.
 *
 * Design note (handles both fallback rules described by the business logic
 * with a single pass, instead of two separate branches):
 *
 * A StockRecord list for one symbol is already sorted ascending by date
 * (guaranteed by MetaStock#createStockList). For a given year/month, the
 * FIRST record found in that list whose date falls in that year/month is,
 * by definition:
 *   - the first real trading day of that year/month, IF the symbol already
 *     had data before/at that boundary (method 1: normal first business day)
 *   - OR the symbol's very first available record, IF the symbol only
 *     started trading partway through that year/month (method 2: fallback
 *     to first available data, e.g. MICRON01 starting 2026-02-09)
 * Both business rules therefore fall out of the same simple scan.
 */
public class RocCalculator {
	private static final java.time.format.DateTimeFormatter TF = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");

	/** A reference price anchored to a specific date (begin of year / begin of month). */
	public static class PricePoint {
		public final LocalDate date;
		public final double close;

		public PricePoint(LocalDate date, double close) {
			this.date = date;
			this.close = close;
		}
	}

	/**
	 * Builds a map of YearMonth -> first available trading record (begin-of-month price)
	 * for every month present in the symbol's data, scanning once in ascending date order.
	 */
	public Map<YearMonth, PricePoint> buildMonthBoundaries(List<StockRecord> data) {
		Map<YearMonth, PricePoint> map = new LinkedHashMap<>();
		YearMonth prevYm = null;
		for (StockRecord r : data) {
			YearMonth ym = YearMonth.from(r.getDate());
			if (!ym.equals(prevYm)) {
				map.put(ym, new PricePoint(r.getDate(), r.getClose()));
				prevYm = ym;
			}
		}
		return map;
	}

	/**
	 * Finds the begin-of-year price for the given year: the first record (in ascending
	 * date order) whose year equals the target year. Returns null if the symbol has no
	 * data at all in that year.
	 */
	public PricePoint findBeginOfYear(List<StockRecord> data, int year) {
		for (StockRecord r : data) {
			if(r.getSymbol().equals("ADVANT19"))
				System.out.println("[" + java.time.LocalTime.now().format(TF) + "] symbol = " + r.getSymbol()+" date="+r.getDate()+" year="+year);
			if (r.getDate().getYear() == year) {
				return new PricePoint(r.getDate(), r.getClose());
			}
		}
		return null;
	}

	/** Most recent (latest) record in the list, or null if empty. */
	public StockRecord latest(List<StockRecord> data) {
		return data.isEmpty() ? null : data.get(data.size() - 1);
	}
}

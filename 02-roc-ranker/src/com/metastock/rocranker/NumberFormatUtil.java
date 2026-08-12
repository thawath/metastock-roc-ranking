package com.metastock.rocranker;

import java.util.Locale;

/**
 * Formatting rules for the report:
 *  - Price / plain numeric columns: 2 decimal places, but if the value is a
 *    whole number (e.g. 225.00) the decimal part is dropped -> "225".
 *  - Percent columns (YTD%, ForwardMonthROC%): always show exactly 2 decimals.
 */
public final class NumberFormatUtil {

	private NumberFormatUtil() {
	}

	public static String formatPrice(double value) {
		double rounded = Math.round(value * 100.0) / 100.0;
		if (rounded == Math.floor(rounded) && !Double.isInfinite(rounded)) {
			return String.valueOf((long) rounded);
		}
		return String.format(Locale.US, "%.2f", rounded);
	}

	public static String formatPercent(double value) {
		return String.format(Locale.US, "%.2f", value);
	}
}

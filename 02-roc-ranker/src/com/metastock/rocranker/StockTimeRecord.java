package com.metastock.rocranker;

import java.time.LocalDateTime;

/**
 * Bean representing an intraday record (kept for completeness / future use;
 * this module only consumes daily records via StockRecord).
 */
public class StockTimeRecord extends StockRecord {

	private LocalDateTime datetime;
	private String period;

	public StockTimeRecord() {
		this.setSymbol("S50");
		this.setDatetime(LocalDateTime.now());
		this.setPeriod("H"); // H = hour, D = day
		this.setOpen(0);
		this.setHigh(0);
		this.setLow(0);
		this.setClose(0);
	}

	public StockTimeRecord(String symbol, LocalDateTime localdatetime, String period, double open, double high,
			double low, double close) {
		this.setSymbol(symbol);
		this.setDatetime(localdatetime);
		this.setPeriod(period);
		this.setOpen(open);
		this.setHigh(high);
		this.setLow(low);
		this.setClose(close);
	}

	public LocalDateTime getDatetime() {
		return datetime;
	}

	public void setDatetime(LocalDateTime datetime) {
		this.datetime = datetime;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

}

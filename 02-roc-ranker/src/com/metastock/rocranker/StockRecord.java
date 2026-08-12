package com.metastock.rocranker;

import java.time.LocalDate;

/**
 * Bean representing one daily OHLC record read from a MetaStock .DAT/.MWD file.
 * (Copied from the previous module's MetaStock reader, package renamed only.)
 */
public class StockRecord {

	private String symbol;
	private LocalDate date;
	private double open;
	private double high;
	private double low;
	private double close;

	public StockRecord() {
		this.setSymbol("AOT");
		this.setDate(LocalDate.now());
		this.setOpen(0);
		this.setHigh(0);
		this.setLow(0);
		this.setClose(0);
	}

	public StockRecord(String symbol, LocalDate localdate, double open, double high, double low, double close) {
		this.setSymbol(symbol);
		this.setDate(localdate);
		this.setOpen(open);
		this.setHigh(high);
		this.setLow(low);
		this.setClose(close);
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}
}

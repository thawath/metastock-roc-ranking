package com.metastock.rocranker;

import java.time.LocalDate;

/**
 * One output row of the DRYTD report:
 * No, Month, Symbol, BeginOfYearDate, BeginOfYearPrice, YTD%, ForwardMonthROC%,
 * NextBeginOfMonthPrice, BeginOfMonthPrice, LatestPrice
 */
public class RocReportRow {

	private int no;
	private String month;
	private String symbol;
	private LocalDate beginOfYearDate;
	private double beginOfYearPrice;
	private double ytdPercent;
	private double forwardMonthRocPercent;
	private double nextBeginOfMonthPrice;
	private double beginOfMonthPrice;
	private double latestPrice;

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public LocalDate getBeginOfYearDate() {
		return beginOfYearDate;
	}

	public void setBeginOfYearDate(LocalDate beginOfYearDate) {
		this.beginOfYearDate = beginOfYearDate;
	}

	public double getBeginOfYearPrice() {
		return beginOfYearPrice;
	}

	public void setBeginOfYearPrice(double beginOfYearPrice) {
		this.beginOfYearPrice = beginOfYearPrice;
	}

	public double getYtdPercent() {
		return ytdPercent;
	}

	public void setYtdPercent(double ytdPercent) {
		this.ytdPercent = ytdPercent;
	}

	public double getForwardMonthRocPercent() {
		return forwardMonthRocPercent;
	}

	public void setForwardMonthRocPercent(double forwardMonthRocPercent) {
		this.forwardMonthRocPercent = forwardMonthRocPercent;
	}

	public double getNextBeginOfMonthPrice() {
		return nextBeginOfMonthPrice;
	}

	public void setNextBeginOfMonthPrice(double nextBeginOfMonthPrice) {
		this.nextBeginOfMonthPrice = nextBeginOfMonthPrice;
	}

	public double getBeginOfMonthPrice() {
		return beginOfMonthPrice;
	}

	public void setBeginOfMonthPrice(double beginOfMonthPrice) {
		this.beginOfMonthPrice = beginOfMonthPrice;
	}

	public double getLatestPrice() {
		return latestPrice;
	}

	public void setLatestPrice(double latestPrice) {
		this.latestPrice = latestPrice;
	}
}

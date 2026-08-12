package com.metastock.rocranker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Hashtable;

public class TestMetaStock {

	public static void main(String[] args) {
		String metaStockPath = "C:\\MetaStockData";
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusYears(11);
		//master();
		
		// SET50 day

		MetaStock meta = new MetaStock(metaStockPath, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31"));
		Hashtable<String, String> masterIndex = meta.loadMaster();
		String symbol = "ADVANT19";
		ArrayList<StockRecord> stockListMeta= meta.createStockList(symbol);
		for(int i=0;i<stockListMeta.size();i++) {
			print(stockListMeta.get(i).getDate()+" close:"+stockListMeta.get(i).getClose());
		}
		meta.printFirstLastData(symbol);		

	}
	

	public static void print(String st) {
		System.out.println(st);
	}

}

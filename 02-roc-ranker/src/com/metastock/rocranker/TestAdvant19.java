package com.metastock.rocranker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * TestAdvant19
 *
 * เจาะจง debug เฉพาะ symbol "ADVANT19" เพื่อหาสาเหตุที่ report ผิด
 * (record จริงควรเริ่ม 19/01/2026 มี ~132 record แต่ตอนรัน report ได้ 2320 record
 *  ช่วง 2012-2024 ซึ่งไม่ตรงกับข้อมูลจริง)
 *
 * พิมพ์:
 *  - masterIndex.get("ADVANT19") -> ชื่อไฟล์ที่ระบบ map ให้ symbol นี้
 *  - จำนวน record ทั้งหมดที่อ่านได้
 *  - วันที่ + ราคาปิดของ record แรก และ record สุดท้าย
 *  - ตัวอย่าง record 5 ตัวแรก และ 5 ตัวสุดท้าย (ไว้ตรวจสอบความต่อเนื่องของวันที่)
 *
 * แก้ METASTOCK_DIR ด้านล่าง หรือส่งเป็น command-line argument ตัวแรก
 */
public class TestAdvant19 {

	private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final String DEFAULT_METASTOCK_DIR = "C:\\MetaStockData";
	private static final String TARGET_SYMBOL = "ADVANT19";

	public static void main(String[] args) {
		String metaStockDir = args.length > 0 ? args[0] : DEFAULT_METASTOCK_DIR;

		log("MetaStock dir = " + metaStockDir);
		log("Target symbol = " + TARGET_SYMBOL);

		// ใช้ startDate กว้างสุด ให้ครอบคลุมทุก record ที่มีจริง
		LocalDate startDate = LocalDate.of(2000, 1, 1);
		LocalDate endDate = LocalDate.now();

		MetaStock meta = new MetaStock(metaStockDir, startDate, endDate);

		log("Calling loadMaster() ...");
		Hashtable<String, String> masterIndex = meta.loadMaster();
		log("loadMaster() done. Total entries in masterIndex = " + masterIndex.size());

		// 1) เช็คว่า symbol นี้ map ไปไฟล์ชื่ออะไร
		String mappedFile = masterIndex.get(TARGET_SYMBOL);
		log("masterIndex.get(\"" + TARGET_SYMBOL + "\") = " + mappedFile);

		if (mappedFile == null) {
			log("!! ไม่พบ " + TARGET_SYMBOL + " ใน masterIndex เลย - เช็คว่าชื่อสะกดตรงกับใน MASTER/XMASTER ไหม (case-sensitive)");
			return;
		}

		// 2) เช็คว่ามี symbol อื่นที่ mapไปไฟล์เดียวกันหรือไม่ (บอกโอกาสชนกันของ index)
		int sameFileCount = 0;
		for (String key : masterIndex.keySet()) {
			if (mappedFile.equals(masterIndex.get(key))) {
				sameFileCount++;
				if (sameFileCount <= 5) {
					log("   symbol ที่ map ไปไฟล์เดียวกัน (" + mappedFile + ") : " + key);
				}
			}
		}
		log("จำนวน symbol ที่ map ไปไฟล์เดียวกับ " + TARGET_SYMBOL + " (" + mappedFile + ") = " + sameFileCount
				+ (sameFileCount > 1 ? "  <-- ผิดปกติ ควรมีแค่ 1" : ""));

		// 3) ดึงข้อมูลจริง
		log("Calling createStockList(\"" + TARGET_SYMBOL + "\") ...");
		ArrayList<StockRecord> data = meta.createStockList(TARGET_SYMBOL);
		log("createStockList() done. record count = " + data.size());

		if (data.isEmpty()) {
			log("!! ไม่มี record เลย");
			return;
		}

		StockRecord first = data.get(0);
		StockRecord last = data.get(data.size() - 1);

		log("First record : date=" + first.getDate() + " close=" + first.getClose() + " open=" + first.getOpen()
				+ " high=" + first.getHigh() + " low=" + first.getLow());
		log("Last  record : date=" + last.getDate() + " close=" + last.getClose() + " open=" + last.getOpen()
				+ " high=" + last.getHigh() + " low=" + last.getLow());

		// 4) print ตัวอย่างต้น/ท้าย 5 record เพื่อดูความต่อเนื่องของวันที่ (เช็คว่าวัน jump แปลกๆ ตรงไหน)
		log("---- First 5 records ----");
		for (int i = 0; i < Math.min(5, data.size()); i++) {
			StockRecord r = data.get(i);
			log("  [" + i + "] date=" + r.getDate() + " close=" + r.getClose());
		}

		log("---- Last 5 records ----");
		int n = data.size();
		for (int i = Math.max(0, n - 5); i < n; i++) {
			StockRecord r = data.get(i);
			log("  [" + i + "] date=" + r.getDate() + " close=" + r.getClose());
		}

		// 5) เช็คว่ามีการ "กระโดดปี" ผิดปกติระหว่าง record หรือไม่ (บอกจุดที่ไฟล์อาจมีข้อมูลปนกัน)
		log("---- Scanning for abnormal date jumps (> 30 days between consecutive records) ----");
		int jumpCount = 0;
		for (int i = 1; i < data.size(); i++) {
			LocalDate prevDate = data.get(i - 1).getDate();
			LocalDate curDate = data.get(i).getDate();
			long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(prevDate, curDate);
			if (daysBetween > 30 || daysBetween < 0) {
				jumpCount++;
				if (jumpCount <= 20) {
					log("  jump at index " + i + " : " + prevDate + " -> " + curDate + " (" + daysBetween + " days)");
				}
			}
		}
		log("Total abnormal jumps found = " + jumpCount);

		log("Done.");
	}

	private static void log(String msg) {
		System.out.println("[" + LocalTime.now().format(TF) + "] " + msg);
	}
}

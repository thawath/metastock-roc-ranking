package com.metastock.rocranker;
//create date 04 April 2023

//purpose read meta stock to stock record list

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;


public class MetaStock {

	private String dir;
	private LocalDate startDate;
	private LocalDate endDate;

	ArrayList<StockRecord> stockList;
	ArrayList<StockTimeRecord> stockTimeList;
	Hashtable<String, String> masterIndex;
	private ArrayList<HeaderMetaStock> headerList;

	public MetaStock(String dir, LocalDate start, LocalDate end) {
		this.setDir(dir);
		this.startDate = start;
		this.endDate = end;
	}

	/**
	 * Puts a symbol -> full-file-path mapping into masterIndex, warning if the
	 * same symbol name was already mapped to a DIFFERENT physical file (a true
	 * duplicate-name collision across MASTER/XMASTER files, as opposed to the
	 * file-number-recycling collision this fix already resolves).
	 */
	private void putMasterEntry(String name, String fullPath) {
		String existing = masterIndex.get(name);
		if (existing != null && !existing.equals(fullPath)) {
			System.err.println("[warn] duplicate symbol name '" + name + "' found in multiple MASTER/XMASTER files: "
					+ existing + "  ->  overwritten by  ->  " + fullPath);
		}
		masterIndex.put(name, fullPath);
	}

	public Hashtable<String, String> loadMaster() {
		masterIndex = new Hashtable<String, String>();
		setHeaderList(new ArrayList<HeaderMetaStock>());
		HeaderMetaStock header;
		int headercount = 0;
		File masterFile = null;
		DataInputStream in = null;
		File emasterFile = null;
		DataInputStream ein = null;

		int fileNumber;
		String symbol;
		String name;
		Date beginDate;
		Date beginDate2;
		Date endDate;

		// Master
		try {
			masterFile = new File(getDir(), "MASTER");

			in = new DataInputStream(new FileInputStream(masterFile));
			headercount = in.readUnsignedByte();
			in.skip(52);
			// System.out.println("header:" + headercount);
			try {
				while (true) {
					fileNumber = in.readUnsignedByte(); // 0-0 (1)
					in.skip(6); // 1-6 (6)
					name = readASCIIString(16, in); // 7-22 (16)
					//System.out.println(name);
					in.skip(2); // 23-24 (2)
					beginDate = readFloatDate(in); // 25-28 (4)
					endDate = readFloatDate(in); // 29-32 (4)
					in.skip(3); // 33-35 (3)
					symbol = readASCIIString(14, in); // 36-49 (14)
					in.skip(3); // 50-52(3)
					// FIX: เก็บ path เต็ม (root dir + filename) ไม่ใช่แค่ชื่อไฟล์เปล่าๆ
					// เพราะ fileNumber ("F81.DAT") ซ้ำกันได้ข้าม subdirectory
					putMasterEntry(name.trim(), new File(getDir(), "F" + fileNumber + ".DAT").getPath());

					header = new HeaderMetaStock();
					header.setName(name.trim());
					header.setBeginDate(beginDate);
					header.setEndDate(endDate);
					header.setFilename("F" + fileNumber + ".DAT");
					header.setMaster("MASTER");
					getHeaderList().add(header);

					// System.out.println(name + ":" +"F"+ fileNumber+".DAT");

				}
			} catch (EOFException eof) {
				in.close();
			}

			// EMASTER
/*
			emasterFile = new File(dir, "EMASTER");
			ein = new DataInputStream(new FileInputStream(emasterFile));
			headercount = ein.read();
			ein.skip(191);
			float beginDateShort = 0, lastDividend = 0, lastDividendAdjRate = 0; //
			System.out.println("header:" + headercount);
			try {
				while (true) {
					ein.skip(2); // 0-1 (2)
					fileNumber = ein.readUnsignedByte(); // 2-2 (1)
					ein.skip(8); // 3-10 (8)
					symbol = readASCIIString(14, ein);// 11-24 (14)
					ein.skip(7); // 25-31 (7)
					name = readASCIIString(16, ein); // 32-47 (16)
					ein.skip(16); // 48-63 (16)
					beginDateShort = ein.readFloat(); // 64-67 (4)
					ein.skip(4); // 68-71 (4)
					endDate = readFloatDate(ein); // 72-75 (4)
					ein.skip(50); // 76-125(50)
					beginDate = readFloatDate(ein); // 126-129 (4)
					ein.skip(1); // 130-130 (1)
					lastDividend = ein.readFloat(); // 131-134 (4)
					lastDividendAdjRate = ein.readFloat();// 135-138 (4)
					ein.skip(53); //
					System.out.println(symbol + ":" + fileNumber);
				}
			} catch (EOFException eof) {
				ein.close();
			}
*/
			// XMASTER
			File xmasterFile = new File(getDir(), "XMASTER");
			DataInputStream xin = new DataInputStream(new FileInputStream(xmasterFile));
			xin.skip(10);
			headercount = xin.readUnsignedShort();
			xin.skip(138);
			// System.out.println("header:" + headercount);
			try {
				while (true) {
					xin.skip(1); // 0-0 (1)
					symbol = readASCIIString(15, xin); // 1-15 (15)
					name = readASCIIString(46, xin); // 16-61 (46)
					//System.out.println(name);
					char type = (char) xin.readUnsignedByte(); // 62-62 (1)
					xin.skip(2); // 63-64 (2)
					fileNumber = readUnsignedShort(xin); // 65-66 (2)
					// fileNumber = xin.readUnsignedByte(); // 65-66 (2)
					xin.skip(13); // 67-79 (13)
					Date endDate2 = readIntegerDate(xin); // 80-83 (4)
					xin.skip(20); // 84-103(20)
					beginDate = readIntegerDate(xin); // 104-107 (4)
					beginDate2 = readIntegerDate(xin); // 108-111 (4)
					xin.skip(4); // 112-115 (4)
					endDate = readIntegerDate(xin); // 116-119 (4)
					xin.skip(30); // 120-149(30)
					// FIX: เก็บ path เต็ม (root dir + filename) เช่นเดียวกับ MASTER ด้านบน
					putMasterEntry(name, new File(getDir(), "F" + fileNumber + ".MWD").getPath());

					header = new HeaderMetaStock();
					header.setName(name.trim());
					header.setBeginDate(beginDate);
					header.setEndDate(endDate);
					header.setFilename("F" + fileNumber + ".MWD");
					header.setMaster("XMASTER");
					getHeaderList().add(header);

					// System.out.println(symbol + ":" +"F"+ fileNumber+".MWD");
				}
			} catch (EOFException eof) {
				// eof.printStackTrace();
			}
			xin.close();

		} catch (FileNotFoundException fe) {
		} catch (IOException io) {
		}
		
		// Create a File object for the directory
		File directory = new File(dir);
		File[] subdirectories = directory.listFiles(File::isDirectory);
		String subName;

		for (File subdir : subdirectories) {
			subName = subdir.getName();			
			if (!subName.equals("MSSmart")) {
				//print("subName:"+subName);
				subName=dir+"\\"+subName;
				//print(subName);
				// Master sub
				try {
					masterFile = new File(subName, "MASTER");

					in = new DataInputStream(new FileInputStream(masterFile));
					// headercount =
					in.readUnsignedByte();
					in.skip(52);
					// System.out.println("header:" + headercount);
					try {
						while (true) {
							fileNumber = in.readUnsignedByte(); // 0-0 (1)
							in.skip(6); // 1-6 (6)
							name = readASCIIString(16, in); // 7-22 (16)
							//if(name.startsWith("BA")) {
						//		print(name+" F:"+fileNumber);								
						//	}							
							in.skip(2); // 23-24 (2)
							beginDate = readFloatDate(in); // 25-28 (4)
							endDate = readFloatDate(in); // 29-32 (4)
							in.skip(3); // 33-35 (3)
							// symbol =
							readASCIIString(14, in); // 36-49 (14)
							in.skip(3); // 50-52(3)
							// FIX: เก็บ path เต็มของ subdirectory นี้ (subName) ไม่ใช่ root dir
							// เพราะ fileNumber ใน subdirectory นี้ ซ้ำกับ fileNumber ที่ root/subdir อื่นได้
							putMasterEntry(name.trim(), new File(subName, "F" + fileNumber + ".DAT").getPath());

							header = new HeaderMetaStock();
							header.setName(name.trim());
							header.setBeginDate(beginDate);
							header.setEndDate(endDate);
							header.setFilename("F" + fileNumber + ".DAT");
							header.setMaster("MASTER");
							getHeaderList().add(header);

							// System.out.println(name + ":" +"F"+ fileNumber+".DAT");

						}
					} catch (EOFException eof) {
						in.close();
					}

					// XMASTER
					File xmasterFile = new File(subName, "XMASTER");
					DataInputStream xin = new DataInputStream(new FileInputStream(xmasterFile));
					xin.skip(10);
					// headercount =
					xin.readUnsignedShort();
					xin.skip(138);
					// System.out.println("header:" + headercount);
					try {
						while (true) {
							xin.skip(1); // 0-0 (1)
							// symbol =
							readASCIIString(15, xin); // 1-15 (15)
							name = readASCIIString(46, xin); // 16-61 (46)
							System.out.println(name);
							// char type = (char)
							xin.readUnsignedByte(); // 62-62 (1)
							xin.skip(2); // 63-64 (2)
							fileNumber = readUnsignedShort(xin); // 65-66 (2)
							// fileNumber = xin.readUnsignedByte(); // 65-66 (2)
							xin.skip(13); // 67-79 (13)
							// Date endDate2 =
							readIntegerDate(xin); // 80-83 (4)
							xin.skip(20); // 84-103(20)
							beginDate = readIntegerDate(xin); // 104-107 (4)
							// beginDate2 =
							readIntegerDate(xin); // 108-111 (4)
							xin.skip(4); // 112-115 (4)
							endDate = readIntegerDate(xin); // 116-119 (4)
							xin.skip(30); // 120-149(30)
							// FIX: เก็บ path เต็มของ subdirectory นี้ (subName) ไม่ใช่ root dir
							putMasterEntry(name, new File(subName, "F" + fileNumber + ".MWD").getPath());

							header = new HeaderMetaStock();
							header.setName(name.trim());
							header.setBeginDate(beginDate);
							header.setEndDate(endDate);
							header.setFilename("F" + fileNumber + ".MWD");
							header.setMaster("XMASTER");
							getHeaderList().add(header);

							// System.out.println(symbol + ":" +"F"+ fileNumber+".MWD");
						}
					} catch (EOFException eof) {
						// eof.printStackTrace();
					}
					xin.close();

				} catch (FileNotFoundException fe) {
				} catch (IOException io) {
				}
			}
		} // for

		return masterIndex;
	}

	public ArrayList<StockRecord> createStockList(String symbol) {
		stockList = new ArrayList<StockRecord>();
		StockRecord stock = new StockRecord();
		DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
		DataInputStream in = null;
		File recordFile = null;
		Date date = null;
		LocalDate dateFloat;
		float open = 0, high = 0, low = 0, close = 0, volume = 0, openInterest = 0;

		boolean firstdate = false;

		try {
			//print(this.masterIndex.get(symbol));
			// FIX: masterIndex เก็บ path เต็มอยู่แล้ว (รวม subdirectory) ไม่ต้องเอามาต่อกับ getDir() อีก
			recordFile = new File(this.masterIndex.get(symbol));
			in = new DataInputStream(new FileInputStream(recordFile));
			// System.out.println(symbolText);
			try {// read F.DAT ,MWD
				while (true) {
					date = readFloatDate(in); // 0-3 (4);
					open = readFloat(in); // 4-7 (4);
					high = readFloat(in); // 8-11 (4);
					low = readFloat(in); // 12-15 (4);
					close = readFloat(in); // 16-19 (4);
					volume = readFloat(in); // 20-23 (4);
					openInterest = readFloat(in); // 24-27 (4);

					dateFloat = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					if ((dateFloat.equals(startDate) || dateFloat.isAfter(startDate)) && !firstdate) {
						// System.out.println("date: "+date);
						firstdate = true;
					}
					if (firstdate) {
						// String dateKey = DATE_FORMAT.format(date);
						// if (symbol.equals("SET")) {
						// print(symbol + " " + dateKey + " " + date + " " +
						// this.masterIndex.get(symbol));
						// }
						stock = new StockRecord(symbol, date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
								open, high, low, close);
						stockList.add(stock);
					}
				}

			} catch (EOFException eof) {
			}

		} catch (FileNotFoundException ffe) {
			System.err.println("<createstockList>" + ffe.getMessage());
		} catch (IOException io) {
			System.err.println("<createstockList>" + io.getMessage());
		}
		return stockList;
	}

	public ArrayList<StockTimeRecord> createStockTimeList(String symbol) {
		stockTimeList = new ArrayList<StockTimeRecord>();
		StockTimeRecord stock = new StockTimeRecord();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yy");
		DataInputStream in = null;
		File recordFile = null;
		LocalDateTime date = null;
		LocalTime time;
		LocalDate dateFloat, timeFloat;
		LocalDateTime dateTime;
		float open = 0, high = 0, low = 0, close = 0, volume = 0, openInterest = 0;

		boolean firstdate = false;

		try {
			// FIX: masterIndex เก็บ path เต็มอยู่แล้ว (รวม subdirectory) ไม่ต้องเอามาต่อกับ getDir() อีก
			recordFile = new File(this.masterIndex.get(symbol));
			in = new DataInputStream(new FileInputStream(recordFile));
			// System.out.println(symbolText);
			try {// read F.DAT ,MWD
				while (true) {
					date = readFloatTime(in); // 0-3 (4);
					open = readFloat(in); // 4-7 (4);
					high = readFloat(in); // 8-11 (4);
					low = readFloat(in); // 12-15 (4);
					close = readFloat(in); // 16-19 (4);
					volume = readFloat(in); // 20-23 (4);
					// openInterest = readFloat(in); // 24-27 (4); // error not this colomn find
					// solution half day 05 April 2023

					/*
					 * if (dateFloat.isAfter(startDate) && !firstdate) { //
					 * System.out.println("date: "+date); firstdate = true; }
					 */
					if (date.isAfter(startDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime())) {
						String dateKey = dateFormat.format(date);
						/*
						 * if (symbol.equals("S50IF_CONVL")) { print(symbol + " key: " +
						 * dateKey+" date: "+date + " close: " + close + " " +
						 * this.masterIndex.get(symbol)); }
						 */
						stock = new StockTimeRecord(symbol, date, "H", open, high, low, close);
						stockTimeList.add(stock);
					}
				}

			} catch (EOFException eof) {
			}

		} catch (FileNotFoundException ffe) {
			System.err.println("<createstockList>" + ffe.getMessage());
		} catch (IOException io) {
			System.err.println("<createstockList>" + io.getMessage());
		}

		Comparator<StockTimeRecord> timeCompare = Comparator.comparing(StockTimeRecord::getDatetime);
		stockTimeList.sort(timeCompare);

		return stockTimeList;
	}

	public void printFirstLastData(String symbol) {
		stockList = createStockList(symbol);
		StockRecord stock;
		for (int i = 0; i < 2; i++) {
			stock = stockList.get(i);
			print("Symbol: " + symbol + " Date: " + stock.getDate() + " Close: "
					+ String.format("%,6.2f", stock.getClose()) + " size: " + stockList.size());
		}
		for (int i = stockList.size() - 2; i < stockList.size(); i++) {
			stock = stockList.get(i);
			print("Symbol: " + symbol + " Date: " + stock.getDate() + " Close: "
					+ String.format("%,6.2f", stock.getClose()) + " size: " + stockList.size());
		}
	}

	public void printFirstLastDataTime(String symbol) {
		stockTimeList = createStockTimeList(symbol);
		StockTimeRecord stock;
		for (int i = 0; i < 2; i++) {
			stock = stockTimeList.get(i);
			print("Symbol: " + symbol + " Time: " + stock.getDatetime() + " Close: "
					+ String.format("%,6.2f", stock.getClose()) + " size: " + stockTimeList.size());
		}
		for (int i = stockTimeList.size() - 2; i < stockTimeList.size(); i++) {
			stock = stockTimeList.get(i);
			print("Symbol: " + symbol + " Time: " + stock.getDatetime() + " Close: "
					+ String.format("%,6.2f", stock.getClose()) + " size: " + stockTimeList.size());
		}
	}

	public String readASCIIString(int byteCount, DataInputStream in) {
		StringBuffer buf = new StringBuffer();
		try {
			while (byteCount-- > 0) { // read into buf until 0 byte or byteCount
				char c = (char) in.readUnsignedByte();
				if (c == 0)
					break;
				else
					buf.append(c);
			}
			// consume any remaining bytes after 0.
			if (byteCount > 0)
				in.skip(byteCount);
		} catch (IOException i) {
			// System.out.println(byteCount);
		}
		return buf.toString();
	}

	public Date readFloatDate(DataInputStream in) throws IOException {
		// float f = in.readFloat(); // error 4/4/2003 half day find solution is next
		float f = readFloat(in);
		return toDate((int) f);
	}

	public LocalDateTime readFloatTime(DataInputStream in) throws IOException {
		float f = readFloat(in);
		float time = readFloat(in);
		return toTime((int) f, (int) time);
	}

	private Calendar calendar = new GregorianCalendar();

	private Date toDate(int i) {
		// System.out.println("Date i: "+i);
		int dateOfMonth = i % 100;
		i /= 100;
		int month = i % 100;
		i /= 100;
		int year = i;
		if (year < 1000)
			year += 1900;
		calendar.clear();
		calendar.set(year, month - 1, dateOfMonth);
		// int parsedYear = calendar.get(Calendar.YEAR);
		return calendar.getTime();
	}

	private LocalDateTime toTime(int date, int time) {
		// System.out.println("Time date: "+date+" "+ time);
		LocalDateTime dateTime;
		int day = 0, month = 0, year = 0;
		int hour = 0, min = 0;

		year = 1900 + (date / 10000);
		month = (date % 10000) / 100;
		day = date % 100;

		hour = time / 10000;
		min = (time % 10000) / 100;
		if (month > 0 && month < 13 && day > 0 && day < 32 && time > 0)
			dateTime = LocalDateTime.of(year, month, day, hour, min);
		else
			dateTime = LocalDateTime.of(1990, 1, 1, 0, 0);

		return dateTime;
	}

	public int readInt(DataInputStream in) throws IOException {
		return (in.readUnsignedByte() | in.readUnsignedByte() << 8 | in.readUnsignedByte() << 16 | in.readByte() << 24);
	}

	public Date readIntegerDate(DataInputStream in) throws IOException {
		int i = readInt(in);
		return toDate(i);
	}

	public short readUnsignedShort(DataInputStream in) throws IOException {
		return (short) (in.readUnsignedByte() | in.readUnsignedByte() << 8);
	}

	public float readFloat(DataInputStream in) throws IOException {
		int b0 = in.readUnsignedByte();
		int b1 = in.readUnsignedByte();
		int b2 = in.readUnsignedByte();
		int b3 = in.readUnsignedByte();
		int mantissa = (b2 << 16 | b1 << 8 | b0) & 0x7fffff;
		int sign = b2 & 0x80;
		int exponent = b3 - 2;
		int ieeeFloatBits = sign << 24 | exponent << 23 | mantissa;
		return Float.intBitsToFloat(ieeeFloatBits);
	}

	public static void print(String st) {
		System.out.println(st);
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public ArrayList<HeaderMetaStock> getHeaderList() {
		return headerList;
	}

	public void setHeaderList(ArrayList<HeaderMetaStock> headerList) {
		this.headerList = headerList;
	}

}

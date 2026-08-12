package com.metastock.rocranker;

import java.time.LocalDate;
import java.util.List;

/**
 * 02-roc-ranker
 *
 * Workflow:
 *  1. Load DR symbol list from a comma-separated text file (drafl.txt).
 *  2. Read each symbol's full daily price history from the MetaStock database.
 *  3. Compute YTD ROC (per month, Jan..current) and Forward-Month ROC.
 *  4. Sort by YTD% descending, keep Top 10 per month.
 *  5. Write the full report to DRYTD.csv and print the latest month's Top 10
 *     to the console, nicely column-aligned.
 *
 * Usage:
 *   java -jar roc-ranker.jar [metaStockDir] [drListFile] [outputCsv]
 * All three arguments are optional; defaults below are used if omitted.
 */
public class Main {

	// ---- Default configuration (override via command-line args if needed) ----
	private static final String DEFAULT_METASTOCK_DIR = "C:\\MetaStockData";
	private static final String DEFAULT_DR_LIST_FILE = "C:\\meta\\dr\\report\\drafl.txt";
	private static final String DEFAULT_OUTPUT_CSV = "C:\\meta\\dr\\report\\DRYTD.csv";
	private static final String REFERENCE_SYMBOL = "GOOG80"; // longest continuous history -> trading calendar
	private static final int TOP_N = 10;
	private static final java.time.format.DateTimeFormatter TF = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");

	public static void main(String[] args) throws Exception {
		String metaStockDir = args.length > 0 ? args[0] : DEFAULT_METASTOCK_DIR;
		String drListFile = args.length > 1 ? args[1] : DEFAULT_DR_LIST_FILE;
		String outputCsv = args.length > 2 ? args[2] : DEFAULT_OUTPUT_CSV;

		System.out.println("MetaStock dir : " + metaStockDir);
		System.out.println("DR list file  : " + drListFile);
		System.out.println("Output CSV    : " + outputCsv);
		System.out.println();

		// 1. Load DR symbol list
		DrListLoader listLoader = new DrListLoader();
		List<String> symbols = listLoader.load(drListFile);
		System.out.println("Loaded " + symbols.size() + " DR symbols: " + symbols);

		// 2. Init MetaStock reader (wide start date to capture full history)
		LocalDate startDate = LocalDate.of(2000, 1, 1);
		LocalDate endDate = LocalDate.now();
		MetaStock metaStock = new MetaStock(metaStockDir, startDate, endDate);
		metaStock.loadMaster();

		// 3. Compute report rows (all months, Top 10 each)
		RocReportGenerator generator = new RocReportGenerator(metaStock, REFERENCE_SYMBOL, TOP_N);
		List<RocReportRow> rows = generator.generate(symbols);

		// 4. Write CSV report
		new CsvReportWriter().write(outputCsv, rows);
		System.out.println();
		System.out.println("Report written: " + outputCsv + " (" + rows.size() + " rows)");

		// 5. Print latest month's Top 10 to console, column-aligned
		System.out.println();
		System.out.println("=== Latest month Top " + TOP_N + " (sorted by YTD%) ===");
		new ConsoleReportPrinter().printLatestMonth(rows);
	}
}

package com.metastock.rocranker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Loads the DR symbol list from a text file such as:
 * C:\meta\dr\report\drafl.txt
 *
 * File format: a single line of symbols separated by commas, e.g.
 * AAOI03,AAPL80
 *
 * Splits on comma and/or newline, trims whitespace, drops empty tokens,
 * and removes duplicates while preserving order.
 */
public class DrListLoader {

	public List<String> load(String filePath) throws IOException {
		String content = new String(Files.readAllBytes(Path.of(filePath)), StandardCharsets.UTF_8);
		LinkedHashSet<String> symbols = new LinkedHashSet<>();
		for (String raw : content.split("[,\\r\\n]+")) {
			String s = raw.trim();
			if (!s.isEmpty()) {
				symbols.add(s);
			}
		}
		return new ArrayList<>(symbols);
	}
}

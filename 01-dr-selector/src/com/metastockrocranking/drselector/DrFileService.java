package com.metastockrocranking.drselector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DrFileService {

    public List<DrRecord> readDrTxt(String path) throws IOException {
        List<DrRecord> records = new ArrayList<>();

        for (String line : Files.readAllLines(Paths.get(path))) {
            //System.out.println(line);
            if (line.isBlank()) continue;

            String[] cols = line.split("\t");
            if (cols.length < 6) continue; // กันแถวข้อมูลไม่ครบ

            String drName = cols[0].trim();
            if( drName.equals("XD")) continue;

            // column ที่ 6 (index 5) อาจมี comma คั่นหลักพัน เช่น 8,677.37 -> ยังต้องตัด comma นี้ทิ้งก่อน parse เป็นตัวเลข
            String col5 = cols[5].trim().replace(",", "").replace("\"", "");
            double value = Double.parseDouble(col5);

            records.add(new DrRecord(drName, value));
        }
        return records;
    }

    public Set<String> readPortfolio(String path) throws IOException {
        return Files.readAllLines(Paths.get(path)).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public void writeDrWithValue(String path, List<DrRecord> records) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path))) {
            for (DrRecord r : records) {
                writer.write(r.getDrName() + "," + r.getValue());
                writer.newLine();
            }
        }
    }

    public void writeDrNamesOnly(String path, List<DrRecord> records) throws IOException {
        String joined = records.stream()
                .map(DrRecord::getDrName)
                .collect(Collectors.joining(","));
        Files.writeString(Paths.get(path), joined);
    }
}
package com.metastockrocranking.drselector;

import java.util.*;
import java.util.stream.Collectors;

public class DrSelector {

    public List<DrRecord> select(List<DrRecord> allRecords, Set<String> portfolioNames) {
        Map<String, List<DrRecord>> groupedByPrefix = allRecords.stream()
                .collect(Collectors.groupingBy(DrRecord::getPrefix));

        List<DrRecord> result = new ArrayList<>();
        for (List<DrRecord> group : groupedByPrefix.values()) {
            result.add(selectFromGroup(group, portfolioNames));
        }

        result.sort(Comparator.comparing(DrRecord::getDrName));
        return result;
    }

    private DrRecord selectFromGroup(List<DrRecord> group, Set<String> portfolioNames) {
        DrRecord maxByValue = Collections.max(group, Comparator.comparingDouble(DrRecord::getValue));

        // กรณีปกติ: ตัวมูลค่าสูงสุดอยู่ใน portfolio อยู่แล้ว -> เลือกตัวนี้
        if (portfolioNames.contains(maxByValue.getDrName())) {
            return maxByValue;
        }

        // ตัวมูลค่าสูงสุดไม่อยู่ใน portfolio -> เช็คตัวอื่นในกลุ่มว่ามีตัวไหนอยู่ใน portfolio ไหม
        List<DrRecord> inPortfolio = group.stream()
                .filter(r -> portfolioNames.contains(r.getDrName()))
                .collect(Collectors.toList());

        if (!inPortfolio.isEmpty()) {
            // ถ้ามีมากกว่า 1 ตัวอยู่ใน portfolio เลือกตัวมูลค่าสูงสุดในกลุ่มย่อยนี้
            return Collections.max(inPortfolio, Comparator.comparingDouble(DrRecord::getValue));
        }

        return maxByValue; // ไม่มีตัวไหนใน portfolio เลย -> ใช้กฎมูลค่าสูงสุดตามปกติ
    }
}
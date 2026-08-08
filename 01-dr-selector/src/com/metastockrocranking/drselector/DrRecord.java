package com.metastockrocranking.drselector;

public class DrRecord {
    private final String drName;
    private final String prefix;
    private final double value;

    public DrRecord(String drName, double value) {
        this.drName = drName;
        this.value = value;
        this.prefix = extractPrefix(drName);
    }

    // ตัด suffix ตัวเลข 2 หลักท้ายออก เช่น AAPL80 -> AAPL
    private static String extractPrefix(String drName) {
        if (drName != null && drName.length() > 2) {
            return drName.substring(0, drName.length() - 2);
        }
        return drName;
    }

    public String getDrName() { return drName; }
    public String getPrefix() { return prefix; }
    public double getValue() { return value; }
}
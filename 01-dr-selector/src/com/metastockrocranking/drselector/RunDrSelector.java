package com.metastockrocranking.drselector;

import java.util.List;
import java.util.Set;

public class RunDrSelector {

    private static final String INPUT_DR_TXT    = "C:\\meta\\dr\\InDR\\dr.txt";
    private static final String PORTFOLIO_TXT    = "C:\\meta\\dr\\InDR\\port.txt";
    private static final String OUTPUT_DR_TXT    = "C:\\meta\\dr\\report\\dr.txt";
    private static final String OUTPUT_DRAFL_TXT = "C:\\meta\\dr\\report\\drafl.txt";

    public static void main(String[] args) throws Exception {
        System.out.println("=== [01] RunDrSelector: start ===");

        DrFileService fileService = new DrFileService();
        DrSelector selector = new DrSelector();

        List<DrRecord> allRecords = fileService.readDrTxt(INPUT_DR_TXT);
        Set<String> portfolio = fileService.readPortfolio(PORTFOLIO_TXT);

        List<DrRecord> selected = selector.select(allRecords, portfolio);

        fileService.writeDrWithValue(OUTPUT_DR_TXT, selected);
        fileService.writeDrNamesOnly(OUTPUT_DRAFL_TXT, selected);

        System.out.println("=== [01] RunDrSelector: done, selected " + selected.size() + " DR ===");
    }
}
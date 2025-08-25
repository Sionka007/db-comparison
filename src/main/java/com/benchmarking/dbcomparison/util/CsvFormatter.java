package com.benchmarking.dbcomparison.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CsvFormatter {
    private static final DecimalFormat decimalFormat;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pl", "PL"));
        decimalFormat = new DecimalFormat("#,##0.00", symbols);
    }

    public static String formatNumber(double number) {
        return decimalFormat.format(number);
    }

    public static String formatCsvLine(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(";");
            }
            Object value = values[i];
            if (value instanceof Number) {
                sb.append(formatNumber(((Number) value).doubleValue()));
            } else {
                sb.append(value != null ? value.toString() : "");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}

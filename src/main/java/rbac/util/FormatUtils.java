package rbac.util;

import java.util.List;

public final class FormatUtils {

    private FormatUtils() {}

    public static String formatTable(String[] headers, List<String[]> rows) {
        int cols = headers.length;
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) widths[i] = headers[i].length();
        for (String[] row : rows) {
            for (int i = 0; i < cols && i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i] != null ? row[i].length() : 0);
            }
        }
        StringBuilder sb = new StringBuilder();
        String sep = borderLine(widths);
        sb.append(sep).append("\n");
        sb.append(rowLine(headers, widths));
        sb.append(sep).append("\n");
        for (String[] row : rows) {
            sb.append(rowLine(padRow(row, cols), widths));
        }
        sb.append(sep).append("\n");
        return sb.toString();
    }

    private static String[] padRow(String[] row, int cols) {
        String[] r = new String[cols];
        for (int i = 0; i < cols; i++) r[i] = i < row.length && row[i] != null ? row[i] : "";
        return r;
    }

    private static String borderLine(int[] widths) {
        StringBuilder b = new StringBuilder("+");
        for (int w : widths) {
            b.append("-".repeat(w + 2)).append("+");
        }
        return b.toString();
    }

    private static String rowLine(String[] cells, int[] widths) {
        StringBuilder b = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String c = i < cells.length && cells[i] != null ? cells[i] : "";
            b.append(" ").append(padRight(c, widths[i])).append(" |");
        }
        return b.append("\n").toString();
    }

    public static String formatBox(String text) {
        if (text == null) text = "";
        String[] lines = text.split("\n");
        int max = 0;
        for (String line : lines) max = Math.max(max, line.length());
        StringBuilder sb = new StringBuilder();
        sb.append("+").append("-".repeat(max + 2)).append("+\n");
        for (String line : lines) sb.append("| ").append(padRight(line, max)).append(" |\n");
        sb.append("+").append("-".repeat(max + 2)).append("+\n");
        return sb.toString();
    }

    public static String formatHeader(String text) {
        String t = text != null ? text : "";
        return "\n=== " + t + " ===\n";
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return " ".repeat(length - text.length()) + text;
    }
}

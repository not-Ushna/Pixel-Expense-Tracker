// Expense.java
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Expense {
    private String description;
    private double amount;
    private String category;
    private LocalDate date;
    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE;

    public Expense(String description, double amount, String category, LocalDate date) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    // getters
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }

    @Override
    public String toString() {
        return String.format("%s | %-10s | %-20s | $%.2f", date.format(F), category, description, amount);
    }

    // Simple JSON-line serializer (escapes quotes/backslashes)
    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public String toJsonLine() {
        return "{\"date\":\"" + date.format(F) +
               "\",\"category\":\"" + esc(category) +
               "\",\"description\":\"" + esc(description) +
               "\",\"amount\":" + String.format("%.2f", amount) + "}";
    }

    // crude parser for same format
    public static Expense fromJsonLine(String line) {
        try {
            String d = extract(line, "\"date\":\"", "\"");
            String c = extract(line, "\"category\":\"", "\"");
            String desc = extract(line, "\"description\":\"", "\"");
            String amtS = extractAmount(line, "\"amount\":");
            double a = Double.parseDouble(amtS);
            return new Expense(desc, a, c, LocalDate.parse(d, F));
        } catch (Exception e) {
            return null;
        }
    }

    private static String extract(String s, String start, String end) {
        int i = s.indexOf(start);
        if (i < 0) return "";
        i += start.length();
        int j = s.indexOf(end, i);
        if (j < 0) j = s.length();
        return s.substring(i, j).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String extractAmount(String s, String key) {
        int i = s.indexOf(key);
        if (i < 0) return "0";
        i += key.length();
        int j = i;
        while (j < s.length() && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '.' || s.charAt(j) == '-')) j++;
        return s.substring(i, j);
    }
}

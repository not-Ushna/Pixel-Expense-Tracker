// ExpenseTracker.java
import java.util.*;
import java.nio.file.*;
import java.io.*;

public class ExpenseTracker {
    private final List<Expense> expenses = new ArrayList<>();
    private final Path store = Paths.get("expenses.jsonl");

    public ExpenseTracker() {
        load();
    }

    public synchronized void addExpense(Expense e) {
        expenses.add(e);
        save();
    }

    public synchronized List<Expense> all() {
        return new ArrayList<>(expenses);
    }

    public synchronized List<Expense> byCategory(String cat) {
        List<Expense> out = new ArrayList<>();
        for (Expense e : expenses) if (e.getCategory().equalsIgnoreCase(cat)) out.add(e);
        return out;
    }

    public synchronized double total() {
        double t = 0;
        for (Expense e : expenses) t += e.getAmount();
        return t;
    }

    public synchronized void save() {
        try (BufferedWriter w = Files.newBufferedWriter(store, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Expense e : expenses) {
                w.write(e.toJsonLine());
                w.newLine();
            }
        } catch (IOException ex) {
            System.err.println("Save error: " + ex.getMessage());
        }
    }

    public synchronized void load() {
        expenses.clear();
        if (!Files.exists(store)) return;
        try {
            List<String> lines = Files.readAllLines(store);
            for (String l : lines) {
                if (l == null || l.trim().isEmpty()) continue;
                Expense e = Expense.fromJsonLine(l);
                if (e != null) expenses.add(e);
            }
        } catch (IOException ex) {
            System.err.println("Load error: " + ex.getMessage());
        }
    }
}

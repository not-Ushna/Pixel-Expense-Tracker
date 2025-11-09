import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.time.LocalDate;
import java.util.List;

public class Main {
    private ExpenseTracker tracker = new ExpenseTracker();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        try {
            DefaultTerminalFactory factory = new DefaultTerminalFactory();
            Screen screen = factory.createScreen();
            screen.startScreen();

            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
                    new EmptySpace(TextColor.ANSI.BLUE));
            final BasicWindow window = new BasicWindow("Pixel Expense Tracker");

            Panel root = new Panel();
            root.setLayoutManager(new LinearLayout(Direction.VERTICAL));

            Label title = new Label("✨ PIXEL EXPENSE TRACKER ✨").addStyle(SGR.BOLD);
            title.setForegroundColor(TextColor.ANSI.CYAN);
            root.addComponent(title);

            Panel buttons = new Panel(new LinearLayout(Direction.HORIZONTAL));
            buttons.addComponent(new Button("➕ Add", () -> openAdd(gui)));
            buttons.addComponent(new Button("📜 View All", () -> openList(gui, "All Expenses", tracker.all())));
            buttons.addComponent(new Button("🔍 By Category", () -> openCategory(gui)));
            buttons.addComponent(new Button("💰 Total", () -> showTotal(gui)));
            buttons.addComponent(new Button("🚪 Exit", window::close));
            root.addComponent(buttons);

            Label hint = new Label("Use the buttons above to manage your expenses.");
            hint.setForegroundColor(TextColor.ANSI.GREEN);
            root.addComponent(hint);

            window.setComponent(root);
            gui.addWindowAndWait(window);
            screen.stopScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openAdd(MultiWindowTextGUI gui) {
        final BasicWindow w = new BasicWindow("Add Expense");
        Panel p = new Panel(new GridLayout(2));

        final TextBox desc = new TextBox(new TerminalSize(40, 1));
        final TextBox amt = new TextBox(new TerminalSize(12, 1));
        final TextBox cat = new TextBox(new TerminalSize(20, 1));

        p.addComponent(new Label("Description:"));
        p.addComponent(desc);
        p.addComponent(new Label("Amount:"));
        p.addComponent(amt);
        p.addComponent(new Label("Category:"));
        p.addComponent(cat);

        Panel btns = new Panel(new LinearLayout(Direction.HORIZONTAL));
        btns.addComponent(new Button("Save", () -> {
            String d = desc.getText().trim();
            String aS = amt.getText().trim();
            String c = cat.getText().trim();
            double a;
            try {
                a = Double.parseDouble(aS);
            } catch (Exception ex) {
                Label warn = new Label("❌ Invalid amount! Must be a number.");
                warn.setForegroundColor(TextColor.ANSI.RED);
                p.addComponent(warn);
                return;
            }
            tracker.addExpense(new Expense(
                    d.isEmpty() ? "(no description)" : d,
                    a,
                    c.isEmpty() ? "(uncategorized)" : c,
                    LocalDate.now()
            ));
            w.close();
        }));
        btns.addComponent(new Button("Cancel", w::close));

        p.addComponent(new EmptySpace());
        p.addComponent(btns);

        w.setComponent(p);
        gui.addWindowAndWait(w);
    }

    private void openList(MultiWindowTextGUI gui, String title, List<Expense> list) {
        final BasicWindow w = new BasicWindow(title);
        Panel p = new Panel(new LinearLayout(Direction.VERTICAL));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-12s | %-12s | %-30s | %8s\n", "DATE", "CATEGORY", "DESCRIPTION", "AMOUNT"));
        sb.append("--------------------------------------------------------------------------\n");

        if (list.isEmpty()) {
            sb.append("(no expenses found)\n");
        } else {
            for (Expense e : list) {
                sb.append(String.format("%-12s | %-12s | %-30s | $%7.2f\n",
                        e.getDate(), e.getCategory(), trim(e.getDescription(), 30), e.getAmount()));
            }
        }

        TextBox box = new TextBox(new TerminalSize(80, 20), sb.toString());
        box.setReadOnly(true);
        box.setVerticalFocusSwitching(false);
        p.addComponent(box);

        p.addComponent(new Button("Close", w::close));
        w.setComponent(p);
        gui.addWindowAndWait(w);
    }

    private void openCategory(MultiWindowTextGUI gui) {
        final BasicWindow w = new BasicWindow("Filter by Category");
        Panel p = new Panel(new GridLayout(2));

        final TextBox cat = new TextBox(new TerminalSize(20, 1));
        p.addComponent(new Label("Category:"));
        p.addComponent(cat);

        Panel btns = new Panel(new LinearLayout(Direction.HORIZONTAL));
        btns.addComponent(new Button("Show", () -> {
            String c = cat.getText().trim();
            List<Expense> filtered = tracker.byCategory(c);
            w.close();
            openList(gui, "Category: " + c, filtered);
        }));
        btns.addComponent(new Button("Cancel", w::close));

        p.addComponent(new EmptySpace());
        p.addComponent(btns);

        w.setComponent(p);
        gui.addWindowAndWait(w);
    }

    private void showTotal(MultiWindowTextGUI gui) {
        double total = tracker.total();
        final BasicWindow w = new BasicWindow("Total Spending");

        Panel p = new Panel(new LinearLayout(Direction.VERTICAL));
        Label l = new Label(String.format("Total: $%.2f", total)).addStyle(SGR.BOLD);
        l.setForegroundColor(TextColor.ANSI.GREEN);
        p.addComponent(l);
        p.addComponent(new Button("OK", w::close));

        w.setComponent(p);
        gui.addWindowAndWait(w);
    }

    private String trim(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 3) + "...";
    }
}

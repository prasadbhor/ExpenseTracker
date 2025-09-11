import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


public class ExpenseTracker {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final List<String> incomeCategories = Arrays.asList("salary", "business", "other");
    private static final List<String> expenseCategories = Arrays.asList("food", "rent", "travel", "utilities", "other");

    private static final List<Transaction> transactions = new ArrayList<>();

    public static void main(String[] args) {
        printWelcome();
        mainMenuLoop();

    }

    private static void printWelcome() {
        System.out.println("===============PrasadBhor=================");
        System.out.println("         Welcome to Expense Tracker  System  Prasad Bhor Hi     ");
        System.out.println("=================Assignment===============");
    }

    private static void mainMenuLoop() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Add Income");
            System.out.println("2. Add Expense");
            System.out.println("3. View Monthly Summary");
            System.out.println("4. Load transactions from file");
            System.out.println("5. Save transactions to file");
            System.out.println("6. Exit");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    addTransaction("income");
                    break;
                case "2":
                    addTransaction("expense");
                    break;
                case "3":
                    showMonthlySummary();
                    break;
                case "4":
                    loadFromFileInteractive();
                    break;
                case "5":
                    saveToFileInteractive();
                    break;
                case "6":
                    exit = true;
                    System.out.println("Thank you for using Expense Tracker. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void addTransaction(String type) {
        System.out.println("\nAdding a new " + type + " transaction.");

        // Select category
        List<String> categories = type.equalsIgnoreCase("income") ? incomeCategories : expenseCategories;
        System.out.println("Select a category:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, capitalize(categories.get(i)));
        }
        int catIndex = -1;
        while (catIndex < 0 || catIndex >= categories.size()) {
            System.out.print("Enter category number: ");
            String catInput = scanner.nextLine().trim();
            try {
                catIndex = Integer.parseInt(catInput) - 1;
                if (catIndex < 0 || catIndex >= categories.size()) {
                    System.out.println("Invalid category number. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a number.");
            }
        }
        String category = categories.get(catIndex);

        // Date input
        LocalDate date = null;
        while (date == null) {
            System.out.print("Enter date (YYYY-MM-DD), leave blank for today: ");
            String dateInput = scanner.nextLine().trim();
            if (dateInput.isEmpty()) {
                date = LocalDate.now();
            } else {
                try {
                    date = LocalDate.parse(dateInput, DATE_FORMAT);
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Try again.");
                }
            }
        }

        // Amount input
        Double amount = null;
        while (amount == null) {
            System.out.print("Enter amount: ");
            String amountInput = scanner.nextLine().trim();
            try {
                amount = Double.parseDouble(amountInput);
                if (amount <= 0) {
                    System.out.println("Amount must be positive. Try again.");
                    amount = null;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Enter a numeric value.");
            }
        }

        // Description (optional)
        System.out.print("Enter a description (optional): ");
        String description = scanner.nextLine().trim();

        Transaction t = new Transaction(type.toLowerCase(), date, category.toLowerCase(), amount, description);
        transactions.add(t);
        System.out.println("Transaction added successfully.");
    }

    private static void showMonthlySummary() {
        if (transactions.isEmpty()) {
            System.out.println("\nNo transactions to show. Add some first.");
            return;
        }

        System.out.print("\nEnter year and month for summary (YYYY-MM), leave blank for current month: ");
        String ymInput = scanner.nextLine().trim();
        LocalDate monthStart;
        if (ymInput.isEmpty()) {
            LocalDate now = LocalDate.now();
            monthStart = LocalDate.of(now.getYear(), now.getMonth(), 1);
        } else {
            try {
                monthStart = LocalDate.parse(ymInput + "-01", DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Showing current month summary.");
                LocalDate now = LocalDate.now();
                monthStart = LocalDate.of(now.getYear(), now.getMonth(), 1);
            }
        }
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        System.out.println("\nMonthly summary for " + monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue()));
        System.out.println("=================================");

        double totalIncome = 0;
        double totalExpense = 0;

        Map<String, Double> incomeByCategory = new HashMap<>();
        Map<String, Double> expenseByCategory = new HashMap<>();

        for (Transaction t : transactions) {
            if (!t.getDate().isBefore(monthStart) && !t.getDate().isAfter(monthEnd)) {
                if (t.getType().equals("income")) {
                    totalIncome += t.getAmount();
                    incomeByCategory.put(t.getCategory(),
                            incomeByCategory.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
                } else if (t.getType().equals("expense")) {
                    totalExpense += t.getAmount();
                    expenseByCategory.put(t.getCategory(),
                            expenseByCategory.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
                }
            }
        }

        System.out.println("Income:");
        if (incomeByCategory.isEmpty()) {
            System.out.println("  No income records.");
        } else {
            for (String cat : incomeByCategory.keySet()) {
                System.out.printf("  %s: %.2f\n", capitalize(cat), incomeByCategory.get(cat));
            }
            System.out.printf("  Total Income: %.2f\n", totalIncome);
        }

        System.out.println("\nExpenses:");
        if (expenseByCategory.isEmpty()) {
            System.out.println("  No expense records.");
        } else {
            for (String cat : expenseByCategory.keySet()) {
                System.out.printf("  %s: %.2f\n", capitalize(cat), expenseByCategory.get(cat));
            }
            System.out.printf("  Total Expenses: %.2f\n", totalExpense);
        }

        double net = totalIncome - totalExpense;
        System.out.println("\nNet Total: " + (net >= 0 ? "\u001B[32m" : "\u001B[31m") + String.format("%.2f", net) + "\u001B[0m");
    }

    private static void loadFromFileInteractive() {
        System.out.print("Enter path to the CSV file to load: ");
        String filePath = scanner.nextLine().trim();
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("File not found or invalid. Aborting load.");
            return;
        }

        try {
            int loadedCount = loadFromFile(filePath);
            System.out.println("Loaded " + loadedCount + " transactions from file.");
        } catch (IOException e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }

    private static int loadFromFile(String filePath) throws IOException {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("type,")) {
                    // Skip empty lines, comments or header line
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    System.out.println("Skipping invalid line (not enough fields): " + line);
                    continue;
                }
                String type = parts[0].toLowerCase();
                String dateStr = parts[1];
                String category = parts[2].toLowerCase();
                String amountStr = parts[3];

                if (!type.equals("income") && !type.equals("expense")) {
                    System.out.println("Skipping invalid type on line: " + line);
                    continue;
                }
                LocalDate date;
                try {
                    date = LocalDate.parse(dateStr, DATE_FORMAT);
                } catch (DateTimeParseException e) {
                    System.out.println("Skipping line due to invalid date: " + line);
                    continue;
                }
                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping line due to invalid amount: " + line);
                    continue;
                }
                // Description optional, ignored in file load
                Transaction t = new Transaction(type, date, category, amount, "");
                transactions.add(t);
                count++;
            }
        }
        return count;
    }

    private static void saveToFileInteractive() {
        System.out.print("Enter path to save current transactions (e.g., data.csv): ");
        String filePath = scanner.nextLine().trim();
        try {
            saveToFile(filePath);
            System.out.println("Transactions saved successfully to " + filePath);
        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }

    private static void saveToFile(String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write("type,date,category,amount\n");
            for (Transaction t : transactions) {
                bw.write(String.format("%s,%s,%s,%.2f\n",
                        t.getType(),
                        t.getDate().format(DATE_FORMAT),
                        t.getCategory(),
                        t.getAmount()));
            }
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // Inner class to represent a transaction
    private static class Transaction {
        private final String type; // income or expense
        private final LocalDate date;
        private final String category;
        private final double amount;
        private final String description;

        public Transaction(String type, LocalDate date, String category, double amount, String description) {
            this.type = type.toLowerCase();
            this.date = date;
            this.category = category.toLowerCase();
            this.amount = amount;
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }

        @SuppressWarnings("unused")
        public String getDescription() {
            return description;
        }
    }
}



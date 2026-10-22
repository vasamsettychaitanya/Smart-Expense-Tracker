# Smart Expense Tracker (Java + JDBC + MySQL)

A beginner-friendly console application for recording and managing everyday expenses. It demonstrates Java OOP, JDBC, SQL, CRUD operations, filtering, and monthly summaries.

## Features
- Add an expense with title, amount, category, date, and optional notes
- View all saved expenses
- Search by category or exact date
- Update and delete an expense (deletion asks for confirmation)
- Calculate a selected month's total spending
- Show category totals in descending order and identify the highest-spending category
- Uses `PreparedStatement` to safely pass user input to SQL
- Automatically creates the `expenses` table when the app starts

## Tech stack
- Java 17+
- Maven
- MySQL 8+
- JDBC / MySQL Connector/J

## Requirements
Install Java JDK 17 or newer, Maven, and MySQL Server. You can use MySQL Workbench to run SQL commands.

## Setup
1. Start MySQL Server.
2. Open MySQL Workbench or a MySQL terminal and run `database/schema.sql`. This creates the `smart_expense_tracker` database and table.
3. Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties`.
4. Edit `db.properties` with your local MySQL username and password. **Do not commit `db.properties` to GitHub.** It is ignored by `.gitignore`.
5. Open a terminal in the project folder and run:

   ```bash
   mvn clean compile
   mvn exec:java -Dexec.mainClass="com.chaitanya.expensetracker.ExpenseTrackerApp"
   ```

   If `mvn exec:java` says the exec plugin is unavailable, use this command instead to compile and run with Maven's dependency classpath plugin, or run the main class from IntelliJ IDEA/Eclipse after importing the project as a Maven project:

   ```bash
   mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass="com.chaitanya.expensetracker.ExpenseTrackerApp"
   ```

## Example workflow
1. Choose `1` to add an expense.
2. Enter `Lunch`, `250`, `Food`, `2026-10-21`, and `College canteen`.
3. Choose `2` to view all expenses.
4. Choose `7` and enter `2026-10` to view October 2026 totals.
5. Choose `0` to exit.

Dates must use `YYYY-MM-DD`. Month reports use `YYYY-MM`.

## Project structure
```text
SmartExpenseTracker/
├── database/schema.sql
├── src/main/java/com/chaitanya/expensetracker/
│   ├── DBConnection.java
│   ├── Expense.java
│   ├── ExpenseDAO.java
│   └── ExpenseTrackerApp.java
├── src/main/resources/db.properties.example
├── .gitignore
├── pom.xml
└── README.md
```

## How the code is organized
- `Expense`: model class representing one expense. It stores the ID, title, amount, category, date, and notes.
- `DBConnection`: creates JDBC connections using settings from `db.properties`.
- `ExpenseDAO`: Data Access Object (DAO) containing SQL operations. Keeps database logic separate from the menu.
- `ExpenseTrackerApp`: console menu, input validation, and user interaction.

## OOP concepts demonstrated
- **Encapsulation:** expense fields are private and accessed using getters.
- **Abstraction:** the menu calls methods such as `DAO.add()` without needing to know the SQL details.
- **Separation of concerns:** model, database access, and user interface are kept in separate classes.
- **Objects:** each expense is represented by an `Expense` object.

## SQL concepts demonstrated
- `INSERT` adds a record.
- `SELECT` reads and searches records.
- `UPDATE` edits an existing record.
- `DELETE` removes a record.
- `SUM` calculates totals.
- `GROUP BY` aggregates spending by category.
- `ORDER BY` ranks categories by total spending.

## Add it to GitHub
1. Create a new empty repository on GitHub named `Smart-Expense-Tracker`.
2. Open a terminal inside this project folder.
3. Run:

   ```bash
   git init
   git add .
   git commit -m "Add Smart Expense Tracker Java project"
   git branch -M main
   git remote add origin https://github.com/YOUR-USERNAME/Smart-Expense-Tracker.git
   git push -u origin main
   ```

4. Replace `YOUR-USERNAME` with your GitHub username.
5. Before pushing, confirm `src/main/resources/db.properties` is not staged or committed. Never upload your real database password.

## Possible future improvements
- Add a Java Swing or JavaFX interface
- Add login and user-specific expenses
- Export monthly reports to CSV/PDF
- Add budget limits and spending alerts
- Add charts for category-wise spending

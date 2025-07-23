package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_EXPENSE = 1001;

    private TextView tvWelcome, tvTotal;
    private LinearLayout expenseListContainer;

    private double totalExpenses = 0.0;
    private List<ExpenseItem> expenseList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is logged in, otherwise redirect to LoginActivity
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        tvWelcome = findViewById(R.id.tv_welcome);
        tvTotal = findViewById(R.id.tv_total);
        expenseListContainer = findViewById(R.id.expense_list_container);

        // Display username from Firebase Authentication with better fallback
        String userName = getUserDisplayName(currentUser);
        tvWelcome.setText("Welcome, " + userName);

        findViewById(R.id.btn_add_expense).setOnClickListener(v -> {
            // Start AddExpenseActivity for result
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EXPENSE);
        });

        updateTotalExpenses();

        TextView tvSettings = findViewById(R.id.tv_settings);
        tvSettings.setOnClickListener(v -> showSettingsMenu(v));
    }

    private String getUserDisplayName(FirebaseUser user) {
        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            return user.getDisplayName();
        } else if (user.getEmail() != null) {
            // Extract name from email (everything before @)
            String email = user.getEmail();
            return email.substring(0, email.indexOf('@'));
        } else {
            return "User";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_EXPENSE && resultCode == RESULT_OK && data != null) {
            String description = data.getStringExtra("description");
            double amount = data.getDoubleExtra("amount", 0);
            String category = data.getStringExtra("category");
            String date = data.getStringExtra("date");

            addExpense(description, amount, category, date);
            updateTotalExpenses();
        }
    }

    private void addExpense(String description, double amount, String category, String date) {
        // Create expense object
        Expense expense = new Expense(description, amount, category, date);

        // Inflate the custom expense item layout
        View expenseItemView = getLayoutInflater().inflate(R.layout.item_expense, expenseListContainer, false);

        // Find views in the inflated layout
        TextView tvDescription = expenseItemView.findViewById(R.id.tv_description);
        TextView tvCategory = expenseItemView.findViewById(R.id.tv_category);
        TextView tvDate = expenseItemView.findViewById(R.id.tv_date);
        TextView tvAmount = expenseItemView.findViewById(R.id.tv_amount);
        Button btnDelete = expenseItemView.findViewById(R.id.btn_delete);

        // Set the data to views
        tvDescription.setText(description);
        tvCategory.setText(category);
        tvDate.setText(date);
        tvAmount.setText(String.format("$%.2f", amount));

        // Set category background color based on category
        setCategoryBackground(tvCategory, category);

        // Store the expense with its view for later deletion
        ExpenseItem expenseItemObj = new ExpenseItem(expense, expenseItemView);
        expenseList.add(expenseItemObj);

        // Set delete click listener
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteExpense(expenseItemObj);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Add the view to container
        expenseListContainer.addView(expenseItemView);

        // Update total expenses value
        totalExpenses += amount;

        // Show confirmation
        Toast.makeText(this, "Expense added successfully!", Toast.LENGTH_SHORT).show();
    }

    private void setCategoryBackground(TextView tvCategory, String category) {
        int backgroundColor;
        switch (category.toLowerCase()) {
            case "food":
                backgroundColor = 0xFF10B981; // Green
                break;
            case "transport":
                backgroundColor = 0xFF3B82F6; // Blue
                break;
            case "entertainment":
                backgroundColor = 0xFFEF4444; // Red
                break;
            case "shopping":
                backgroundColor = 0xFFF59E0B; // Yellow
                break;
            case "bills":
                backgroundColor = 0xFF8B5CF6; // Purple
                break;
            case "healthcare":
                backgroundColor = 0xFFEC4899; // Pink
                break;
            case "education":
                backgroundColor = 0xFF06B6D4; // Cyan
                break;
            default: // Others
                backgroundColor = 0xFF6B7280; // Gray
                break;
        }
        tvCategory.setBackgroundColor(backgroundColor);
    }

    private void deleteExpense(ExpenseItem expenseItem) {
        // Remove from list
        expenseList.remove(expenseItem);

        // Remove view from container
        expenseListContainer.removeView(expenseItem.view);

        // Update total
        totalExpenses -= expenseItem.expense.amount;
        updateTotalExpenses();

        Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
    }

    private void updateTotalExpenses() {
        tvTotal.setText(String.format("$%.2f", totalExpenses));
    }

    private void showSettingsMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add("About");
        popupMenu.getMenu().add("Delete All Expenses");
        popupMenu.getMenu().add("Logout");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if (title.equals("About")) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("About Expense Tracker")
                        .setMessage("A simple and elegant expense tracking app to help you manage your finances.\n\nVersion 1.0\nDeveloped with ❤")
                        .setPositiveButton("OK", null)
                        .show();
                return true;
            } else if (title.equals("Delete All Expenses")) {
                if (expenseList.isEmpty()) {
                    Toast.makeText(this, "No expenses to delete", Toast.LENGTH_SHORT).show();
                    return true;
                }

                new AlertDialog.Builder(this)
                        .setTitle("Delete All Expenses")
                        .setMessage("Are you sure you want to delete all expenses? This action cannot be undone.")
                        .setPositiveButton("Delete All", (dialog, which) -> {
                            expenseList.clear();
                            expenseListContainer.removeAllViews();
                            totalExpenses = 0.0;
                            updateTotalExpenses();
                            Toast.makeText(MainActivity.this, "All expenses deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            } else if (title.equals("Logout")) {
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                            navigateToLogin();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is still logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        }
    }

    // Expense class
    public static class Expense {
        public String description;
        public double amount;
        public String category;
        public String date;

        public Expense(String description, double amount, String category, String date) {
            this.description = description;
            this.amount = amount;
            this.category = category;
            this.date = date;
        }
    }

    // Helper class to store expense with its view
    private static class ExpenseItem {
        public Expense expense;
        public View view;

        public ExpenseItem(Expense expense, View view) {
            this.expense = expense;
            this.view = view;
        }
    }
}
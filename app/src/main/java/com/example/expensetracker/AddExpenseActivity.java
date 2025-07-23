package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etDescription, etAmount, etDate;
    private Spinner spinnerCategory;
    private Button btnSelectDate, btnSave;

    private Calendar selectedDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        initViews();
        setupCategorySpinner();
        setupClickListeners();

        // Initialize with current date
        selectedDate = Calendar.getInstance();
        updateDateField();
    }

    private void initViews() {
        etDescription = findViewById(R.id.et_description);
        etAmount = findViewById(R.id.et_amount);
        etDate = findViewById(R.id.et_date);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSelectDate = findViewById(R.id.btn_select_date);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateField();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set max date to today (prevent future dates)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void updateDateField() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void saveExpense() {
        // Clear previous errors
        etDescription.setError(null);
        etAmount.setError(null);

        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String date = etDate.getText().toString().trim();

        // Validation
        if (description.isEmpty()) {
            etDescription.setError("Please enter a description");
            etDescription.requestFocus();
            return;
        }

        if (description.length() > 50) {
            etDescription.setError("Description too long (max 50 characters)");
            etDescription.requestFocus();
            return;
        }

        if (amountStr.isEmpty()) {
            etAmount.setError("Please enter an amount");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than zero");
                etAmount.requestFocus();
                return;
            }
            if (amount > 999999.99) {
                etAmount.setError("Amount is too large");
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Please enter a valid amount");
            etAmount.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send data back to MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("description", description);
        resultIntent.putExtra("amount", amount);
        resultIntent.putExtra("category", category);
        resultIntent.putExtra("date", date);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setupCategorySpinner() {
        String[] categories = {"Food", "Transport", "Entertainment", "Shopping", "Bills", "Healthcare", "Education", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        // Check if user has entered any data
        String description = etDescription.getText().toString().trim();
        String amount = etAmount.getText().toString().trim();

        if (!description.isEmpty() || !amount.isEmpty()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Discard Changes")
                    .setMessage("Are you sure you want to discard the expense data?")
                    .setPositiveButton("Discard", (dialog, which) -> {
                        setResult(RESULT_CANCELED);
                        super.onBackPressed();
                    })
                    .setNegativeButton("Continue Editing", null)
                    .show();
        } else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }
}
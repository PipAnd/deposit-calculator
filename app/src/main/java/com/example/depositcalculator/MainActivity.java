package com.example.depositcalculator;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private List<Deposit> deposits;
    private ArrayAdapter<Deposit> adapter;
    private TextView totalAmountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deposits = new ArrayList<>();
        totalAmountText = findViewById(R.id.total_amount_text);
        
        ListView listView = findViewById(R.id.deposits_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deposits);
        listView.setAdapter(adapter);

        Button addDepositButton = findViewById(R.id.add_deposit_button);
        addDepositButton.setOnClickListener(v -> showAddDepositDialog());

        updateTotalAmount();
    }

    private void showAddDepositDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить вклад");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_deposit, null);
        builder.setView(dialogView);

        EditText amountEdit = dialogView.findViewById(R.id.amount_edit);
        EditText interestEdit = dialogView.findViewById(R.id.interest_edit);
        Button openDateButton = dialogView.findViewById(R.id.open_date_button);
        Button closeDateButton = dialogView.findViewById(R.id.close_date_button);

        Calendar openCalendar = Calendar.getInstance();
        Calendar closeCalendar = Calendar.getInstance();
        closeCalendar.add(Calendar.YEAR, 1);

        openDateButton.setText(formatDate(openCalendar.getTime()));
        closeDateButton.setText(formatDate(closeCalendar.getTime()));

        DatePickerDialog.OnDateSetListener openDateListener = (view, year, month, dayOfMonth) -> {
            openCalendar.set(year, month, dayOfMonth);
            openDateButton.setText(formatDate(openCalendar.getTime()));
        };

        DatePickerDialog.OnDateSetListener closeDateListener = (view, year, month, dayOfMonth) -> {
            closeCalendar.set(year, month, dayOfMonth);
            closeDateButton.setText(formatDate(closeCalendar.getTime()));
        };

        openDateButton.setOnClickListener(v -> new DatePickerDialog(
                MainActivity.this, openDateListener,
                openCalendar.get(Calendar.YEAR),
                openCalendar.get(Calendar.MONTH),
                openCalendar.get(Calendar.DAY_OF_MONTH)).show());

        closeDateButton.setOnClickListener(v -> new DatePickerDialog(
                MainActivity.this, closeDateListener,
                closeCalendar.get(Calendar.YEAR),
                closeCalendar.get(Calendar.MONTH),
                closeCalendar.get(Calendar.DAY_OF_MONTH)).show());

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            try {
                double amount = Double.parseDouble(amountEdit.getText().toString());
                double interest = Double.parseDouble(interestEdit.getText().toString());
                
                Deposit deposit = new Deposit(amount, interest, 
                        openCalendar.getTime(), closeCalendar.getTime());
                deposits.add(deposit);
                adapter.notifyDataSetChanged();
                updateTotalAmount();
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Введите корректные данные", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.create().show();
    }

    private String formatDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.format("%02d.%02d.%04d", 
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));
    }

    private void updateTotalAmount() {
        double total = 0;
        for (Deposit deposit : deposits) {
            total += deposit.getAmount();
        }
        totalAmountText.setText(String.format("Общая сумма вкладов: %.2f", total));
    }
}

package com.example.depositcalculator;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity {
    private List<Deposit> deposits;
    private ArrayAdapter<Deposit> adapter;
    private TextView totalAmountText;
    private TextView currentAmountText;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deposits = new ArrayList<>();
        totalAmountText = findViewById(R.id.total_amount_text);
        currentAmountText = findViewById(R.id.current_amount_text); // Новый TextView для текущей суммы
        sharedPreferences = getSharedPreferences("deposit_data", Context.MODE_PRIVATE);
        gson = new Gson();
        
        // Загрузим сохраненные данные
        loadDeposits();
        
        ListView listView = findViewById(R.id.deposits_list);
        adapter = new ArrayAdapter<Deposit>(this, android.R.layout.simple_list_item_1, deposits) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                Deposit deposit = deposits.get(position);
                text.setText(String.format(
                    "Вклад: %.2f %s, %.2f%%\nТекущая сумма: %.5f %s\n" +
                    "Рост: %.6f руб/сек, %.4f руб/мин, %.2f руб/час\n" +
                    "Рост за периоды: %.2f/день, %.2f/неделю, %.2f/месяц",
                    deposit.getAmount(),
                    deposit.getCurrency(),
                    deposit.getInterestRate(),
                    deposit.getCurrentAmount(),
                    deposit.getCurrency(),
                    deposit.getGrowthPerSecond(),
                    deposit.getGrowthPerMinute(),
                    deposit.getGrowthPerHour(),
                    deposit.getGrowthPerDay(),
                    deposit.getGrowthPerWeek(),
                    deposit.getGrowthPerMonth()
                ));
                return view;
            }
        };
        listView.setAdapter(adapter);

        Button addDepositButton = findViewById(R.id.add_deposit_button);
        addDepositButton.setOnClickListener(v -> showAddDepositDialog());

        updateTotalAmount();

        // Анимация текущей суммы
        startCurrentAmountAnimation();
    }

    private void startCurrentAmountAnimation() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // Обновляем каждую секунду
                    runOnUiThread(() -> {
                        double currentTotal = 0;
                        for (Deposit deposit : deposits) {
                            currentTotal += deposit.getCurrentAmount();
                        }
                        currentAmountText.setText(String.format(
                            "Текущая сумма с процентами: %.5f руб.", 
                            currentTotal
                        ));
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Сохраним данные при выходе из приложения
        saveDeposits();
    }

    private void saveDeposits() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String depositsJson = gson.toJson(deposits);
        editor.putString("deposits", depositsJson);
        editor.apply();
    }

    private void loadDeposits() {
        String depositsJson = sharedPreferences.getString("deposits", "");
        if (!depositsJson.isEmpty()) {
            try {
                Type listType = new TypeToken<ArrayList<Deposit>>(){}.getType();
                List<Deposit> savedDeposits = gson.fromJson(depositsJson, listType);
                if (savedDeposits != null) {
                    deposits.clear();
                    deposits.addAll(savedDeposits);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showAddDepositDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить вклад");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_deposit, null);
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
                
                // Добавим валюту по умолчанию (рубли)
                Deposit deposit = new Deposit(amount, interest, 
                        openCalendar.getTime(), closeCalendar.getTime(), "руб.");
                deposits.add(deposit);
                adapter.notifyDataSetChanged();
                updateTotalAmount();
                saveDeposits(); // Сохраняем данные после добавления
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Введите корректные данные", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.create().show();
    }

    private void showEditDepositDialog(int position) {
        Deposit deposit = deposits.get(position);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать вклад");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_deposit, null);
        builder.setView(dialogView);

        EditText amountEdit = dialogView.findViewById(R.id.amount_edit);
        EditText interestEdit = dialogView.findViewById(R.id.interest_edit);
        Button openDateButton = dialogView.findViewById(R.id.open_date_button);
        Button closeDateButton = dialogView.findViewById(R.id.close_date_button);

        // Заполним поля текущими значениями
        amountEdit.setText(String.valueOf(deposit.getAmount()));
        interestEdit.setText(String.valueOf(deposit.getInterestRate()));
        
        Calendar openCalendar = Calendar.getInstance();
        openCalendar.setTime(deposit.getOpenDate());
        
        Calendar closeCalendar = Calendar.getInstance();
        closeCalendar.setTime(deposit.getCloseDate());

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

        openDate…

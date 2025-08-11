package com.example.depositcalculator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Deposit implements Serializable {
    private double amount;
    private double interestRate;
    private Date openDate;
    private Date closeDate;

    public Deposit(double amount, double interestRate, Date openDate, Date closeDate) {
        this.amount = amount;
        this.interestRate = interestRate;
        this.openDate = openDate;
        this.closeDate = closeDate;
    }

    // Геттеры
    public double getAmount() { return amount; }
    public double getInterestRate() { return interestRate; }
    public Date getOpenDate() { return openDate; }
    public Date getCloseDate() { return closeDate; }
    
    // Сеттеры
    public void setAmount(double amount) { this.amount = amount; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    public void setOpenDate(Date openDate) { this.openDate = openDate; }
    public void setCloseDate(Date closeDate) { this.closeDate = closeDate; }
    
    // Расчет дохода за весь период
    public double getProfit() {
        long diffInMillis = closeDate.getTime() - openDate.getTime();
        long days = diffInMillis / (24 * 60 * 60 * 1000);
        return amount * (interestRate / 100) * (days / 365.0);
    }
    
    // Расчет текущего дохода с капитализацией (ежедневная капитализация)
    public double getCurrentProfit() {
        Date currentDate = new Date();
        // Если вклад уже закрыт, возвращаем полный доход
        if (currentDate.after(closeDate)) {
            return getProfit();
        }
        // Если вклад еще не открыт, возвращаем 0
        if (currentDate.before(openDate)) {
            return 0;
        }
        // Расчет дохода на текущую дату
        long diffInMillis = currentDate.getTime() - openDate.getTime();
        long days = diffInMillis / (24 * 60 * 60 * 1000);
        
        // Формула сложных процентов: A = P(1 + r/365)^t
        double dailyRate = interestRate / 100 / 365;
        double currentAmount = amount * Math.pow(1 + dailyRate, days);
        return currentAmount - amount;
    }
    
    // Текущая сумма с учетом капитализации
    public double getCurrentAmount() {
        return amount + getCurrentProfit();
    }
    
    public String getFormattedOpenDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(openDate);
    }
    
    public String getFormattedCloseDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(closeDate);
    }
    
    public String getFormattedCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    @Override
    public String toString() {
        return String.format("Вклад: %.2f руб., %.2f%%\n%s - %s\nТекущая сумма: %.2f руб.", 
                amount, interestRate, getFormattedOpenDate(), getFormattedCloseDate(), getCurrentAmount());
    }
}

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
    
    public double getProfit() {
        long diffInMillis = closeDate.getTime() - openDate.getTime();
        long days = diffInMillis / (24 * 60 * 60 * 1000);
        return amount * (interestRate / 100) * (days / 365.0);
    }
    
    public String getFormattedOpenDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(openDate);
    }
    
    public String getFormattedCloseDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(closeDate);
    }
    
    @Override
    public String toString() {
        return String.format("Вклад: %.2f руб., %.2f%%, %s - %s", 
                amount, interestRate, getFormattedOpenDate(), getFormattedCloseDate());
    }
}

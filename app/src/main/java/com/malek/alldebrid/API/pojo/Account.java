package com.malek.alldebrid.API.pojo;


import java.util.Date;

public class Account {

    private String username;
    private String emailAdress;
    private Date accountExpirationDate;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailAdress() {
        return emailAdress;
    }

    public void setEmailAdress(String emailAdress) {
        this.emailAdress = emailAdress;
    }

    public Date getAccountExpirationDate() {
        return accountExpirationDate;
    }

    public void setAccountExpirationDate(Date accountExpirationDate) {
        this.accountExpirationDate = accountExpirationDate;
    }

    public int daysLeft() {
        if (accountExpirationDate == null)
            return 0;
        return (int) ((accountExpirationDate.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24));
    }
}

package com.malek.alldebrid.API.pojo;


import java.util.Date;

public class Account {

    private String username;
    private String password;
    private String type;
    private String emailAdress;
    private String cookie;
    private Date expirationDate;
    private Date lastLogin;

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

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

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public int daysLeft() {
        if (expirationDate == null)
            return 0;
        return (int) ((expirationDate.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24));
    }
}

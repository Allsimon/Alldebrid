package com.malek.alldebrid.ui.adapter;

public abstract class NavDrawerItem {
    public static final int NO_ICON = -1;
    public static final boolean SECOND_TEXT_VISIBLE = true;
    private String title;
    private int icon;
    private String count = "0";
    // boolean to set visibility of the counter
    private boolean isCounterVisible = false;

    public NavDrawerItem() {
    }

    public NavDrawerItem(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public NavDrawerItem(String title, int icon,
                         String count) {
        this.title = title;
        this.icon = icon;
        this.isCounterVisible = true;
        this.count = count;
    }

    public NavDrawerItem(String title, int icon,
                         int count) {
        this.title = title;
        this.icon = icon;
        this.isCounterVisible = true;
        this.count = count + "";
    }

    public abstract void onClick();

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return this.icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = "" + count;
    }

    public boolean getCounterVisibility() {
        return this.isCounterVisible;
    }

    public void setCounterVisibility(boolean isCounterVisible) {
        this.isCounterVisible = isCounterVisible;
    }

    public void setCount(String count) {
        this.count = count;
    }

}
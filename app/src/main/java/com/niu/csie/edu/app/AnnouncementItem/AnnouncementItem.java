package com.niu.csie.edu.app.AnnouncementItem;

public class AnnouncementItem {

    private String title;
    private String date;
    private String href;

    public AnnouncementItem(String title, String date, String href) {
        this.title = title;
        this.date = date;
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getHref() {
        return href;
    }
}

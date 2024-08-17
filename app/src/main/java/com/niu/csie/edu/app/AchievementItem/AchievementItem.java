package com.niu.csie.edu.app.AchievementItem;

public class AchievementItem {
    private int imageResId;
    private String title;
    private String description;
    private int imageResultResId;

    public AchievementItem(int imageResId, String title, String description, int imageResultResId) {
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
        this.imageResultResId = imageResultResId;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResultResId() {
        return imageResultResId;
    }

    public void setImageResultResId(int imageResultResId) {
        this.imageResultResId = imageResultResId;
    }

}

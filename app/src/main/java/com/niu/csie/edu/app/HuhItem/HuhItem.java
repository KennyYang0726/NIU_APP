package com.niu.csie.edu.app.HuhItem;

public class HuhItem {
    private int imageResId;
    private String mainText;
    private String subText;

    public HuhItem(int imageResId, String mainText, String subText) {
        this.imageResId = imageResId;
        this.mainText = mainText;
        this.subText = subText;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getMainText() {
        return mainText;
    }

    public String getSubText() {
        return subText;
    }
}

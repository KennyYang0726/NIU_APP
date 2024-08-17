package com.niu.csie.edu.app.ScoreQuote;

public class ScoreQuote {
    private String type;
    private String lesson;
    private String score;

    public ScoreQuote(String type, String lesson, String score) {
        this.type = type;
        this.lesson = lesson;
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public String getLesson() {
        return lesson;
    }

    public String getScore() {
        return score;
    }
}

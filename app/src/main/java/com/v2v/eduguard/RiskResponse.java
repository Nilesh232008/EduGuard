package com.v2v.eduguard;

public class RiskResponse {

    private int risk;        // % value
    private String level;    // LOW / MEDIUM / HIGH

    public int getRisk() {
        return risk;
    }

    public String getLevel() {
        return level;
    }
}
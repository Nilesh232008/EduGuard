package com.v2v.eduguard;

public class Student {

    public String id, name;

    public boolean present = false;

    // 🔥 ML DATA
    public float attendance = 0;
    public float marks = 0;
    public float behavior = 1;
    public boolean feesPaid = true;

    public int riskScore = 0;
    public String riskLevel = "LOW";

    public Student(){}

    public Student(String id, String name){
        this.id = id;
        this.name = name;
    }
}
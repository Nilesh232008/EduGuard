package com.v2v.eduguard;

public class Student {

    public String id, name;

    // 🔥 ATTENDANCE
    public boolean present = false;

    // 🔥 HOMEWORK / ASSIGNMENT
    public boolean homeworkDone = false;

    // 🔥 ML DATA
    public float attendance = 0;   // percentage
    public float marks = 0;
    public float behavior = 1;
    public boolean feesPaid = true;

    // 🔥 EXTRA (for your assignment logic)
    public int assignments = 0; // number of completed assignments

    // 🔥 RISK
    public int riskScore = 0;
    public String riskLevel = "LOW";

    public Student(){}

    public Student(String id, String name){
        this.id = id;
        this.name = name;
    }
}
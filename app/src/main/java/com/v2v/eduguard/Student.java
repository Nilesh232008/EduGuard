package com.v2v.eduguard;

public class Student {

    public String id;
    public String name;
    public boolean present = true;

    public int riskScore = 0;
    public String riskLevel = "LOW";

    public Student(){}

    public Student(String id, String name){
        this.id = id;
        this.name = name;
        this.present = true;
    }
}




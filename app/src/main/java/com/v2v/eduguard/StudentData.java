package com.v2v.eduguard;
public class StudentData {

    public float attendance;
    public float marks;
    public float behavior;
    public float fees;
    public float assignments;

    public StudentData() {}

    public StudentData(float attendance, float marks, float behavior, float fees, float assignments) {
        this.attendance = attendance;
        this.marks = marks;
        this.behavior = behavior;
        this.fees = fees;
        this.assignments = assignments;
    }
}
package com.v2v.eduguard;

public class Faculty {

    public String name;
    public String subject;
    public String className;

    public Faculty() {
        // Needed for Firebase
    }

    public Faculty(String name, String subject, String className) {
        this.name = name;
        this.subject = subject;
        this.className = className;
    }
}


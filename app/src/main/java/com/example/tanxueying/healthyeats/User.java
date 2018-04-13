package com.example.tanxueying.healthyeats;

public class User {

    private String username;
    private String gender;
    private String age;
    private String height;
    private String weight;
    private String exercise;
    private String goal_cal;
    private String food_cal;

    public User() {

    }

    public User(String username, String gender, String age, String height, String weight, String exercise) {
        this.username = username;
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.exercise = exercise;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getExercise() {
        return exercise;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public String getGoal_cal() {
        return goal_cal;
    }

    public void setFood_cal(String food_cal) {
        this.food_cal = food_cal;
    }

    public String getFood_cal() {
        return food_cal;
    }


    public void setCal() {
        Double h = Double.parseDouble(this.height) * 2.54;
        Double w = Double.parseDouble(this.weight) / 2.2;
        Integer a = Integer.parseInt(this.age);
        Integer e = Integer.parseInt(this.exercise);
        Double c = new Double(0);
        if (this.gender.equals("Male")) {
            c = 66.47+ (13.75 * w) + (5.0 * h) - (6.75 * a) + e;
        } else {
            c = 665.09 + (9.56 * w) + (1.84 * h) - (4.67 * a) + e;
        }
        this.goal_cal = c.intValue() + "";
    }
}
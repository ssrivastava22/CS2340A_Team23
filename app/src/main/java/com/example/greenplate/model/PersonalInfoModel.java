package com.example.greenplate.model;
import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PersonalInfoModel {
    private String height;
    private String weight;
    private String gender;
    public PersonalInfoModel(String height, String weight, String gender) {
        this.height = height;
        this.weight = weight;
        this.gender = gender;
    }

    public String getHeight() {
        return height;
    }

    public String getWeight() {
        return weight;
    }

    public String getGender() { return gender; }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
}

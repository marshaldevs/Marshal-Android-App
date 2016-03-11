package com.basmach.marshal.entities;

import android.content.Context;
import android.widget.ImageView;
import com.basmach.marshal.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class Course {
    private long id;
    private String courseCode;
    private String name;
    private int minimumPeople;
    private int maximumPeople;
    private String description;
    private String targetPopulation;
    private String professionalDomain;
    private String syllabus;
    private String dayTime;
    private int durationInHours;
    private int durationInDays;
    private int passingGrade;
    private double price;
    private ArrayList cycles;
    private String photoUrl;
    private Boolean isMooc;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinimumPeople() {
        return minimumPeople;
    }

    public void setMinimumPeople(int minimumPeople) {
        this.minimumPeople = minimumPeople;
    }

    public int getMaximumPeople() {
        return maximumPeople;
    }

    public void setMaximumPeople(int maximumPeople) {
        this.maximumPeople = maximumPeople;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetPopulation() {
        return targetPopulation;
    }

    public void setTargetPopulation(String targetPopulation) {
        this.targetPopulation = targetPopulation;
    }

    public String getProfessionalDomain() {
        return professionalDomain;
    }

    public void setProfessionalDomain(String professionalDomain) {
        this.professionalDomain = professionalDomain;
    }

    public String getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    public String getDayTime() {
        return dayTime;
    }

    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }

    public int getDurationInHours() {
        return durationInHours;
    }

    public void setDurationInHours(int durationInHours) {
        this.durationInHours = durationInHours;
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }

    public int getPassingGrade() {
        return passingGrade;
    }

    public void setPassingGrade(int passingGrade) {
        this.passingGrade = passingGrade;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ArrayList getCycles() {
        return cycles;
    }

    public void setCycles(ArrayList cycles) {
        this.cycles = cycles;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Boolean getIsMooc() {
        return isMooc;
    }

    public void setIsMooc(Boolean isMooc) {
        this.isMooc = isMooc;
    }

    public void getPhotoViaPicasso(Context context, ImageView imageView) {
        Picasso.with(context).load(this.getPhotoUrl())
                .into(imageView);
    }

}

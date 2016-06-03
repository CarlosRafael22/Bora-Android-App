package com.example.rafael.boraapp.models;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by rafael on 27/05/16.
 */
public class Activity {

    private String id;
    private String title;
    private String category;
    private String author_id;
    private String date;
    private String place;
    private String place_LatLng;
    private Date updated_at;
    private ArrayList<String> comments;

    public Activity(){

    }

    public Activity(String id, String title, String category, String author_id){
        this.id = id;
        this.title = title;
        this.category = category;
        this.author_id = author_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPlace_LatLng() {
        return place_LatLng;
    }

    public void setPlace_LatLng(String place_LatLng) {
        this.place_LatLng = place_LatLng;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }


}

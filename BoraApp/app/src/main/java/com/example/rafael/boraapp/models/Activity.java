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
    private Date date;
    private String place;
    private String place_LatLng;
    private Date updatedAt;
    private ArrayList<String> comments;

    public Activity(){

    }

    public Activity(String id, String title, String category, String author_id){
        this.id = id;
        this.title = title;
        this.category = category;
        this.author_id = author_id;
    }


    public Activity(String id, String title, String category, String author_id, Date date, String place){
        this.id = id;
        this.title = title;
        this.category = category;
        this.author_id = author_id;
        this.date = date;
        this.place = place;
    }

    public Activity(String id, String title, String category, String author_id, Date date, String place, Date updated_at){
        this.id = id;
        this.title = title;
        this.category = category;
        this.author_id = author_id;
        this.date = date;
        this.place = place;
        this.updatedAt = updated_at;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updated_at) {
        this.updatedAt = updated_at;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }


}

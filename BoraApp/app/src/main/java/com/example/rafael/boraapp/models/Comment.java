package com.example.rafael.boraapp.models;

import java.util.Date;

/**
 * Created by rafael on 27/05/16.
 */
public class Comment {

    private String comment;
    private String author_id;
    private String id;
    private Date updated_at;

    public Comment(){

    }

    public Comment(String comment, String author, String id){
        this.comment = comment;
        this.author_id = author;
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

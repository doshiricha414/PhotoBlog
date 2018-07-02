package com.example.photoblog;


import java.util.Date;

public class BlogPost {
    public String user_id,image_url,description;
    public Date timestamp;


    public BlogPost(){

    }

    public BlogPost(String user_id, String image_url, String description,Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}

package com.abcexample.myassistant;

/**
 * Created by divya on 26-01-2018.
 */

public class Users {
    private String Name;
    private String Status;
    private String thumbnail;

    public Users(){

    }

    public Users(String name, String status, String thumbnail) {
        Name = name;
        Status = status;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}

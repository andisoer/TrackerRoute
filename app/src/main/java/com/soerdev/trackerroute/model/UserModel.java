package com.soerdev.trackerroute.model;

public class UserModel {
    private String id, username, link_foto;

    public UserModel() {

    }

    public UserModel(String id, String username, String link_foto) {
        this.id = id;
        this.username = username;
        this.link_foto = link_foto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLink_foto() {
        return link_foto;
    }

    public void setLink_foto(String link_foto) {
        this.link_foto = link_foto;
    }
}

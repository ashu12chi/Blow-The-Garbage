package com.npdevs.blowthegarbage;

public class Garbage {

    private String description;
    private boolean severe;
    private boolean organic;
    private double latitude;
    private double longitude;
    private int upvotes;
    private boolean verified;
    private String url;

    public Garbage(){
        //empty
    }

    public Garbage(String description, boolean severe, boolean organic, double latitude, double longitude, int upvotes,boolean verified,String url)
    {
        if(description.trim().equals("")){
            description = "No Name";
        }
        this.description = description;
        this.severe = severe;
        this.organic = organic;
        this.latitude=latitude;
        this.longitude=longitude;
        this.upvotes=upvotes;
        this.verified=verified;
        this.url=url;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean getSevere(){
        return severe;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean getOrganic() {
        return organic;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public boolean getVerified() {
        return verified;
    }

    public void setDescription(String name){
        this.description = name;
    }

    public void setSevere(boolean severe){
        this.severe = severe;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setOrganic(boolean organic) {
        this.organic = organic;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}

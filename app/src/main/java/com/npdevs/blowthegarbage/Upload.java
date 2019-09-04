package com.npdevs.blowthegarbage;

public class Upload {
    private String description;
    private String severe;
    private String organic;

    public String getOrganic() {
        return organic;
    }

    public void setOrganic(String organic) {
        this.organic = organic;
    }

    public Upload(){
        //empty
    }
    public Upload(String description,String severe,String organic)
    {
        if(description.trim().equals("")){
            description = "No Name";
        }
        this.description = description;
        this.severe = severe;
        this.organic = organic;
    }
    public String getDescription()
    {
        return description;
    }
    public String getSevere(){
        return severe;
    }
    public void setDescription(String name){
        this.description = name;
    }
    public void setSevere(String imageUrl){
        this.severe = severe;
    }
}

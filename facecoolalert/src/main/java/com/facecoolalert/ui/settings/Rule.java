package com.facecoolalert.ui.settings;

public class Rule {

    private String name;
    private Integer brLow;
    private Integer brHigh;
    private Integer sharpness;
    private Integer size;
    private Integer absYaw;

    public Rule(String name, Integer brLow, Integer brHigh, Integer sharpness, Integer size, Integer absYaw) {
        this.name = name;
        this.brLow = brLow;
        this.brHigh = brHigh;
        this.sharpness = sharpness;
        this.size = size;
        this.absYaw = absYaw;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBrLow() {
        return brLow;
    }

    public void setBrLow(Integer brLow) {
        this.brLow = brLow;
    }

    public Integer getBrHigh() {
        return brHigh;
    }

    public void setBrHigh(Integer brHigh) {
        this.brHigh = brHigh;
    }

    public Integer getSharpness() {
        return sharpness;
    }

    public void setSharpness(Integer sharpness) {
        this.sharpness = sharpness;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getAbsYaw() {
        return absYaw;
    }

    public void setAbsYaw(Integer absYaw) {
        this.absYaw = absYaw;
    }
}

package com.prj.app;

import java.util.Date;

public class Scan implements Comparable<Scan> {
    String bssid;
    Double distance;
    Date timestamp;

    public Scan(String bssid, Double distance, Date timestamp) {
        this.bssid = bssid;
        this.distance = distance;
        this.timestamp = timestamp;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(Scan o) {
        if (getTimestamp() == null || o.getTimestamp() == null)
            return 0;
        return getTimestamp().compareTo(o.getTimestamp());
    }
}

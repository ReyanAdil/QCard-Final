package com.qcard.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SharableWrapper implements Serializable {

    private String version;
    private String authorId;
    private ArrayList<SharableQSet> sharableQSets;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public ArrayList<SharableQSet> getSharableQSets() {
        return sharableQSets;
    }

    public void setSharableQSets(ArrayList<SharableQSet> sharableQSets) {
        this.sharableQSets = sharableQSets;
    }

    public void addSharableQSet(SharableQSet sharableQSet) {
        if (sharableQSets == null) sharableQSets = new ArrayList<>();
        sharableQSets.add(sharableQSet);
    }
}

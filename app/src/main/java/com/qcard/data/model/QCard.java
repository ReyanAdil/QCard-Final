package com.qcard.data.model;

import com.qcard.data.GlobalData;

import java.io.Serializable;

public class QCard implements Serializable {
    private String userId;
    private String id;
    private String setId;
    private String term;
    private String definition;
    private String termImage;
    private String definitionImage;
    private String tagId;
    private long time;
    private boolean remembered;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getTermImage() {
        return termImage;
    }

    public void setTermImage(String termImage) {
        this.termImage = termImage;
    }

    public String getDefinitionImage() {
        return definitionImage;
    }

    public void setDefinitionImage(String definitionImage) {
        this.definitionImage = definitionImage;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isRemembered() {
        return remembered;
    }

    public void setRemembered(boolean remembered) {
        this.remembered = remembered;
    }
}

package com.qcard.data.model;

import java.io.Serializable;

public class SharableQCard implements Serializable {
    private String term;
    private String definition;
    private String termImage;
    private String definitionImage;

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
}

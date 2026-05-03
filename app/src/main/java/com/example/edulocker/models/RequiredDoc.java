package com.example.edulocker.models;

public class RequiredDoc {
    private String docTypeName; // matches DocumentType.name
    private boolean mandatory;  // true = blocks submit if missing, false = optional

    public RequiredDoc() {}

    public RequiredDoc(String docTypeName, boolean mandatory) {
        this.docTypeName = docTypeName;
        this.mandatory = mandatory;
    }

    public String getDocTypeName() { return docTypeName; }
    public void setDocTypeName(String docTypeName) { this.docTypeName = docTypeName; }
    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
}

package com.example.edulocker.models;

import com.google.firebase.Timestamp;

public class DocumentType {
    private String docTypeId;
    private String name;       // canonical name e.g. "Aadhaar Card"
    private boolean mandatory; // true = mandatory for scholarship, false = optional
    private Timestamp createdAt;

    public DocumentType() {}

    public DocumentType(String name, boolean mandatory) {
        this.name = name;
        this.mandatory = mandatory;
    }

    public String getDocTypeId() { return docTypeId; }
    public void setDocTypeId(String docTypeId) { this.docTypeId = docTypeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

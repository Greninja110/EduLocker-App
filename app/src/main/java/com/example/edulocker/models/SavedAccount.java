package com.example.edulocker.models;

public class SavedAccount {
    private String uid;
    private String email;
    private String password;
    private String role;
    private String name;
    private String alias; // user-given nickname

    public SavedAccount() {}

    public SavedAccount(String uid, String email, String password,
                        String role, String name, String alias) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
        this.alias = alias;
    }

    public String getUid()      { return uid; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public String getRole()     { return role; }
    public String getName()     { return name; }
    public String getAlias()    { return alias; }

    public void setUid(String uid)           { this.uid = uid; }
    public void setEmail(String email)       { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role)         { this.role = role; }
    public void setName(String name)         { this.name = name; }
    public void setAlias(String alias)       { this.alias = alias; }

    /** Label shown in UI — alias if set, otherwise name. */
    public String getDisplayName() {
        return (alias != null && !alias.trim().isEmpty()) ? alias : name;
    }
}

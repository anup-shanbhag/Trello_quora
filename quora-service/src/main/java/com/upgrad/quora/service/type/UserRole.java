package com.upgrad.quora.service.type;

public enum UserRole {
    ADMIN ("ADMIN"), REGULAR ("NONADMIN");
    private String textRole;
    UserRole(String textRole){
        this.textRole = textRole;
    }
    public String getRole(){
        return this.textRole;
    }
}

package com.upgrad.quora.service.constants;

public enum UserStatus {
    USER_REGISTERED("USER SUCCESSFULLY REGISTERED"),
    SIGNIN_SUCCESSFUL("SIGNED IN SUCCESSFULLY"),
    SIGNOUT_SUCCESSFUL("SIGNED OUT SUCCESSFULLY"),
    USER_DELETED("USER SUCCESSFULLY DELETED");

    String textStatus;

    UserStatus(String textStatus) {
        this.textStatus = textStatus;
    }

    public String getStatus() {
        return this.textStatus;
    }
}

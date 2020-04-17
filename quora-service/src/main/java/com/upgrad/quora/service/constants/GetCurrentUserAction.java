package com.upgrad.quora.service.constants;

public enum GetCurrentUserAction {

    CREATE_ANSWER("CREATE_ANSWER"),
    EDIT_ANSWER("EDIT_ANSWER"),
    DELETE_ANSWER("DELETE_ANSWER"),
    GET_ALL_ANSWER("GET_ALL_ANSWER"),
    GET_USER_DETAILS("GET_USER_DETAILS"),
    DELETE_USER("DELETE_USER"),
    CREATE_QUESTION("CREATE_QUESTION"),
    GET_ALL_QUESTIONS("GET_ALL_QUESTIONS"),
    EDIT_QUESTION("EDIT_QUESTION"),
    DELETE_QUESTION("DELETE_QUESTION"),
    GET_ALL_QUESTIONS_BY_USER("GET_ALL_QUESTIONS_BY_USER");



    private String textAction;

    GetCurrentUserAction(String s) {
    }

    public String getTextAction() {
        return textAction;
    }
}

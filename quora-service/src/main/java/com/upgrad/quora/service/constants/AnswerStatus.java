package com.upgrad.quora.service.constants;

public enum AnswerStatus {

    ANSWER_CREATED("ANSWER CREATED"),ANSWER_EDITED("ANSWER EDITED");
    private String textStatus;

    AnswerStatus(String textStatus) {
        this.textStatus = textStatus;
    }

    public String getTextStatus() {
        return textStatus;
    }
}

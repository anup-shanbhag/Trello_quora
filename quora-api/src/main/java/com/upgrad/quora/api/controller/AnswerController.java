package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerEditRequest;
import com.upgrad.quora.api.model.AnswerEditResponse;
import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.constants.AnswerStatus;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class AnswerController {

    @Autowired
    AnswerBusinessService answerBusinessService;

    @Autowired
    UserBusinessService userBusinessService;

    @Autowired
    QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create",
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse>
    createAnswer(@RequestHeader("authorization") final String authorization,
                 AnswerRequest answerRequest,@PathVariable("questionId") final String questionID)
        throws AuthorizationFailedException, InvalidQuestionException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization,"Bearer ") : authorization;
        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setAns(answerRequest.getAnswer());
        answerEntity.setDate(LocalDate.now());
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setUser(userBusinessService.getCurrentUser(token));
        try{
            QuestionEntity question = questionService.getQuestion(questionID);
            answerEntity.setQuestion(question);
        }
        catch(InvalidQuestionException e){
            throw new InvalidQuestionException("QUES-001","The question entered is invalid");
        }
        AnswerEntity createdAnswer = answerBusinessService.createAnswer(answerEntity);
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswer.getUuid()).status(AnswerStatus.ANSWER_CREATED.getTextStatus());
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }


    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}",
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse>
    editAnswer(@RequestHeader("authorization") final String authorization,
               AnswerEditRequest answerEditRequest, @PathVariable("answerId") final String answerId)
        throws AnswerNotFoundException,AuthorizationFailedException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization,"Bearer ") : authorization;
        AnswerEntity answerEntity;
        try {
            answerEntity = answerBusinessService.getAnswer(answerId);
        }catch (AnswerNotFoundException ANF){
            throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist");
        }
        if (!answerEntity.getUser().getUuid().equals(userBusinessService.getCurrentUser(token).getUuid())) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
        }
        answerEntity.setAns(answerEditRequest.getContent());
        AnswerEntity editedAnswer = answerBusinessService.editAnswer(answerEntity);
        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(editedAnswer.getUuid()).status(AnswerStatus.ANSWER_EDITED.getTextStatus());
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }
}




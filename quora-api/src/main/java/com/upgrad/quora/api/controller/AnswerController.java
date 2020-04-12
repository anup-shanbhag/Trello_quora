package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.constants.AnswerStatus;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
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
        String[] bearerToken = authorization.split("Bearer ");
        if (bearerToken.length < 2) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setAns(answerRequest.getAnswer());
        answerEntity.setDate(LocalDate.now());
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setUser(userBusinessService.getCurrentUser(bearerToken[1]));
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
    public ResponseEntity<AnswerResponse>
    editAnswer(@RequestHeader("authorization") final String authorization,
                   AnswerRequest answerRequest,@PathVariable("answerId") final String answerId)
        throws AuthorizationFailedException, InvalidQuestionException, UserNotFoundException {
        String[] bearerToken = authorization.split("Bearer ");
        if (bearerToken.length < 2) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        return null;
    }


}

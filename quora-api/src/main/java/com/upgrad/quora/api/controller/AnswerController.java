package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.constants.AnswerStatus;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
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
        UserEntity user = userBusinessService.getCurrentUser(token);
        AnswerEntity answerEntity = answerBusinessService.getAnswer(answerId);
        answerEntity.setAns(answerEditRequest.getContent());
        AnswerEntity editedAnswer = answerBusinessService.editAnswer(answerEntity,user);
        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(editedAnswer.getUuid()).status(AnswerStatus.ANSWER_EDITED.getTextStatus());
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path ="/answer/delete/{answerId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnsuwer(@RequestHeader("authorization") final String authorization,@PathVariable("answerId") String answerId) throws AuthorizationFailedException, AnswerNotFoundException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization,"Bearer ") : authorization;
        UserEntity user = userBusinessService.getCurrentUser(token);
        AnswerEntity answerEntity = answerBusinessService.getAnswer(answerId);
        answerId = answerBusinessService.deleteAnswer(answerEntity,user);
        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse();
        answerDeleteResponse.setId(answerId);
        answerDeleteResponse.setStatus(AnswerStatus.ANSWER_DELETED.getTextStatus());
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    }
}




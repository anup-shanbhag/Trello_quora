package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.constants.QuestionStatus;
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
public class QuestionController {

    @Autowired
    QuestionService questionService;

    @Autowired
    UserBusinessService userService;

    /**
     * This is used to create a question in the application which will be shown to all  users. It takes input for content of the question & authorization token and creates the question in the database.
     * @param authorization Authorization token from request header
     * @param request An input request with question content
     * @return Response Entity with questionId, message and Http Status Code
     * @throws AuthorizationFailedException if the authorization token is invalid, expired or not found
     */
    @RequestMapping(path="/question/create", method= RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(@RequestHeader("authorization") String authorization, QuestionRequest request) throws AuthorizationFailedException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization,"Bearer ") : authorization;
        UserEntity user = userService.getCurrentUser(token);
        QuestionEntity question = new QuestionEntity();
        question.setContent(request.getContent());
        question.setDate(LocalDate.now());
        question.setUuid(UUID.randomUUID().toString());
        question.setUser(user);
        question = questionService.createQuestion(question);
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getUuid());
        response.setStatus(QuestionStatus.QUESTION_CREATED.getStatus());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * This is used to edit a question that has been posted by a user. Note, only the question owner or an admin can edit a question. It takes questionId, question content and authorization token to find and update a question in the database.
     * @param authorization Authorization token from request header
     * @param questionId Id of the question to delete
     * @param request An input request with question content
     * @return Response Entity with questionId, message and Http Status Code
     * @throws AuthorizationFailedException if the authorization token is invalid, expired or not found.
     * @throws AuthorizationFailedException if a non-admin non-owner(question) attempts to edit a question.
     * @throws InvalidQuestionException if a question with input questionId doesn't exist
     */
    @RequestMapping(path="/question/edit/{questionId}",method=RequestMethod.PUT,consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestion(@RequestHeader("authorization") String authorization, @PathVariable("questionId")String questionId, QuestionEditRequest request) throws AuthorizationFailedException, InvalidQuestionException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization,"Bearer ") : authorization;
        UserEntity user = userService.getCurrentUser(token);
        QuestionEntity question = questionService.getQuestion(questionId);
        question.setContent(request.getContent());
        questionId = questionService.editQuestion(question, user);
        QuestionEditResponse response = new QuestionEditResponse();
        response.setId(questionId);
        response.setStatus(QuestionStatus.QUESTION_EDITED.getStatus());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    /**
     * This is used to delete a question that has been posted by a user. Note, only the question owner or an admin can delete a question. It takes questionId and authorization token to find and delete a question in the database.
     * @param authorization Authorization token from request header
     * @param questionId Id of the question to delete
     * @return Response Entity with questionId, message and Http Status Code
     * @throws AuthorizationFailedException if the authorization token is invalid, expired or not found.
     * @throws AuthorizationFailedException if a non-admin non-owner(question) attempts to delete a question.
     * @throws InvalidQuestionException if a question with input questionId doesn't exist
     */
    @RequestMapping(path="/question/delete/{questionId}",method=RequestMethod.DELETE,produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(@RequestHeader("authorization") String authorization, @PathVariable("questionId")String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization,"Bearer ") : authorization;
        UserEntity user = userService.getCurrentUser(token);
        QuestionEntity question = questionService.getQuestion(questionId);
        questionId = questionService.deleteQuestion(question, user);
        QuestionDeleteResponse response = new QuestionDeleteResponse();
        response.setId(questionId);
        response.setStatus(QuestionStatus.QUESTION_DELETED.getStatus());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }



}
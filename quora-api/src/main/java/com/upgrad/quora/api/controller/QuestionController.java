package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.StringTokenizer;
import java.util.UUID;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    QuestionService questionService;

    @Autowired
    UserBusinessService userService;

    /**
     * This is used to create a question in the application which will be shown to all  users. Any user who is signed-in can access this endpoint. It uses HTTP POST method JSON Payload. It takes input for content of the question & authorization token and creates the question in the database.
     * @param authorization Authorization token from request header
     * @param request An input request with question content
     * @return Response Entity with questionId, message and Http Status Code
     * @throws AuthorizationFailedException if the authorization token is invalid, expired or not found
     */
    @RequestMapping(path="/question/create", method= RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(@RequestHeader("authorization") String authorization, QuestionRequest request) throws AuthorizationFailedException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization,"Bearer ") : authorization;
        UserEntity user = userService.getLoggedInUser(token);
        QuestionEntity question = new QuestionEntity();
        question.setContent(request.getContent());
        question.setDate(LocalDate.now());
        question.setUuid(UUID.randomUUID().toString());
        question.setUser(user);
        question = questionService.createQuestion(question);
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getUuid());
        response.setStatus("QUESTION CREATED");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }



}

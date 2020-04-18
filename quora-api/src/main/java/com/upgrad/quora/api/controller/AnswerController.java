package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.constants.AnswerStatus;
import com.upgrad.quora.service.constants.GetCurrentUserAction;
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
import java.util.ArrayList;
import java.util.List;
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

    /**
     * This is used to create an answer in the application for a given question.
     *Any logged in user can add a answer to an existing question
     * @param authorization Authorization token from request header
     * @param answerRequest  An input request with Answer content
     * @return answerResponse with  message and Http Status Code
     * @throws AuthorizationFailedException if the authorization token is invalid,
     *                                      expired or not found
     * @throws InvalidQuestionException if question uuid given is invalid.
     */
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
        answerEntity.setUser(userBusinessService.getCurrentUser(token, GetCurrentUserAction.CREATE_ANSWER));
        QuestionEntity question;
        try {
             question = questionService.getQuestion(questionID);
        }catch (InvalidQuestionException iqe){
            throw new InvalidQuestionException("QUES-001","The question entered is invalid");
        }
        answerEntity.setQuestion(question);
        AnswerEntity createdAnswer = answerBusinessService.createAnswer(answerEntity);
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswer.getUuid()).status(AnswerStatus.ANSWER_CREATED.getTextStatus());
        return new ResponseEntity<>(answerResponse, HttpStatus.CREATED);
    }


    /**
     * This is used to edit an answer for a given answer id
     * only the owner of the answer can edit the answer
     * @param authorization Authorization token from request header
     * @param answerEditRequest  An input request with edited Answer content
     * @return answerEditResponse with  message and Http Status Code
     * @throws AuthorizationFailedException if the authorization token is invalid,
     *                                      expired or not found
     * @throws AnswerNotFoundException if answer uuid given is invalid.
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}",
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse>
    editAnswer(@RequestHeader("authorization") final String authorization,
               AnswerEditRequest answerEditRequest, @PathVariable("answerId") final String answerId)
        throws AnswerNotFoundException,AuthorizationFailedException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization,"Bearer ") : authorization;
        UserEntity user = userBusinessService.getCurrentUser(token,GetCurrentUserAction.EDIT_ANSWER);
        AnswerEntity answerEntity = answerBusinessService.getAnswer(answerId);
        answerEntity.setAns(answerEditRequest.getContent());
        AnswerEntity editedAnswer = answerBusinessService.editAnswer(answerEntity,user);
        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(editedAnswer.getUuid())
            .status(AnswerStatus.ANSWER_EDITED.getTextStatus());
        return new ResponseEntity<>(answerEditResponse, HttpStatus.OK);
    }



    /**
     * This is used to delete an answer for a given answer id
     * only the owner of the answer can or an admin can delete the answer
     * @param authorization Authorization token from request header
     * @param answerId  An input request to delete the answer.
     * @return answerDeleteResponse with  message and Http Status Code
     * @throws AuthorizationFailedException if the authorization token is invalid,
     *                                      expired or not found
     * @throws AnswerNotFoundException if answer uuid given is invalid.
     */
    @RequestMapping(method = RequestMethod.DELETE, path ="/answer/delete/{answerId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(@RequestHeader("authorization") final String authorization,
                                                              @PathVariable("answerId") String answerId)
        throws AuthorizationFailedException, AnswerNotFoundException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization,"Bearer ") : authorization;
        UserEntity user = userBusinessService.getCurrentUser(token,GetCurrentUserAction.DELETE_ANSWER);
        AnswerEntity answerEntity = answerBusinessService.getAnswer(answerId);
        answerId = answerBusinessService.deleteAnswer(answerEntity,user);
        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse();
        answerDeleteResponse.setId(answerId);
        answerDeleteResponse.setStatus(AnswerStatus.ANSWER_DELETED.getTextStatus());
        return new ResponseEntity<>(answerDeleteResponse, HttpStatus.OK);
    }

    /**
     * This is used to delete an answer for a given answer id
     * only the owner of the answer can or an admin can delete the answer
     * @param authorization Authorization token from request header
     * @param questionId  An input request to get all the answers for it.
     * @return list of AnswerDetailsResponse with  message and Http Status Code
     * @throws AuthorizationFailedException if the authorization token is invalid,
     *                                      expired or not found
     * @throws InvalidQuestionException if answer uuid given is invalid.
     */
    @RequestMapping(method = RequestMethod.GET, path ="/answer/all/{questionId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion(@RequestHeader("authorization") final String authorization,
                                                                                @PathVariable("questionId") String questionId)
        throws AuthorizationFailedException, InvalidQuestionException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization,"Bearer ") : authorization;
        UserEntity user = userBusinessService.getCurrentUser(token,GetCurrentUserAction.GET_ALL_ANSWER);
        QuestionEntity question;
        try{
             question = questionService.getQuestion(questionId);
        }catch(InvalidQuestionException invalidQuestionException ){
            throw new InvalidQuestionException("QUES-001","The question with entered uuid whose details are to be seen does not exist");
        }
        List<AnswerEntity> answerEntityList = answerBusinessService.getAllAnswersToQuestion(questionId);
        List<AnswerDetailsResponse> answerDetailsResponse = new ArrayList<>();
        answerEntityList.forEach(answer ->
            answerDetailsResponse.add(
                new AnswerDetailsResponse()
                    .id(answer.getUuid())
                    .questionContent(question.getContent())
                    .answerContent(answer.getAns())));
        return new ResponseEntity<>(answerDetailsResponse, HttpStatus.OK);
    }
}




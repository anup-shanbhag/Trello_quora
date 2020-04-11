package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    @Autowired
    QuestionDao questionDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion (QuestionEntity question){
        return questionDao.createQuestion(question);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteQuestion (String questionId, UserEntity user) throws InvalidQuestionException, AuthorizationFailedException {
        QuestionEntity question = questionDao.getQuestion(questionId);
        if(question!=null && (user.getId().equals(question.getUser().getId()) || user.getRole().equalsIgnoreCase("admin"))){
            questionDao.deleteQuestion(question);
            return question.getUuid();
        }
        else if(question==null){
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }
        else{
            throw new AuthorizationFailedException("ATHR-003","Only the question owner or admin can delete the question");
        }
    }


}

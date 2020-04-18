package com.upgrad.quora.service.business;

import com.upgrad.quora.service.constants.UserRole;
import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.upgrad.quora.service.constants.ErrorConditions.*;

@Service
public class AnswerBusinessService {

  @Autowired
  AnswerDao answerDao;

  @Autowired
  UserBusinessService userBusinessService;

  @Autowired
  QuestionService questionService;

  /**
   * Method takes a answerEntity as input
   *
   * @param answerEntity
   * @return Answer entity created in the database for this request
   *
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public AnswerEntity createAnswer(AnswerEntity answerEntity){
    return answerDao.createAnswer(answerEntity);
  }

  /**
   * Method takes a answerId as input
   *
   * @param answerId
   * @return Answer entity from the database where uuid=answerId
   * @throws AnswerNotFoundException
   *
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public AnswerEntity getAnswer (String answerId) throws AnswerNotFoundException {
    AnswerEntity answer = answerDao.getAnswer(answerId);
    if(answer==null){
      throw new AnswerNotFoundException(ANS_NOT_FOUND.getCode(),ANS_NOT_FOUND.getMessage());
    } else {
      return answer;
    }
  }

  /**
   * Method takes a answerEntity,user as input
   *
   * @param answerEntity,user
   * @return Answer entity from the database where uuid=answerId
   * @throws AuthorizationFailedException
   *
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public AnswerEntity editAnswer(AnswerEntity answerEntity, UserEntity user) throws AuthorizationFailedException {
    if (!answerEntity.getUser().getUuid().equals(user.getUuid())) {
      throw new AuthorizationFailedException(ANS_EDIT_UNAUTHORIZED.getCode(), ANS_EDIT_UNAUTHORIZED.getMessage());
    }
    AnswerEntity editedAnswer = answerDao.editAnswer(answerEntity);
    return editedAnswer;
  }

  /**
   * Method takes a answerEntity,user as input
   *
   * @param answerEntity,user
   * @return uuid of the answer that was deleted
   * @throws AuthorizationFailedException
   *
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public String deleteAnswer(AnswerEntity answerEntity,UserEntity user) throws AuthorizationFailedException {
    if (!answerEntity.getUser().getUuid().equals(user.getUuid()) && !user.getRole().equals(UserRole.ADMIN.getRole())){
      throw new AuthorizationFailedException(ANS_DELETE_UNAUTHORIZED.getCode(), ANS_DELETE_UNAUTHORIZED.getMessage());
    }
    answerDao.deleteAnswer(answerEntity);
    return answerEntity.getUuid();
  }

  /**
   * Method takes a answerEntity,user as input
   *
   * @param questionId
   * @return list of Answer entity from the database where uuid=questionId
   *
   *
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public List<AnswerEntity> getAllAnswersToQuestion(String questionId){
    List<AnswerEntity> answerList = answerDao.getAllAnswersToQuestion(questionId);
    return  answerList;
  }

}

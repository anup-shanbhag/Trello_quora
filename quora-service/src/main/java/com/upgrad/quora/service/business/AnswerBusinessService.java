package com.upgrad.quora.service.business;

import com.upgrad.quora.service.constants.UserRole;
import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnswerBusinessService {

  @Autowired
  AnswerDao answerDao;

  @Autowired
  UserBusinessService userBusinessService;

  @Transactional(propagation = Propagation.REQUIRED)
  public AnswerEntity createAnswer(AnswerEntity answerEntity){
    return answerDao.createAnswer(answerEntity);
  }


  @Transactional(propagation = Propagation.REQUIRED)
  public AnswerEntity getAnswer (String answerId) throws AnswerNotFoundException {
    AnswerEntity answer = answerDao.getAnswer(answerId);
    if(answer==null){
      throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist");
    } else {
      return answer;
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AnswerEntity editAnswer(AnswerEntity answerEntity, UserEntity user) throws AuthorizationFailedException {
    if (!answerEntity.getUser().getUuid().equals(user.getUuid())) {
      throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
    }
    AnswerEntity editedAnswer = answerDao.editAnswer(answerEntity);
    return editedAnswer;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public String deleteAnswer(AnswerEntity answerEntity,UserEntity user) throws AuthorizationFailedException {
    if (!answerEntity.getUser().getUuid().equals(user.getUuid()) && !user.getRole().equals(UserRole.ADMIN.getRole())){
      throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
    }
    answerDao.deleteAnswer(answerEntity);
    return answerEntity.getUuid();
  }
}

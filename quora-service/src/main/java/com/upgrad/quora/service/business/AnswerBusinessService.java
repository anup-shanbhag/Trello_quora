package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnswerBusinessService {

  @Autowired
  AnswerDao answerDao;

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
  public AnswerEntity editAnswer(AnswerEntity answerEntity){
    AnswerEntity editedAnswer = answerDao.editAnswer(answerEntity);
    return editedAnswer;
  }
}

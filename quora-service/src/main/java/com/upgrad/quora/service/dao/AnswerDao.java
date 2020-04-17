package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Method takes a answerEntity as input
     *
     * @param answerEntity
     * @return Answer entity created in the database for this request
     *
     */
    public AnswerEntity createAnswer(AnswerEntity answerEntity){
        entityManager.persist(answerEntity);
        return answerEntity;
    }

    /**
     * Method takes a answerId as input
     *
     * @param answerId
     * @return Answer entity from the database where uuid=answerId
     * returns null id answer with given id is not found
     *
     */
    public AnswerEntity getAnswer(String answerId){
        try{
            return entityManager.createNamedQuery("Answer.getById",AnswerEntity.class).setParameter("answerId",answerId).getSingleResult();
        }catch(NoResultException nre){
            return null;
        }
    }

    /**
     * Method takes a answerEntity as input
     *
     * @param answerEntity
     * @return Answer entity that has been updated.
     *
     *
     */
    public AnswerEntity editAnswer(AnswerEntity answerEntity){
        entityManager.merge(answerEntity);
        return answerEntity;
    }


    /**
     * Method takes a answerEntity as input
     *
     * @param answerEntity
     * @return uuid of the answer that was deleted
     *
     *
     */
    public void deleteAnswer(AnswerEntity answerEntity){
        entityManager.remove(answerEntity);
    }

    /**
     * Method takes a answerEntity as input
     *
     * @param questionId
     * @returnlist of answer entities for the given question ID
     *
     *
     */
    public List<AnswerEntity> getAllAnswersToQuestion(String questionId){
        return entityManager.createNamedQuery("Answers.fetchAllPerQuestion",AnswerEntity.class).setParameter("questionId",questionId).getResultList();
    }

}

package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.Optional;

@Repository
public class QuestionDao {

    @PersistenceContext
    EntityManager entityManager;

    /**
     * Method takes a question entity as a parameter and creates/stores it in the database
     * @param question Question to be created/stored
     * @return created question entity
     */
    public QuestionEntity createQuestion(QuestionEntity question){
        entityManager.persist(question);
        return question;
    }

    /**
     * Method takes question id as a parameter, and fetches a question from the database having the same id.
     * @param questionId Question to fetch
     * @return question question having id=questionId, null if no such question exists in the database
     */
    public QuestionEntity getQuestion(String questionId){
        try{
            return entityManager.createNamedQuery("Questions.getById",QuestionEntity.class).setParameter("questionId",questionId).getSingleResult();
        }
        catch(NoResultException e){
            return null;
        }
    }

    /**
     * Method takes a question entity as a parameter and removes it from the database (merge)
     * @param question Question to be removed
     */
    public void deleteQuestion(QuestionEntity question){
        entityManager.remove(question);
    }

    /**
     * Method takes a question entity as a parameter and updates it in the database (merge)
     * @param question Question to be updated
     */
    public void updateQuestion(QuestionEntity question){
        entityManager.merge(question);
    }
}

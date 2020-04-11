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

    public QuestionEntity createQuestion(QuestionEntity question){
        entityManager.persist(question);
        return question;
    }

    public QuestionEntity getQuestion(String questionId){
        try{
            return entityManager.createNamedQuery("Questions.getById",QuestionEntity.class).setParameter("questionId",questionId).getSingleResult();
        }
        catch(NoResultException e){
            return null;
        }
    }
    public void deleteQuestion(QuestionEntity question){
        entityManager.remove(question);
    }
}

/**
 * This JAVA class implements question service handling business
 * logic for creating, reading, updating, deleting and listing questions
 * @author  Anup Shanbhag (shanbhaganup@gmail.com)
 * @version 1.0
 * @since   2020-04-16
 */

package com.upgrad.quora.service.business;

import java.util.List;

import com.upgrad.quora.service.constants.ErrorConditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.upgrad.quora.service.constants.UserRole;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;

import static com.upgrad.quora.service.constants.ErrorConditions.*;

@Service
public class QuestionService {

	@Autowired
	QuestionDao questionDao;

	/**
	 * Method takes a question entity and stores it in the database
	 * 
	 * @param question Question to store in the database
	 * @return Created question entity
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public QuestionEntity createQuestion(QuestionEntity question) {
		return questionDao.createQuestion(question);
	}

	/**
	 * Method takes a questionId as a parameter and fetches the entity from database
	 * 
	 * @param questionId
	 * @return Question entity from the database table with id = questionId
	 * @throws InvalidQuestionException
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public QuestionEntity getQuestion(String questionId) throws InvalidQuestionException {
		QuestionEntity question = questionDao.getQuestion(questionId);
		if (question == null) {
			throw new InvalidQuestionException(QUES_NOT_FOUND.getCode(), QUES_NOT_FOUND.getMessage());
		} else {
			return question;
		}
	}

	/**
	 * Method takes question and user entities as parameters and updates the
	 * question in the database if the user is the question owner
	 * 
	 * @param question Question to to be removed
	 * @param user     Logged in User
	 * @return Id of the updated question
	 * @throws AuthorizationFailedException if logged in user is not the question
	 *                                      owner
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public String editQuestion(QuestionEntity question, UserEntity user) throws AuthorizationFailedException {
		if (user.getId().equals(question.getUser().getId())) {
			questionDao.updateQuestion(question);
			return question.getUuid();
		} else {
			throw new AuthorizationFailedException(QUES_EDIT_UNAUTHORIZED.getCode(),
					QUES_EDIT_UNAUTHORIZED.getMessage());
		}
	}

	/**
	 * Method takes question and user entities as parameters and removes the
	 * question from the database if the user is an admin or the question owner
	 * 
	 * @param question Question to to be removed
	 * @param user     Logged in User
	 * @return Id of the deleted question
	 * @throws AuthorizationFailedException if logged in user is neither an admin
	 *                                      nor the question owner
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public String deleteQuestion(QuestionEntity question, UserEntity user) throws AuthorizationFailedException {
		if (user.getId().equals(question.getUser().getId())
				|| user.getRole().equalsIgnoreCase(UserRole.ADMIN.getRole())) {
			questionDao.deleteQuestion(question);
			return question.getUuid();
		} else {
			throw new AuthorizationFailedException(QUES_DELETE_UNAUTHORIZED.getCode(),
					QUES_DELETE_UNAUTHORIZED.getMessage());
		}
	}

	/**
	 * Method returns a list of all questions available in the database irrespective
	 * of owner or posted user
	 * 
	 * @return a list of all questions available in the database
	 */
	public List<QuestionEntity> getAllQuestions() {
		return questionDao.getAllQuestions();
	}

	/**
	 * Method returns a list of all questions posted by a specific user
	 * 
	 * @param user a user whose questions are to be fetched
	 * @return a list of questions posted by the input user
	 */
	public List<QuestionEntity> getUserQuestions(UserEntity user) {
		return questionDao.getUserQuestions(user);
	}

}

package com.upgrad.quora.service.business;

import com.upgrad.quora.service.constants.GetCurrentUserAction;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static com.upgrad.quora.service.constants.ErrorConditions.*;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    /**
     * Method takes a userUuid as a parameter and fetches the user entity from database
     *
     * @param userUuid
     * @param authorizationToken
     * @return User entity from the database table with uuid = userUuid
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity getUser(final String userUuid, final String authorizationToken, GetCurrentUserAction action)
            throws AuthorizationFailedException, UserNotFoundException {
        UserEntity currentUser = getCurrentUser(authorizationToken, action);
        UserEntity userEntity = userDao.getUser(userUuid);
        if (userEntity == null) {
            switch (action){
                case GET_ALL_QUESTIONS_BY_USER: throw new UserNotFoundException(QUES_GET_USR_NOT_FOUND.getCode(), QUES_GET_USR_NOT_FOUND.getMessage());
                default: throw new UserNotFoundException(USER_NOT_FOUND.getCode(), USER_NOT_FOUND.getMessage());
            }
        }
        return userEntity;
    }

    /**
     * Method takes authorization token as input and return the current logged in user.
     * @param action action for which current user is needed
     * @param authorizationToken User's authorization token
     * @return Returns current logged in user
     * @throws AuthorizationFailedException if the authorization token is invalid, expired or not found
     * @author Anup Shanbhag (shanbhaganup@gmail.com)
     */
    public UserEntity getCurrentUser(String authorizationToken, GetCurrentUserAction action ) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException(USER_NOT_SIGNED_IN.getCode(), USER_NOT_SIGNED_IN.getMessage());
        } else if ((userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(LocalDateTime.now()))
                || userAuthEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            switch(action){
                case CREATE_ANSWER : throw new AuthorizationFailedException(ANS_CREATE_AUTH_FAILURE.getCode(),ANS_CREATE_AUTH_FAILURE.getMessage());
                case EDIT_ANSWER : throw new AuthorizationFailedException(ANS_EDIT_AUTH_FAILURE.getCode(),ANS_EDIT_AUTH_FAILURE.getMessage());
                case DELETE_ANSWER : throw new AuthorizationFailedException(ANS_DELETE_AUTH_FAILURE.getCode(),ANS_DELETE_AUTH_FAILURE.getMessage());
                case GET_ALL_ANSWER : throw new AuthorizationFailedException(ANS_GET_AUTH_FAILURE.getCode(),ANS_GET_AUTH_FAILURE.getMessage());
                case DELETE_USER: throw new AuthorizationFailedException(USER_SIGNED_OUT.getCode(),USER_SIGNED_OUT.getMessage());
                case CREATE_QUESTION: throw new AuthorizationFailedException(QUES_CREATE_AUTH_FAILURE.getCode(),QUES_CREATE_AUTH_FAILURE.getMessage());
                case GET_ALL_QUESTIONS: throw new AuthorizationFailedException(QUES_GET_ALL_AUTH_FAILURE.getCode(),QUES_GET_ALL_AUTH_FAILURE.getMessage());
                case EDIT_QUESTION: throw new AuthorizationFailedException(QUES_EDIT_AUTH_FAILURE.getCode(),QUES_EDIT_AUTH_FAILURE.getMessage());
                case DELETE_QUESTION: throw new AuthorizationFailedException(QUES_DELETE_AUTH_FAILURE.getCode(),QUES_DELETE_AUTH_FAILURE.getMessage());
                case GET_ALL_QUESTIONS_BY_USER: throw new AuthorizationFailedException(QUES_GET_AUTH_FAILURE.getCode(),QUES_GET_AUTH_FAILURE.getMessage());
                default: throw new AuthorizationFailedException(USER_GET_AUTH_FAILURE.getCode(),USER_GET_AUTH_FAILURE.getMessage());
            }
        } else {
            return userAuthEntity.getUser();
        }
    }

    /**
     * Method takes a user entity and stores it in the database
     *
     * @param newUser User profile to be stored in the database
     * @return Created user entity
     */
    public UserEntity registerUser(UserEntity newUser) throws SignUpRestrictedException {
        try {
            String[] encryptedText = passwordCryptographyProvider.encrypt(newUser.getPassword());
            newUser.setSalt(encryptedText[0]);
            newUser.setPassword(encryptedText[1]);
            return userDao.registerUser(newUser);
        }catch (DataIntegrityViolationException dataIntegrityViolationException) {
            if (dataIntegrityViolationException.getCause() instanceof ConstraintViolationException) {
                String constraintName = ((ConstraintViolationException) dataIntegrityViolationException.getCause()).getConstraintName();
                if (StringUtils.containsIgnoreCase(constraintName, "userName")) {
                    throw new SignUpRestrictedException(USERNAME_ALREADY_EXISTS.getCode(), USERNAME_ALREADY_EXISTS.getMessage());
                } else {
                    throw new SignUpRestrictedException(USER_EMAIL_ALREADY_EXISTS.getCode(), USER_EMAIL_ALREADY_EXISTS.getMessage());
                }
            } else {
                throw dataIntegrityViolationException;
            }
        }
    }

    /**
     * Method takes a user's username & password, validates and generates new access token
     *
     * @param userName,password validates in database
     * @return generated access token
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authenticateUser(final String userName, final String password) throws AuthenticationFailedException {
        UserEntity userEntity = userDao.getUserByUserName(userName);
        if (userEntity == null) {
            throw new AuthenticationFailedException(USERNAME_NOT_FOUND.getCode(), USERNAME_NOT_FOUND.getMessage());
        }

        String encrypedPassword = PasswordCryptographyProvider.encrypt(password, userEntity.getSalt());

        if (encrypedPassword.equals(userEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encrypedPassword);
            UserAuthEntity userAuthEntity = new UserAuthEntity();
            userAuthEntity.setUser(userEntity);
            userAuthEntity.setUuid(userEntity.getUuid());

            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);

            userAuthEntity.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));

            userAuthEntity.setLoginAt(now);
            userAuthEntity.setExpiresAt(expiresAt);

            userDao.createAuthToken(userAuthEntity);
            userAuthEntity.setLoginAt(now);
            return userAuthEntity;
        } else {
            throw new AuthenticationFailedException(USER_WRONG_PASSWORD.getCode(), USER_WRONG_PASSWORD.getMessage());
        }
    }

    /**
     * Method takes authorization token as input and sign-out user.
     *
     * @param authorizationToken User's authorization token
     * @return Signed-out user entity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signoutUser(String authorizationToken) throws SignOutRestrictedException {   //signOutUser
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorizationToken);

        if (userAuthEntity == null || (userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(LocalDateTime.now()))
                || userAuthEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new SignOutRestrictedException(USER_HAS_SIGNED_OUT.getCode(), USER_HAS_SIGNED_OUT.getMessage());
        } else {
            userAuthEntity.setLogoutAt(LocalDateTime.now());
            userDao.signoutUser(userAuthEntity);
            return userAuthEntity.getUser();
        }
    }

    /**
     * Method takes userId & access token as input and delete user.
     *
     * @param token  User's authorization token
     * @param userId uuid of user to be deleted
     * @return uuid of deleted user
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteUser(String userId, String token)
            throws AuthorizationFailedException, UserNotFoundException {
            UserEntity adminUser = this.getCurrentUser(token,GetCurrentUserAction.DELETE_USER);
            if (!adminUser.getRole().equals("admin")) {
                throw new AuthorizationFailedException(USER_DELETE_UNAUTHORIZED.getCode(), USER_DELETE_UNAUTHORIZED.getMessage());
            } else {
                UserEntity user = userDao.getUser(userId);
                if (user == null) {
                    throw new UserNotFoundException(USER_DELETE_USR_NOT_FOUND.getCode(), USER_DELETE_USR_NOT_FOUND.getMessage());
                } else {
                    userDao.deleteUser(user);
                }
                return userId;
            }
    }
}
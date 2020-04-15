package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

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
     * @return User entity from the database table with uuid = userUuid
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity getUser(final String userUuid, final String authorizationToken)
            throws AuthorizationFailedException, UserNotFoundException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if ((userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(LocalDateTime.now()))
                || userAuthEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
        } else {
            UserEntity userEntity = userDao.getUser(userUuid);
            if (userEntity == null) {
                throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
            }
            return userEntity;
        }
    }

    /**
     * Method takes authorization token as input and return the current logged in user.
     *
     * @param authorizationToken User's authorization token
     * @return Returns current logged in user
     * @throws AuthorizationFailedException if the authorization token is invalid, expired or not found.
     */
    public UserEntity getCurrentUser(String authorizationToken) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if ((userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(LocalDateTime.now()))
                || userAuthEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
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
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity registerUser(UserEntity newUser) {
        String[] encryptedText = passwordCryptographyProvider.encrypt(newUser.getPassword());
        newUser.setSalt(encryptedText[0]);
        newUser.setPassword(encryptedText[1]);

        return userDao.registerUser(newUser);
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
            throw new AuthenticationFailedException("ATN-001", "This username does not exist");
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
            throw new AuthenticationFailedException("ATN-002", "Password failed");
        }
    }

    /**
     * Method takes authorization token as input and sign-out user.
     *
     * @param authorizationToken User's authorization token
     * @return Signed-out user entity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signoutUser(String authorizationToken) throws SignOutRestrictedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorizationToken);

        if(userAuthEntity == null || (userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(LocalDateTime.now()))
                || userAuthEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }else {
            userAuthEntity.setLogoutAt(LocalDateTime.now());
            userDao.signoutUser(userAuthEntity);
            return userAuthEntity.getUser();
        }
    }
}
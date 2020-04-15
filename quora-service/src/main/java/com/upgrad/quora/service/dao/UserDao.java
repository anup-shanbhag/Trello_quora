package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Method takes user uuid as a parameter, and fetches a user entiry from the database having the same id.
     *
     * @param userUuid User to fetch
     * @return user, user having uuid=userUuId, null if no such user exists in the database
     */
    public UserEntity getUser(final String userUuid) {
        try {
            return entityManager.createNamedQuery("userByUuid", UserEntity.class)
                    .setParameter("uuid", userUuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Method takes user username as a parameter, and fetches a user entiry from the database having the same username.
     *
     * @param userName User to fetch
     * @return user entity, user having username=userName, null if no such user exists in the database
     */
    public UserEntity getUserByUserName(final String userName) {
        try {
            return entityManager.createNamedQuery("userByUserName", UserEntity.class)
                    .setParameter("userName", userName)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Method takes authorizationToken as a parameter, and fetches a user_auth entiry from the database having the same access token
     *
     * @param authorizationToken UserAuth to fetch
     * @return UserAuth, userAuth having accesstoken=authorizationToken, null if no such userAuth exists in the database
     */
    public UserAuthEntity getUserAuthToken(final String authorizationToken) {
        try {
            return entityManager.createNamedQuery("userAuthTokenByAccessToken", UserAuthEntity.class)
                    .setParameter("accessToken", authorizationToken)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Method takes user entiry as a parameter, and creates it in the database
     *
     * @param newUser, user profile to be created
     * @return userEntity, created user profile
     */
    public UserEntity registerUser(final UserEntity newUser) {
        entityManager.persist(newUser);
        return newUser;
    }

    /**
     * Method takes user auth entity as a parameter, and creates it in the database
     *
     * @param userAuthEntity, user auth entity to be created
     * @return userEntity, created user profile
     */
    public UserAuthEntity createAuthToken(UserAuthEntity userAuthEntity) {
        entityManager.persist(userAuthEntity);
        return userAuthEntity;
    }

    /**
     * Method takes user auth entity as a parameter, and updates it in the database
     *
     * @param userAuthEntity, user auth entity with updated LogoutAt time
     * @return userEntity of signed-out user profile
     */
    public UserEntity signoutUser(UserAuthEntity userAuthEntity) {
        entityManager.merge(userAuthEntity);
        return userAuthEntity.getUser();
    }
}

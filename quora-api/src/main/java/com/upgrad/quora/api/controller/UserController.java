package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.constants.UserStatus;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;


@RestController
@RequestMapping("/")
public class UserController {
    @Autowired
    private UserBusinessService userBusinessService;

    /**
     * This is used to register a user in the application. It takes input for content of the user and register in the database
     *
     * @param user An input request with user content
     * @return Response Entity with userUuId, message and Http Status Code
     * @throws SignUpRestrictedException if the username or email already exists
     */
    @RequestMapping(method = RequestMethod.POST,
            path = "/user/signup",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> registerUser(SignupUserRequest user) throws SignUpRestrictedException {
        UserEntity newUserEntiry = new UserEntity();

        newUserEntiry.setFirstName(user.getFirstName());
        newUserEntiry.setLastName(user.getLastName());
        newUserEntiry.setUserName(user.getUserName());
        newUserEntiry.setEmail(user.getEmailAddress());
        newUserEntiry.setPassword(user.getPassword());
        newUserEntiry.setCountry(user.getCountry());
        newUserEntiry.setAboutMe(user.getAboutMe());
        newUserEntiry.setDob(user.getDob());
        newUserEntiry.setContactNumber(user.getContactNumber());
        newUserEntiry.setRole("nonadmin");
        newUserEntiry.setUuid(UUID.randomUUID().toString());
        newUserEntiry.setSalt("quora123");

        UserEntity createdUser = userBusinessService.registerUser(newUserEntiry);
        SignupUserResponse userResponse = new SignupUserResponse()
                .id(createdUser.getUuid())
                .status(UserStatus.USER_REGISTERED.getStatus());
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    /**
     * This is used to signin a user. It takes input user's basic auth credential, validates and generate access token
     *
     * @param authorization An input request with user's basic auth credential
     * @return Response Entity with generated access token, message and Http Status Code
     * @throws AuthenticationFailedException if the user's basic auth is invalid
     */
    @RequestMapping(method = RequestMethod.POST,
            path = "/user/signin",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signinUser(@RequestHeader("authorization") String authorization)
            throws AuthenticationFailedException, IllegalArgumentException {
        try {

            String authorizationKey = (authorization.contains("Basic ")) ?
                    StringUtils.substringAfter(authorization, "Basic ") : authorization;

            byte[] decode = Base64.getDecoder().decode(authorizationKey);
            String decodeText = new String(decode);
            String[] decodedArray = decodeText.split(":");
            UserAuthEntity userAuthEntity = userBusinessService.authenticateUser(decodedArray[0], decodedArray[1]);

            SigninResponse signinResponse = new SigninResponse();
            signinResponse.setId(userAuthEntity.getAccessToken());
            signinResponse.setMessage(UserStatus.SIGNIN_SUCCESSFUL.getStatus());
            return new ResponseEntity<>(signinResponse, HttpStatus.OK);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException iae) {
            throw new AuthenticationFailedException("ATN-002", "Password failed");
        }
    }

    /**
     * This is used to sign-out a user. It takes input access token, validates, sign-out user and LogoutAt time
     *
     * @param authorization Access token from request header
     * @return Response Entity with signed-out user's uuid, message and Http Status Code
     * @throws SignOutRestrictedException if authorization is invalid
     */
    @RequestMapping(method = RequestMethod.POST,
            path = "/user/signout",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signoutUser(@RequestHeader("authorization") String authorization)
            throws SignOutRestrictedException {
        String token = (authorization.contains("Bearer ")) ?
                StringUtils.substringAfter(authorization, "Bearer ") : authorization;

        UserEntity userEntity = userBusinessService.signoutUser(token);
        SignoutResponse signoutResponse = new SignoutResponse();
        signoutResponse.setId(userEntity.getUuid());
        signoutResponse.setMessage(UserStatus.SIGNOUT_SUCCESSFUL.getStatus());

        return new ResponseEntity<>(signoutResponse, HttpStatus.OK);

    }
}

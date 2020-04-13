package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class CommonController {

    @Autowired
    private UserBusinessService userBusinessService;

    /**
     * This is used to get a user in an application. It takes input of user's uuid & authorization token.
     *
     * @param authorization Authorization token from request header
     * @param userId        UUID of an query user
     * @return Response Entity with user profile, message and Http Status Code
     * @throws AuthorizationFailedException if the authorization token is invalid, expired or not found
     * @throws UserNotFoundException        if the uuid is not found
     */
    @RequestMapping(method = RequestMethod.GET, path = "/userprofile/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> getUserProfile(@RequestHeader("authorization") final String authorization,
                                                              @PathVariable("userId") final String userId)
            throws AuthorizationFailedException, UserNotFoundException {
        String token = (authorization.contains("Bearer ")) ? StringUtils.substringAfter(authorization, "Bearer ") : authorization;

        UserEntity userEntity = userBusinessService.getUser(userId, token);

        UserDetailsResponse userDetailsResponse = new UserDetailsResponse()
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .userName(userEntity.getUserName())
                .emailAddress(userEntity.getEmail())
                .country(userEntity.getCountry())
                .aboutMe(userEntity.getAboutMe())
                .dob(userEntity.getDob())
                .contactNumber(userEntity.getContactNumber());

        return new ResponseEntity<>(userDetailsResponse, HttpStatus.OK);
    }
}

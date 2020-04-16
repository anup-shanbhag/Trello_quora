package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.UserBusinessService;
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
public class AdminController {

    @Autowired
    UserBusinessService userBusinessService;

    /**
     * This is used to delete a user by the admin. It takes input uuid of the user to be deleted, admin's authorization,
     * validates and deletes the user
     *
     * @param userId         An UUID of the user to be deleted
     * @param authorization, access token of admin user to execute operation
     * @return Response Entity with uuid of deleted user, message and Http Status Code
     * @throws AuthorizationFailedException if the authorization token is invalid, expired or not found
     * @throws UserNotFoundException        if the uuid is not found
     */

    @RequestMapping(method = RequestMethod.DELETE,
            path = "/admin/user/{userId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> deleteUser(@PathVariable("userId") String userId,
                                                         @RequestHeader("authorization") String authorization)
            throws AuthorizationFailedException, UserNotFoundException {
        String token = (authorization.contains("Bearer ")) ?
                StringUtils.substringAfter(authorization, "Bearer ") : authorization;
        String deletedUserId = userBusinessService.deleteUser(userId, token);
        UserDeleteResponse userDeleteResponse = new UserDeleteResponse();
        userDeleteResponse.setId(deletedUserId);
        userDeleteResponse.setStatus("USER SUCCESSFULLY DELETED");
        return new ResponseEntity<>(userDeleteResponse, HttpStatus.OK);
    }
}

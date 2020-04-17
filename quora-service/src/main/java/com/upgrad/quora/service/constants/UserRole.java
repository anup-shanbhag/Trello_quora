/**
 * This JAVA class defines enumerated constants for different available
 * user roles
 * @author  Anup Shanbhag (shanbhaganup@gmail.com)
 * @version 1.0
 * @since   2020-04-16
 */

package com.upgrad.quora.service.constants;

public enum UserRole {
    ADMIN ("ADMIN"), REGULAR ("NONADMIN");
    private String textRole;
    UserRole(String textRole){
        this.textRole = textRole;
    }
    public String getRole(){
        return this.textRole;
    }
}

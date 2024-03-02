package org.example.infrastructure.filter;

import java.util.ArrayList;
import java.util.List;

public class PermissionComponent {

    //private

    public void checkPermission() {
        //I want to check permission here

    }

    private List<String> permissionList = new ArrayList<>();

    private List<String> userList = new ArrayList<>();
    public List<String> getPermissionList() {
        permissionList.add("READ");
        permissionList.add("WRITE");
        permissionList.add("REVIEW");
        permissionList.add("DELETE");
        permissionList.add("UPDATE");
        permissionList.add("DOWNLOAD");
        return permissionList;
    }

    public List<String> getUserList() {
        userList.add("admin");
        userList.add("user");
        userList.add("guest");
        return userList;
    }

    //set permission WRITE for user admin
    //set permission READ for user user
    //set permission DOWNLOAD for user guest

    public void process() {

    }


}

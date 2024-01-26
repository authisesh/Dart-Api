package com.si.coredata.reportcontroller.services;

import com.si.coredata.reportcontroller.entity.UserTable;
import com.si.coredata.reportcontroller.model.LoginCred;
import com.si.coredata.reportcontroller.model.Users;

import java.util.List;

public interface UserService {

    public UserTable loadUserByName(LoginCred user);

    public List<UserTable> loadAllUsers();

    public UserTable updateUser(Users user);

    public String deleteUser(int id);

    public UserTable addUser(UserTable userTable);
}

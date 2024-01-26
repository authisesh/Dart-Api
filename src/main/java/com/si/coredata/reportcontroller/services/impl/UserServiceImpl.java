package com.si.coredata.reportcontroller.services.impl;

import com.si.coredata.reportcontroller.entity.UserTable;
import com.si.coredata.reportcontroller.model.LoginCred;
import com.si.coredata.reportcontroller.model.Users;
import com.si.coredata.reportcontroller.repository.UserRepository;
import com.si.coredata.reportcontroller.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;


    @Override
    public UserTable loadUserByName(LoginCred user) {
        System.out.println(">>>>>>" + user.getUserName());
        System.out.println(">>>>>>" + user.getPassword());

        return userRepository.findByUserName(user.getUserName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserTable> loadAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserTable updateUser(Users user) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        UserTable userTable = new UserTable(user.getID(), user.getUserName(), user.getPassword(), timestamp, timestamp, user.getRoleID(), user.getTables());
         userRepository.save(userTable);
        return userRepository.save(userTable);
    }

    @Override
    public String deleteUser(int id) {
        userRepository.deleteById(id);
        return null;
    }

    @Override
    public UserTable addUser(UserTable userTable) {

        return userRepository.save(userTable);
    }
}

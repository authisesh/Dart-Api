package com.si.coredata.reportcontroller.controllers;

import com.si.coredata.reportcontroller.entity.UserTable;
import com.si.coredata.reportcontroller.model.Users;
import com.si.coredata.reportcontroller.services.UserService;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("v1/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/loadUsers")
    public List<ResponseEntity<Object>> loadALlUsers() {


        List<UserTable> userTableList = userService.loadAllUsers();

        List<ResponseEntity<Object>> responseEntityList = new ArrayList<>();
        for (UserTable userTable : userTableList) {

            ResponseEntity<Object> responseEntity = new ResponseEntity<>(userTable, HttpStatus.OK);
            responseEntityList.add(responseEntity);
        }


        return responseEntityList;


    }

        @PutMapping("/editUser")
        public  UserTable editUser( @RequestBody Users user){
          return userService.updateUser(user);
        }

    @DeleteMapping("/deleteUser/{userId}")
    public  String deleteUser( @PathVariable String userId){

        return userService.deleteUser(Integer.parseInt(userId));
    }

    @PostMapping("/addUser")
    public UserTable addUser(@RequestBody Users user){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        UserTable userTable = new UserTable(0,user.getUserName(),user.getPassword(),timestamp,timestamp,user.getRoleID(),user.getTables());

        return userService.addUser(userTable);
    }



}

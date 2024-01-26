package com.si.coredata.reportcontroller.controllers;

import com.si.coredata.reportcontroller.entity.UserTable;
import com.si.coredata.reportcontroller.model.LoginCred;
import com.si.coredata.reportcontroller.model.Users;
import com.si.coredata.reportcontroller.services.UserService;
import com.si.coredata.reportcontroller.utils.CoreDataProcessor;
import jakarta.validation.Valid;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("v1/user")
@CrossOrigin(origins = "*")
public class LoginController {

    static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());


    @Autowired
    UserService userService;

    @Value("${t24.api.tables}")
    String t24TablesApi;


    @PostMapping("/login")
    public ResponseEntity<Object> getLoginStatus(@RequestBody LoginCred users) {

    UserTable userTable = userService.loadUserByName(users);
        Optional<UserTable> userTableOptional = Optional.ofNullable(userTable);
        if(userTableOptional.isPresent()){
            if (users.getUserName().equalsIgnoreCase(userTable.getUserName())
                    && users.getPassword().equals(userTable.getPassword())) {
                return new ResponseEntity<Object>(userTable, HttpStatus.OK);
            } else {
                return new ResponseEntity<Object>("You have entered a wrong password", HttpStatus.UNAUTHORIZED);
            }
        }
        return new ResponseEntity<Object>("User Not Found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/loadTables")
    public List<ResponseEntity<Object>> getTableNames() {
        RestTemplate restTemplate = new RestTemplate();
        List<ResponseEntity<Object>> responseEntityList = new ArrayList<>();
        try {


            Map<?, ?> intradayUpdatedRecords = restTemplate.getForObject(t24TablesApi, Map.class);
            ArrayList<Map> body = (ArrayList<Map>) intradayUpdatedRecords.get("body");
            for (Map<String, String> tableData : body) {
                ResponseEntity responseEntity = new ResponseEntity<>(tableData, HttpStatus.OK);
                responseEntityList.add(responseEntity);
                // tableList.add(tableData.get("tableName"));
            }
        } catch (Exception e) {
            LOGGER.severe(" Error while loding tables " + stackTrace(e));
            ResponseEntity responseEntity = new ResponseEntity<>("T24 Server Might be Down", HttpStatus.INTERNAL_SERVER_ERROR);
            responseEntityList.add(responseEntity);
        }

        return responseEntityList;


    }


    public String stackTrace(Exception e) {

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        return exceptionAsString;
    }

}

package com.si.coredata.reportcontroller.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    private int ID;

    @NotNull(message = "User name is Mandatory")
    private String userName;

    @NotNull(message = "Password name is Mandatory")
    private String password;

    private String tables;


    private int roleID;



}

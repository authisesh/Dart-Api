package com.si.coredata.reportcontroller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "t24_users")
@AllArgsConstructor
@NoArgsConstructor
public class UserTable {

    @Id
    @GeneratedValue(generator ="userSeq",strategy = GenerationType.AUTO)
    @SequenceGenerator(name="userSeq",sequenceName = "user_sequencegen",initialValue = 101,allocationSize = 1)
    @Column(name = "user_id")
    private int ID;


    @Column(name = "user_name")
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "updated_date")
    private Date updatedDate;

    @Column(name = "role_id")
    private int roleID;

    @Column(name = "tables")
    private String tables;


}

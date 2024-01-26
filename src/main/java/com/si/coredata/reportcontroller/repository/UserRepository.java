package com.si.coredata.reportcontroller.repository;

import com.si.coredata.reportcontroller.entity.UserTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

@Component
public interface UserRepository extends JpaRepository<UserTable,Integer> {


    UserTable findByUserName(String userName);
}

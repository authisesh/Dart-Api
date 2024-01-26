package com.si.coredata.reportcontroller.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DelimiterConfig {

    @Value("${idonly.delimiters}")
    private String delimiterList;

    public String getDelimiters() {
        return delimiterList;
    }
}

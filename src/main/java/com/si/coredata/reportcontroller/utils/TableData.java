package com.si.coredata.reportcontroller.utils;

public class TableData {
    private String tableName;
    private int dataCount;
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public int getDataCount() {
		return dataCount;
	}
	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}
	public TableData(String tableName, int dataCount) {
		super();
		this.tableName = tableName;
		this.dataCount = dataCount;
	}

    // Getters and setters

    // Constructors
}

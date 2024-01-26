package com.si.coredata.reportcontroller.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.si.coredata.reportcontroller.utils.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@CrossOrigin(origins = "*")
public class  DashboardController {

	@Autowired
	private JdbcTemplate jdbcTemplate;


	@RequestMapping(value = "/tabledata", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public TableDataResponse getTableData() {
		List<TableData> tableDataList = fetchTableData();

		return new TableDataResponse(tableDataList);
	}

	public static class TableDataResponse {
		private List<TableData> tableDataList;

		public TableDataResponse(List<TableData> tableDataList) {
			this.tableDataList = tableDataList;
		}

		public List<TableData> getTableDataList() {
			return tableDataList;
		}

		public void setTableDataList(List<TableData> tableDataList) {
			this.tableDataList = tableDataList;
		}
	}

	@GetMapping("/top10results")
	public ResponseEntity<List<Map<String, Object>>> fetchTop10Results(@RequestParam("tableName") String tableName) {

		List<Map<String, Object>> topResults = fetchTopResults(tableName);
		return ResponseEntity.ok(topResults);
	} 

	private List<TableData> fetchTableData() {
		//String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
		String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_NAME  " +
				"NOT IN ('t24_users','t24_roles','user_sequencegen')";
		List<String> tableNames = jdbcTemplate.queryForList(query, String.class);

		List<TableData> tableDataList = new ArrayList<>();
		for (String tableName : tableNames) {
			String countQuery = "SELECT COUNT(*) AS DATA_COUNT FROM [" + tableName+"]";
			Integer dataCount = jdbcTemplate.queryForObject(countQuery, Integer.class);
			tableDataList.add(new TableData(tableName, dataCount));
//			tableDataList.add(new TableData("USER",30));
//			tableDataList.add(new TableData("CUSTOMER",100));
//			tableDataList.add(new TableData("DATA",70));
		}

		return tableDataList;
	}

	private List<Map<String, Object>> fetchTopResults(String tableName) {
		String query = "SELECT * FROM [" + tableName+"]";
		return jdbcTemplate.queryForList(query);
	}
}

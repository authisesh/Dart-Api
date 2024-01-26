package com.si.coredata.reportcontroller.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.si.coredata.reportcontroller.utils.CoreDataCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "*")
public class CoreDataReportController {

	static final Logger LOGGER = Logger.getLogger(CoreDataReportController.class.getName());

	@GetMapping("/core/si/datacollector")
	public String onFire() throws InterruptedException {
		trigger();
		return "success";
	}

	@Autowired
	CoreDataCollector coreDataCollector;

	public void trigger() throws InterruptedException {

		coreDataCollector.startScan();

	}

	@GetMapping("/load-data")
	@ResponseBody
	public String loadData(@RequestParam("table") String table) {
		// Logic to call external API and load data into H2 database for the specified table
		// Replace this with your actual
		//System.out.println("Tables : "+ table);
		String [] tables = table.split("\\,");
		List<String> tableList = Arrays.asList(tables);

		coreDataCollector.startScan(tableList,false, null);

		return "SUCCESS";
	}

	@GetMapping("/download-file")
	@ResponseBody
	public String downloadData(@RequestParam("table") String table, HttpServletResponse response) {
		// Logic to call external API and load data into H2 database for the specified table
		// Replace this with your actual
		//System.out.println("Tables : "+ table);
		String [] tables = table.split("\\,");
		List<String> tableList = Arrays.asList(tables);

		coreDataCollector.startScan(tableList,true,response);

		return "SUCCESS";
	}

}

package com.si.coredata.reportcontroller.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;

@Component
public class CoreDataCollector {

    static final Logger LOGGER = Logger.getLogger(CoreDataCollector.class.getName());

    private int tabelScanCount;

    @Value("${t24si.api.url}")
    private String siApiUrl;

    @Value("${databasetype}")
    String databsetype;

    @Autowired
    private ExcelGenerator excelGenerator;

    @Autowired
    CoreDataProcessor replicaProcessor;

    @Autowired
    private DelimiterConfig delimiterConfig;
    @Value("${jdbc.driver.class}")
    String jdbcDriver;

    @Value("${jdbc.sql.url}")
    String jdbcURL;

    public String stackTrace(Exception e) {

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        return exceptionAsString;
    }

    public void truncateData(String tableName) {
        // Initialization code here
        try {
            Class.forName(jdbcDriver).newInstance();
            LOGGER.info("init JDBC driver loaded");

        } catch (Exception err) {
            LOGGER.severe("init Error while loding driver " + stackTrace(err));
        }

        Connection databaseConnection = null;
        try {
            // Connect to the database
            databaseConnection = DriverManager.getConnection(jdbcURL);
            LOGGER.info("init Connected to the database");

            Statement statement = databaseConnection.createStatement();

            tableName = CoreDataProcessor.escapeValueForDatabase(tableName, databsetype);

            // Truncate the table
            String truncateQuery = "TRUNCATE TABLE [" + tableName + "]";
            try {
                statement.executeUpdate(truncateQuery);
                LOGGER.info("Table " + tableName + " truncated successfully.");

            } catch (SQLException e) {
                if (e.getErrorCode() == 1146) {
                    LOGGER.severe("Table " + tableName + " does not exist.");
                } else {
                    LOGGER.severe("Error while truncating table " + tableName + ": " + e.getMessage());
                }
            } finally {
                // Close resources in the finally block
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    if (databaseConnection != null) {
                        databaseConnection.close();
                    }
                } catch (Exception e) {
                    // Handle exceptions if closing resources fails
                }
            }
        } catch (SQLException err) {
            LOGGER.severe("init Error connecting to the database" + stackTrace(err));
        }

    }

    public void startScan(List<String> table, boolean download, HttpServletResponse response) {

        Map<String, Object> properties = getAllKnownProperties();
        for (int i = 0; i <= table.size() - 1; i++) {
            String tableName = table.get(i);
            if (tableName == null)
                continue;

            if (!download)
                truncateData(tableName);


            String apiParam = tableName;

            if (apiParam != null && !apiParam.isEmpty() && apiParam.contains("-ID.ONLY")) {

                LOGGER.info("** -ID.ONLY Request started **");
                RestTemplate restTemplate = new RestTemplate();
                Map<?, ?> intradayUpdatedRecords = restTemplate.getForObject(siApiUrl + "/" + apiParam, Map.class);
                apiParam = apiParam.replace("-ID.ONLY", "");
                ArrayList<?> body = (ArrayList<?>) intradayUpdatedRecords.get("body");
                for (Iterator<?> iterator = body.iterator(); iterator.hasNext(); ) {
                    LinkedHashMap<?, ?> object = (LinkedHashMap<?, ?>) iterator.next();
                    String datas = (String) object.get("data");
                    Pattern pattern = Pattern.compile(Pattern.quote(delimiterConfig.getDelimiters()));
                    String[] references = pattern.split(datas);
                    LOGGER.info("** -ID.ONLY Response length  **" + references.length);
                    for (String reference : references) {

                        if (reference.contains(".") || reference.contains("-")) {
                            String modifiedReference = reference.replaceAll("[\\.-]", "..."); // Replace dots or underscores with three dots
                            String WwithIdLike = "--WITH @ID LIKE ";
                            String pathiParam = apiParam + WwithIdLike + modifiedReference;
                            callAPI(i, tableName, pathiParam, "LIKE", reference,download,response);
                        } else {
                            String WwithIdLike = "--WITH @ID EQ ";
                            String pathiParam = apiParam + WwithIdLike + reference;
                            callAPI(i, tableName, pathiParam, "EQ", reference,download,response);
                        }

                    }
                    if (body.size() > 1) {
                        LOGGER.info("** -ID.ONLY resposnses Body size is > 1 : BREAK **");
                        break;
                    }

                }
                LOGGER.info("** -ID.ONLY Request completed **");

            } else {

                callAPI(i, tableName, apiParam, "ALL", "",download,response);
            }
        }

    }

    public void startScan() {

        Map<String, Object> properties = getAllKnownProperties();

        LOGGER.info(" total table scan count **" + properties.get("tablescan.totalcount"));

        tabelScanCount = Integer.parseInt(properties.get("tablescan.totalcount") + "");
        for (int i = 1; i <= tabelScanCount; i++) {

            String key = "tablescan." + i;
            String tableName = (String) properties.get(key);
            if (tableName == null)
                continue;


            truncateData(tableName);


            String apiParam = tableName;

            if (properties.containsKey(key)) {
                LOGGER.info("Key: " + key);
                LOGGER.info("Value: " + properties.get(key));

                String prefix = key + ".";
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String entryKey = entry.getKey();
                    if (entryKey.startsWith(prefix)) {
                        LOGGER.info("Key: " + entryKey);
                        LOGGER.info("Value: " + entry.getValue());
                        apiParam = apiParam + entry.getValue();
                    }
                }
            }

            if (apiParam != null && !apiParam.isEmpty() && apiParam.contains("-ID.ONLY")) {

                LOGGER.info("** -ID.ONLY Request started **");
                RestTemplate restTemplate = new RestTemplate();
                Map<?, ?> intradayUpdatedRecords = restTemplate.getForObject(siApiUrl + "/" + apiParam, Map.class);
                apiParam = apiParam.replace("-ID.ONLY", "");
                ArrayList<?> body = (ArrayList<?>) intradayUpdatedRecords.get("body");
                for (Iterator<?> iterator = body.iterator(); iterator.hasNext(); ) {

                    LinkedHashMap<?, ?> object = (LinkedHashMap<?, ?>) iterator.next();
                    String datas = (String) object.get("data");
                    Pattern pattern = Pattern.compile(Pattern.quote(delimiterConfig.getDelimiters()));
                    String[] references = pattern.split(datas);
                    LOGGER.info("** -ID.ONLY Response length  **" + references.length);
                    for (String reference : references) {

                        if (reference.contains(".") || reference.contains("-")) {
                            String modifiedReference = reference.replaceAll("[\\.-]", "..."); // Replace dots or underscores with three dots
                            String WwithIdLike = "--WITH @ID LIKE ";
                            String pathiParam = apiParam + WwithIdLike + modifiedReference;
                            callAPI(i, tableName, pathiParam, "LIKE", reference,false,null);
                        } else {
                            String WwithIdLike = "--WITH @ID EQ ";
                            String pathiParam = apiParam + WwithIdLike + reference;
                            callAPI(i, tableName, pathiParam, "EQ", reference,false,null);
                        }

                    }
                    if (body.size() > 1) {
                        LOGGER.info("** -ID.ONLY resposnses Body size is > 1 : BREAK **");
                        break;
                    }

                }
                LOGGER.info("** -ID.ONLY Request completed **");

            } else {

                callAPI(i, tableName, apiParam, "ALL", "",false,null);
            }
        }

    }

    private void callAPI(int i, String tableName, String apiParam, String requestType, String identifier, boolean download,
                         HttpServletResponse response) {
        LOGGER.info(i + "** invoking API for " + apiParam + " started **");

        RestTemplate restTemplate = new RestTemplate();

        String encodedUrl = siApiUrl + "/" + apiParam;
        try {
            /*
             * String encodedApiParam = URLEncoder.encode(apiParam, "UTF-8"); encodedUrl =
             * siApiUrl + "/" + encodedApiParam;
             */
            LOGGER.info("** formatted URL ******* " + encodedUrl);

            // Now use the encoded URL in your API call
        } catch (Exception e) {
            LOGGER.info("Error while formatting URL: " + encodedUrl);
        }

        Map<?, ?> intradayUpdatedRecords = restTemplate.getForObject(encodedUrl, Map.class);

        ArrayList<?> body = (ArrayList<?>) intradayUpdatedRecords.get("body");
        for (Iterator<?> iterator = body.iterator(); iterator.hasNext(); ) {
            LinkedHashMap<?, ?> object = (LinkedHashMap<?, ?>) iterator.next();
            String datas = (String) object.get("data");
            String dataArr[] = datas.split("¬");

            if (dataArr == null || dataArr.length == 0) {
                LOGGER.info("** API response data length " + dataArr.length);
                return;
            }
            tableName = tableName.replace(".", "_");

            if (requestType.equalsIgnoreCase("Like")) {
                // Count the number of matching records
                int matchingCount = 0;

                for (int datalength = 0; datalength < dataArr.length; datalength++) {
                    String row = (String) dataArr[datalength];
                    String columns[] = row.split("\\|");
                    String value = columns[0];
                    if (datalength == 0 || value.equalsIgnoreCase(identifier)) {
                        dataArr[matchingCount] = row; // Move the matching record to the next position
                        matchingCount++;
                    }
                }
                // Resize the new array if needed (remove null elements)
                // Resize the array to contain only the matching records
                String matchingDataArr[] = Arrays.copyOf(dataArr, matchingCount);
                if(!download) {
                    replicaProcessor.process(matchingDataArr, tableName);
                }else{
                    try {
                        excelGenerator.generateExcelRecord(matchingDataArr,tableName,response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                if(!download) {
                    replicaProcessor.process(dataArr, tableName);
                }else{
                    try {
                        excelGenerator.generateExcelRecord(dataArr,tableName,response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

//			LOGGER.info("** total records " +dataArr.length +"**");

            if (body.size() > 1) {
                LOGGER.info("** Body size is > 1 : BREAK **");
                break;
            }
        }

        LOGGER.info(i + "** invoking API for " + apiParam + " completed **");
    }

    @Autowired
    Environment springEnvironment;

    public Map<String, Object> getAllKnownProperties() {
        Map<String, Object> rtn = new HashMap<>();
        if (springEnvironment instanceof ConfigurableEnvironment) {
            for (PropertySource<?> propertySource : ((ConfigurableEnvironment) springEnvironment)
                    .getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    for (String key : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
                        rtn.put(key, propertySource.getProperty(key));
                    }
                }
            }
        }
        return rtn;
    }

}

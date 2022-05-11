package com.beezlabs.hiveonserver.bots;

import com.beezlabs.hiveonserver.libs.JavaBotTemplate;
import com.beezlabs.tulip.libs.models.BotExecutionModel;
import com.beezlabs.tulip.libs.models.BotIdentity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;

class Report {
    public String company_code, employee_id, employee_name, pay_element_description, amount, remarks,mail;
}

class TulipCredentials{
    public String PostgresCreds,hostName,Port,DbName,TableName,WorkSheetName,UpdateCount,FormKey,
    UserId,DmsReadApi,DmsCredentials;

}



public class BulkUploadFile extends JavaBotTemplate {
    List<Report>  ExcelSheetValues = new ArrayList<>();
    BotExecutionModel botExecutionModel = null;

    @Override
    protected void botLogic(BotExecutionModel botExecutionModel) {
        try {

            TulipCredentials BotInputs = new TulipCredentials();

            this.botExecutionModel = botExecutionModel;
            BotInputs.PostgresCreds = botExecutionModel.getProposedBotInputs().get("postgresCredentials").getValue().toString();
            BotInputs.hostName = botExecutionModel.getProposedBotInputs().get("hostName").getValue().toString();
            BotInputs.Port = botExecutionModel.getProposedBotInputs().get("port").getValue().toString();
            BotInputs.DbName = botExecutionModel.getProposedBotInputs().get("dbName").getValue().toString();
            BotInputs.TableName = botExecutionModel.getProposedBotInputs().get("tableName").getValue().toString();
            BotInputs.WorkSheetName = botExecutionModel.getProposedBotInputs().get("workSheetName").getValue().toString();
            BotInputs.UpdateCount = botExecutionModel.getProposedBotInputs().get("updateCount").getValue().toString();
            BotInputs.FormKey = botExecutionModel.getProposedBotInputs().get("formKey").getValue().toString();
            BotInputs.UserId = botExecutionModel.getProposedBotInputs().get("requestedByTulipUserName").getValue().toString();
            BotInputs.DmsReadApi  = botExecutionModel.getProposedBotInputs().get("dmsRead").getValue().toString();
            String DmsCredentials = botExecutionModel.getProposedBotInputs().get("dmsCredentials").getValue().toString();
            Map<String, String> dmsCredentialList = GetIdentityBasicAuth(DmsCredentials, botExecutionModel.getIdentityList());

            String fileSignature = botExecutionModel.getProposedBotInputs().get("fileSignature").getValue().toString();
            Map<String, String> postgresCredential = GetIdentityBasicAuth(BotInputs.PostgresCreds, botExecutionModel.getIdentityList());

            boolean approvalStatus;
            if (IsApproved(BotInputs.FormKey)) {
                addVariable("approvalEntry", "INIf");
                //String fileSignature = GetFileSignature(formKey);
                InputStream inputStream = DownloadFile(BotInputs.DmsReadApi, fileSignature, dmsCredentialList);
                GetValuesFromExcel(inputStream, BotInputs.WorkSheetName);
                addVariable("dmsStatus", "Success");
                Connection connection = ConnectDatabase(postgresCredential, BotInputs.hostName, BotInputs.Port, BotInputs.DbName);
                InsertIntoDatabaseCooperative(connection, BotInputs.TableName, Integer.parseInt(String.valueOf(BotInputs.UpdateCount)), BotInputs.UserId);
                approvalStatus = true;
            } else {
                addVariable("approvalEntry", "INelse");
                approvalStatus = false;
                String arNumber = botExecutionModel.getProposedBotInputs().get("ARNumber").getValue().toString();
                addVariable("ARNumber", arNumber);
            }
            addVariable("ApprovalStatus", approvalStatus);
            success("Bot executed successfully");
        } catch (Exception ex) {
            failure("Bot failed: " + ex.getMessage());
        }
    }


    public Map<String, String> GetIdentityBasicAuth(String username, List<BotIdentity> botIdentityList) {
        Map<String, String> value = new HashMap<>();
        for (BotIdentity botIdentity : botIdentityList
        ) {
            if (botIdentity.getIdentityType().name().equals("BASIC_AUTH")) {

                if (botIdentity.getName().equals(username)) {
                    value.put("username", botIdentity.getCredential().getBasicAuth().getUsername());
                    value.put("password", botIdentity.getCredential().getBasicAuth().getPassword());
                }

            }
        }
        return value;
    }

    public boolean IsApproved(String formKey) {
        if (botExecutionModel.getProposedBotInputs().get(formKey + "_output").getValue() != null) {
            ArrayList<Map<String, Object>> TaskList = (ArrayList<Map<String, Object>>) botExecutionModel.getProposedBotInputs().get(formKey + "_output").getValue();
            if ((boolean) TaskList.get(0).get("approved")) {
                //addVariable("taskStatus", (String) TaskList.get(0).get("approved"));
                return true;
            }
        }
        return false;
    }

    public Connection ConnectDatabase(Map<String, String> postgresCred, String hostName, String port, String dbName) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://" + hostName + ":" + port + "/" + "postgres";
            conn = DriverManager.getConnection(url,"postgres", "navani");
            conn.setAutoCommit(false);
        } catch (Exception ex) {
            throw ex;
        }
        return conn;
    }

    public static InputStream DownloadFile(String dmsRead, String fileSignatureApi, Map<String, String> dmsCreds) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        String filePathOfDMS = dmsRead + fileSignatureApi;
        HttpGet request = new HttpGet(filePathOfDMS);
        String encoding = Base64.getEncoder().encodeToString((dmsCreds.get("username") + ":" + dmsCreds.get("password")).getBytes());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() == 200) {
            InputStream stream = response.getEntity().getContent();
            return stream;
        } else {
            throw new Exception("Error in downloading Uploaded excel file. Status code: " + response.getStatusLine().getStatusCode());
        }
    }

    public void GetValuesFromExcel(InputStream fileInputStream, String workSheetName) throws Exception {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            XSSFSheet sheet = workbook.getSheet(workSheetName);
            int lastRow = sheet.getLastRowNum();
            for (int row = 1; row <= lastRow; row++) {
                try {
                    com.beezlabs.hiveonserver.bots.Report report = new com.beezlabs.hiveonserver.bots.Report();
                    Row rowValue = sheet.getRow(row);
                    DataFormatter dataFormatter = new DataFormatter();
                    report.company_code = dataFormatter.formatCellValue(rowValue.getCell(0));
                    report.employee_id = dataFormatter.formatCellValue(rowValue.getCell(1));
                    report.employee_name = dataFormatter.formatCellValue(rowValue.getCell(2));
                    report.pay_element_description = dataFormatter.formatCellValue(rowValue.getCell(3));
                    report.amount = dataFormatter.formatCellValue(rowValue.getCell(4));
                    report.remarks = dataFormatter.formatCellValue(rowValue.getCell(5));
                    report.mail = dataFormatter.formatCellValue(rowValue.getCell(6));
                    if (!(report.company_code.equals("") || report.employee_id.equals("") || report.employee_name.equals("") || report.pay_element_description.equals("") || report.amount.equals("")  || report.mail.equals(""))) {
                        ExcelSheetValues.add(report);
                    }
                }catch(Exception e){
                    System.out.println("Exception has occured");
                }

            }
        } catch (Exception ex) {
            throw new Exception("Exception occured in GetValuesFromExcel method: " + ex.getMessage());
        }
    }

    public void InsertIntoDatabaseCooperative(Connection connection, String tableName, int insertCountPerBatch, String userId) throws Exception {
        try {
            int visited = 0;
            Instant instant = Instant.now();
            Timestamp currentTime = Timestamp.from(instant);
            Statement statement = connection.createStatement();
            String prepareQuery = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(prepareQuery);
            connection.beginRequest();
            connection.setAutoCommit(false);
            int listCount = ExcelSheetValues.size();
            int batchCount = (int) Math.ceil((double) listCount / (double) insertCountPerBatch);
            for (int batchItr = 1; batchItr <= batchCount; batchItr++) {
                int listItr;
                for (listItr = visited + 0; listItr < listCount && listItr < insertCountPerBatch; listItr++) {
                    preparedStatement.setString(1, ExcelSheetValues.get(listItr).company_code);
                    preparedStatement.setString(2, ExcelSheetValues.get(listItr).employee_id);
                    preparedStatement.setString(3, ExcelSheetValues.get(listItr).employee_name);
                    preparedStatement.setString(4, ExcelSheetValues.get(listItr).pay_element_description);
                    preparedStatement.setString(5, ExcelSheetValues.get(listItr).amount);
                    preparedStatement.setString(6, ExcelSheetValues.get(listItr).remarks);
                    preparedStatement.setString(7,ExcelSheetValues.get(listItr).mail);
                    preparedStatement.setTimestamp(8, currentTime);
                    preparedStatement.setString(9, userId);
                    preparedStatement.addBatch();
                    visited++;
                }
                if (visited != 0) {
                    statement.executeUpdate("LOCK TABLE " + tableName + " IN ACCESS EXCLUSIVE MODE");
                    preparedStatement.executeBatch();
                    connection.commit();
                }
            }
        } catch (Exception ex) {
            connection.rollback();
            connection.close();
            throw new Exception("Exception occured in UpdateDb method: " + ex.getMessage());
        }
    }
}

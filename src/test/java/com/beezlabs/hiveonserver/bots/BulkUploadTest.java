package com.beezlabs.hiveonserver.bots;

import com.beezlabs.hiveonserver.libs.JavaBotTemplate;
import com.beezlabs.tulip.libs.models.*;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkUploadTest {
    @Test
    public void Test() {
        try {
            BotExecutionModel botExecutionModel = new BotExecutionModel();
            Map<String, Variable> proposedBotInputs = new HashMap<>();
            proposedBotInputs.put("dmsRead", new Variable("https://tulip.beezlabs.com/api/core/dms/read/", VariableType.STRING, Object.class));
            proposedBotInputs.put("fileSignature", new Variable("754fcdeb-daa8-3f8f-a34e-7c0553ad722c/Finance%20Payroll%20Template.xlsx", VariableType.STRING, Object.class));
            //proposedBotInputs.put("dmsCredentials", new Variable("dmsCredentialsList", VariableType.STRING, Object.class));
            proposedBotInputs.put("tableName", new Variable("cooperativereports", VariableType.STRING, Object.class));
            proposedBotInputs.put("updateCount", new Variable("900", VariableType.STRING, Object.class));
            proposedBotInputs.put("formKey", new Variable("balcoPayrollAutomation", VariableType.STRING, Object.class));
            proposedBotInputs.put("hostName", new Variable("localhost", VariableType.STRING, Object.class));
            proposedBotInputs.put("port", new Variable("5432", VariableType.STRING, Object.class));
            proposedBotInputs.put("dbName", new Variable("balco", VariableType.STRING, Object.class));
            proposedBotInputs.put("filePath", new Variable("C:\\Users\\Digital Suppliers\\Downloads\\cooperativereports.xlsx", VariableType.STRING, Object.class));
            proposedBotInputs.put("workSheetName", new Variable("Medical", VariableType.STRING, Object.class));
            proposedBotInputs.put("requestedByTulipUserName", new Variable("navani@beezlabs.com", VariableType.STRING, Object.class));
            proposedBotInputs.put("postgresCredentials", new Variable("postgresCredentialsList", VariableType.ARRAY, Object.class));
            BasicAuthModel PostgresCredentials = new BasicAuthModel("postgres", "navani");
            Credential credentialPostgres = new Credential();
            credentialPostgres.setBasicAuth(PostgresCredentials);
            BotIdentity postgresCreds = new BotIdentity("postgresCredentialsList", credentialPostgres, IdentityType.BASIC_AUTH);
            proposedBotInputs.put("dmsCredentials", new Variable("dmsCredentialsList", VariableType.ARRAY, Object.class));
            BasicAuthModel dmsCredentials = new BasicAuthModel("generic", "v5N0]rByVEOjT");
            Credential credentialDms = new Credential();
            credentialPostgres.setBasicAuth(dmsCredentials);
            BotIdentity dmsCreds = new BotIdentity("dmsCredentialsList", credentialDms, IdentityType.BASIC_AUTH);

            List<BotIdentity> identityList = new ArrayList<BotIdentity>();
            identityList.add(postgresCreds);
            identityList.add(dmsCreds);
            botExecutionModel.setProposedBotInputs(proposedBotInputs);
            botExecutionModel.setIdentityList(identityList);

            JavaBotTemplate bulkUpload = new BulkUploadFile();
            bulkUpload.replyCallback(this::botReplyHandler);
            bulkUpload.runBot(botExecutionModel);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }
    public void botReplyHandler(BotReplyModel botReplyModel) {
        Assert.assertEquals("Bot executed successfully", botReplyModel.getBotMessage());
    }
}

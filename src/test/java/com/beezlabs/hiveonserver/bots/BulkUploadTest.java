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
            proposedBotInputs.put("fileSignature", new Variable("bb87032c-5c74-31e8-a808-7d108897d62e/cooperativereports.xlsx", VariableType.STRING, Object.class));
            proposedBotInputs.put("dmsCredentials", new Variable("dmsCredentialsList", VariableType.STRING, Object.class));
            proposedBotInputs.put("tableName", new Variable("public.cooperativedata", VariableType.STRING, Object.class));
            proposedBotInputs.put("updateCount", new Variable("900", VariableType.STRING, Object.class));
            proposedBotInputs.put("formKey", new Variable("balcoPayrollAutomation", VariableType.STRING, Object.class));
            proposedBotInputs.put("hostName", new Variable("localhost", VariableType.STRING, Object.class));
            proposedBotInputs.put("port", new Variable("5432", VariableType.STRING, Object.class));
            proposedBotInputs.put("dbName", new Variable("balco", VariableType.STRING, Object.class));
            proposedBotInputs.put("filePath", new Variable("C:\\Users\\Digital Suppliers\\Downloads\\cooperativereports.xlsx", VariableType.STRING, Object.class));
            proposedBotInputs.put("workSheetName", new Variable("Coopertive", VariableType.STRING, Object.class));
            proposedBotInputs.put("requestedByTulipUserName", new Variable("navani@beezlabs.com", VariableType.STRING, Object.class));
            proposedBotInputs.put("postgresCredentials", new Variable("postgresCredentialsList", VariableType.ARRAY, Object.class));
            BasicAuthModel PostgresCredentials = new BasicAuthModel("postgres", "navani");
            PostgresCredentials.setUsername("postgres");
            PostgresCredentials.setPassword("navani");

            Credential credentialPostgres = new Credential();
            credentialPostgres.setBasicAuth(PostgresCredentials);
            BotIdentity postgresCreds = new BotIdentity("postgresCredentialsList", credentialPostgres, IdentityType.BASIC_AUTH);
            proposedBotInputs.put("dmsCredentials", new Variable("dmsCredentialsList", VariableType.ARRAY, Object.class));

            BasicAuthModel dmsCredentials = new BasicAuthModel("generic", "v5N0]rByVEOjT");
//            dmsCredentials.setUsername("generic");
//            dmsCredentials.setPassword("v5N0]rByVEOjT");
            Credential credentialDms = new Credential();
            credentialDms.setBasicAuth(dmsCredentials);
            BotIdentity dmsCreds = new BotIdentity("dmsCredentialsList", credentialDms, IdentityType.BASIC_AUTH);

            List<BotIdentity> identityList = new ArrayList<BotIdentity>();
            identityList.add(postgresCreds);
            identityList.add(dmsCreds);

            proposedBotInputs.put("balcoPayrollAutomation_output",new Variable("approved",VariableType.STRING,Object.class));

            List<Map<String,Boolean>> PayrollAutimation = new ArrayList<>();
            Map<String,Boolean> Data = new HashMap<>();
            Data.put("approved",true);
            PayrollAutimation.add(Data);
            proposedBotInputs.put("balcoPayrollAutomation_output",new Variable(PayrollAutimation,VariableType.ARRAY,Object.class));

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
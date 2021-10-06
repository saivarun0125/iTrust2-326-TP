package edu.ncsu.csc.iTrust2.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.ncsu.csc.iTrust2.common.TestUtils;
import edu.ncsu.csc.iTrust2.controllers.api.comm.LogEntryRequestBody;
import edu.ncsu.csc.iTrust2.controllers.api.comm.LogEntryTableRow;
import edu.ncsu.csc.iTrust2.models.enums.TransactionType;
import edu.ncsu.csc.iTrust2.models.security.LogEntry;
import edu.ncsu.csc.iTrust2.services.security.LogEntryService;

/**
 * Class for testing logs API.
 *
 * @author Bruno Volpato
 *
 */
@RunWith ( SpringRunner.class )
@SpringBootTest
@AutoConfigureMockMvc
public class APILogEntryTest {
    private MockMvc               mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private LogEntryService service;

    /**
     * Sets up test
     */
    @Before
    public void setup () {
        mvc = MockMvcBuilders.webAppContextSetup( context ).build();
        service.deleteAll();
    }

    /**
     * Tests basic log functionality.
     *
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = { "USER", "ADMIN" } )
    public void testLogQueryAPI () throws UnsupportedEncodingException, Exception {

        LogEntry logEntry = new LogEntry();
        logEntry.setLogCode(TransactionType.LOGIN_SUCCESS);
        logEntry.setPrimaryUser("admin");
        logEntry.setMessage("Logged In");
        logEntry.setTime(ZonedDateTime.now());

        service.save(logEntry);

        assertNotNull(logEntry.getId());

        LogEntryRequestBody body = new LogEntryRequestBody();
        body.setStartDate("2020-01-01T00:00:00Z");
        body.setEndDate("2099-12-31T23:59:59Z");
        body.setPageLength(10);
        body.setPage(1);

        // Fetch logs
        final String content1 = mvc
                .perform( post( "/api/v1/logentries/range" ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( body ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        // Parse response as Drug object
        final Gson gson = new GsonBuilder().create();
        final List<LogEntryTableRow> entries = gson.fromJson( content1, new TypeToken<ArrayList<LogEntryTableRow>>(){}.getType());
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals("Successful login", entries.get(0).getTransactionType());

    }

}

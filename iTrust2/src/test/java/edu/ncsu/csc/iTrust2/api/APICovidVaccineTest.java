package edu.ncsu.csc.iTrust2.api;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import javax.transaction.Transactional;

import org.hamcrest.Matchers;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.ncsu.csc.iTrust2.common.TestUtils;
import edu.ncsu.csc.iTrust2.forms.CovidVaccineForm;
import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.DoseInterval;
import edu.ncsu.csc.iTrust2.services.CovidVaccineService;

/**
 * Class for testing CovidVaccine API.
 *
 * @author Connor
 *
 */
@RunWith ( SpringRunner.class )
@SpringBootTest
@AutoConfigureMockMvc
public class APICovidVaccineTest {
    private MockMvc               mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CovidVaccineService   service;

    /**
     * Sets up test
     */
    @Before
    public void setup () {
        mvc = MockMvcBuilders.webAppContextSetup( context ).build();
        service.deleteAll();
    }

    /**
     * Tests basic CovidVaccine API functionality.
     *
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = { "USER", "ADMIN" } )
    public void testCovidVaccineAPI () throws UnsupportedEncodingException, Exception {
        // Create CovidVaccines for testing
        final CovidVaccineForm form1 = new CovidVaccineForm();
        form1.setCode( "0000-0000-00" );
        form1.setName( "TEST1" );
        form1.setDescription( "DESC1" );
        form1.setAgeRange( new ArrayList<Integer>() );
        form1.getAgeRange().add( 0, 15 );
        form1.getAgeRange().add( 1, 99 );
        form1.setDoseInterval( new DoseInterval( ChronoUnit.WEEKS, 2 ) );
        form1.setNumDoses( (short) 2 );

        final CovidVaccineForm form2 = new CovidVaccineForm();
        form2.setCode( "0000-0000-01" );
        form2.setName( "TEST2" );
        form2.setDescription( "Desc2" );
        form2.setAgeRange( new ArrayList<Integer>() );
        form2.getAgeRange().add( 0, 15 );
        form2.getAgeRange().add( 1, 99 );
        form2.setDoseInterval( new DoseInterval( ChronoUnit.WEEKS, 2 ) );
        form2.setNumDoses( (short) 2 );

        // Add CovidVaccine1 to system
        final String content1 = mvc
                .perform( post( "/api/v1/CovidVaccines" ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( form1 ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        // Parse response as CovidVaccine object
        final Gson gson = new GsonBuilder().create();
        final CovidVaccine covidVaccine1 = gson.fromJson( content1, CovidVaccine.class );
        assertEquals( form1.getCode(), covidVaccine1.getCode() );
        assertEquals( form1.getName(), covidVaccine1.getName() );
        assertEquals( form1.getDescription(), covidVaccine1.getDescription() );

        // Attempt to add same CovidVaccine twice
        mvc.perform( post( "/api/v1/CovidVaccines" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( form1 ) ) ).andExpect( status().isConflict() );

        // Attempt to add bad json data
        mvc.perform( post( "/api/v1/CovidVaccines" ).contentType( MediaType.APPLICATION_JSON ).content( "abcdabcda" ) )
                .andExpect( status().isBadRequest() );

        // Add CovidVaccine2 to system
        final String content2 = mvc
                .perform( post( "/api/v1/CovidVaccines" ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( form2 ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();
        final CovidVaccine covidVaccine2 = gson.fromJson( content2, CovidVaccine.class );
        assertEquals( form2.getCode(), covidVaccine2.getCode() );
        assertEquals( form2.getName(), covidVaccine2.getName() );
        assertEquals( form2.getDescription(), covidVaccine2.getDescription() );

        // Verify CovidVaccines have been added
        mvc.perform( get( "/api/v1/CovidVaccines" ) ).andExpect( status().isOk() )
                .andExpect( content().string( Matchers.containsString( form1.getCode() ) ) )
                .andExpect( content().string( Matchers.containsString( form2.getCode() ) ) );

        // Edit first CovidVaccine's description
        covidVaccine1.setDescription( "This is a better description" );
        final String editContent = mvc
                .perform( put( "/api/v1/CovidVaccines" ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( covidVaccine1 ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();
        final CovidVaccine editedCovidVaccine = gson.fromJson( editContent, CovidVaccine.class );
        assertEquals( covidVaccine1.getId(), editedCovidVaccine.getId() );
        assertEquals( covidVaccine1.getCode(), editedCovidVaccine.getCode() );
        assertEquals( covidVaccine1.getName(), editedCovidVaccine.getName() );
        assertEquals( "This is a better description", editedCovidVaccine.getDescription() );

        // Attempt invalid edit
        covidVaccine2.setCode( "0000-0000-00" );
        mvc.perform( put( "/api/v1/CovidVaccines" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( covidVaccine2 ) ) ).andExpect( status().isConflict() );

        // Follow up with valid edit
        covidVaccine2.setCode( "0000-0000-03" );
        mvc.perform( put( "/api/v1/CovidVaccines" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( covidVaccine2 ) ) ).andExpect( status().isOk() );

        // delete a vaccine from the system
        mvc.perform( delete( "/api/v1/CovidVaccines/" + covidVaccine1.getId() )
                .contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( covidVaccine1 ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        assertEquals( 1, service.count() );

        // try deleting a vaccine that's no longer in the system
        mvc.perform( delete( "/api/v1/CovidVaccines/" + covidVaccine1.getId() )
                .contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( covidVaccine1 ) ) )
                .andExpect( status().isNotFound() ).andReturn().getResponse().getContentAsString();
    }

}

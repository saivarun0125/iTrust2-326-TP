package edu.ncsu.csc.iTrust2.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Assert;
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
import edu.ncsu.csc.iTrust2.forms.UserForm;
import edu.ncsu.csc.iTrust2.forms.VaccineAppointmentRequestForm;
import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.DoseInterval;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.Personnel;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineAppointmentRequest;
import edu.ncsu.csc.iTrust2.models.enums.AppointmentType;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.models.enums.Status;
import edu.ncsu.csc.iTrust2.services.CovidVaccineService;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.services.VaccineAppointmentRequestService;

/**
 * Test for the API functionality for interacting with vaccine appointment
 * requests
 *
 * @author Kai Presler-Marshall
 * @author Matt Dzwonczyk
 *
 * @author Weston Greene
 */
@RunWith ( SpringRunner.class )
@SpringBootTest
@AutoConfigureMockMvc
public class APIVaccineAppointmentRequestTest {

    private MockMvc                          mvc;

    @Autowired
    private WebApplicationContext            context;

    @Autowired
    private VaccineAppointmentRequestService arService;

    @Autowired
    private UserService                      service;

    @Autowired
    private CovidVaccineService              covidVaccineService;

    /**
     * Sets up tests
     */
    @Before
    public void setup () {
        mvc = MockMvcBuilders.webAppContextSetup( context ).build();
        service.deleteAll();
        arService.deleteAll();

        final User user = new Patient( new UserForm( "patient", "123456", Role.ROLE_PATIENT, 1 ) );
        final Patient patient = (Patient) user;
        patient.setDateOfBirth( LocalDate.now().minusYears( 20 ) );
        final User hcp = new Personnel( new UserForm( "hcp", "123456", Role.ROLE_HCP, 1 ) );

        service.saveAll( List.of( patient, hcp ) );

        final CovidVaccine covidVaccine = new CovidVaccine();
        final ArrayList<Integer> vaxAgeRange = new ArrayList<Integer>();
        vaxAgeRange.add( 0, 0 );
        vaxAgeRange.add( 1, 100 );
        covidVaccine.setAgeRange( vaxAgeRange );
        covidVaccine.setCode( "1111-1111-11" );
        covidVaccine.setDescription( "desc" );
        covidVaccine.setDoseInterval( new DoseInterval( ChronoUnit.CENTURIES, 100 ) );
        covidVaccine.setName( "test vaccine" );
        covidVaccine.setNumDoses( (short) 2 );
        covidVaccineService.save( covidVaccine );
    }

    /**
     * Tests that getting a vaccine appointment that doesn't exist returns the
     * proper status
     *
     * @throws Exception
     */
    @Test
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    @Transactional
    public void testGetNonExistentVaccineAppointment () throws Exception {
        mvc.perform( get( "/api/v1/vaccineappointmentrequests/-1" ) ).andExpect( status().isNotFound() );
    }

    /**
     * Tests that deleting a vaccine appointment that doesn't exist returns the
     * proper status.
     */
    @Test
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    @Transactional
    public void testDeleteNonExistentVaccineAppointment () throws Exception {
        mvc.perform( delete( "/api/v1/vaccineappointmentrequests/-1" ) ).andExpect( status().isNotFound() );
    }

    /**
     * Tests creating a vaccine appointment request with bad data. Should return
     * a bad request.
     *
     * @throws Exception
     */
    @Test
    @WithMockUser ( username = "patient", roles = { "PATIENT" } )
    @Transactional
    public void testCreateBadVaccineAppointmentRequest () throws Exception {

        final VaccineAppointmentRequestForm vaccineAppointmentForm = new VaccineAppointmentRequestForm();
        vaccineAppointmentForm.setDate( "0" );
        vaccineAppointmentForm.setType( AppointmentType.GENERAL_CHECKUP.toString() );
        vaccineAppointmentForm.setStatus( Status.PENDING.toString() );
        vaccineAppointmentForm.setHcp( "hcp" );
        vaccineAppointmentForm.setPatient( "patient" );
        vaccineAppointmentForm.setComments( "Test appointment please ignore" );

        mvc.perform( post( "/api/v1/vaccineappointmentrequests" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( vaccineAppointmentForm ) ) ).andExpect( status().isBadRequest() );
    }

    /**
     * Tests VaccineAppointmentRequest API
     *
     * @throws Exception
     */
    @Test
    @WithMockUser ( username = "patient", roles = { "PATIENT" } )
    @Transactional
    public void testVaccineAppointmentRequestAPI () throws Exception {

        final CovidVaccine vaccine = covidVaccineService.findByCode( "1111-1111-11" );

        final User patient = service.findByName( "patient" );

        final VaccineAppointmentRequestForm vaccineAppointmentForm = new VaccineAppointmentRequestForm();
        vaccineAppointmentForm.setDate( "2030-11-19T04:50:00.000-05:00" ); // 2030-11-19
        // 4:50 AM
        // EST
        vaccineAppointmentForm.setType( AppointmentType.GENERAL_CHECKUP.toString() );
        vaccineAppointmentForm.setStatus( Status.PENDING.toString() );
        vaccineAppointmentForm.setHcp( "hcp" );
        vaccineAppointmentForm.setPatient( "patient" );
        vaccineAppointmentForm.setComments( "Test appointment please ignore" );
        vaccineAppointmentForm.setVaccine( vaccine.getCode() );

        System.out.println( TestUtils.asJsonString( vaccineAppointmentForm ) );

        /* Create the request */
        mvc.perform( post( "/api/v1/vaccineappointmentrequests" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( vaccineAppointmentForm ) ) );

        mvc.perform( get( "/api/v1/vaccineappointmentrequest" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        assertEquals( 1, arService.count() );

        List<VaccineAppointmentRequest> forPatient = arService.findAll();
        Assert.assertEquals( 1, forPatient.size() );

        /*
         * We need the ID of the appointment request that actually got _saved_
         * when calling the API above. This will get it
         */
        final Long id = arService.findByPatient( patient ).get( 0 ).getId();

        mvc.perform( get( "/api/v1/vaccineappointmentrequests/" + id ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        vaccineAppointmentForm.setDate( "2030-11-19T03:30:00.000-05:00" ); // 2030-11-19
        // 3:30 AM

        mvc.perform( put( "/api/v1/vaccineappointmentrequests/" + id ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( vaccineAppointmentForm ) ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        forPatient = arService.findAll();
        Assert.assertEquals( 1, forPatient.size() );
        Assert.assertEquals( "2030-11-19T03:30-05:00", forPatient.get( 0 ).getDate().toString() );

        // Updating a nonexistent ID should not work
        mvc.perform( put( "/api/v1/vaccineappointmentrequests/-1" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( vaccineAppointmentForm ) ) ).andExpect( status().isNotFound() );

        mvc.perform( delete( "/api/v1/vaccineappointmentrequests/" + id ) ).andExpect( status().isOk() );

    }

}

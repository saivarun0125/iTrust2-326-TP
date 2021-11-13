package edu.ncsu.csc.iTrust2.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import edu.ncsu.csc.iTrust2.forms.VaccineOfficeVisitForm;
import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.DoseInterval;
import edu.ncsu.csc.iTrust2.models.Hospital;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.Personnel;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;
import edu.ncsu.csc.iTrust2.models.enums.AppointmentType;
import edu.ncsu.csc.iTrust2.models.enums.BloodType;
import edu.ncsu.csc.iTrust2.models.enums.Ethnicity;
import edu.ncsu.csc.iTrust2.models.enums.Gender;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.models.enums.State;
import edu.ncsu.csc.iTrust2.models.enums.Status;
import edu.ncsu.csc.iTrust2.services.CovidVaccineService;
import edu.ncsu.csc.iTrust2.services.HospitalService;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.services.VaccineAppointmentRequestService;
import edu.ncsu.csc.iTrust2.services.VaccineOfficeVisitService;

/**
 * Test for the API functionality for interacting with office visits
 *
 * @author Kai Presler-Marshall
 *
 */
@RunWith ( SpringRunner.class )
@SpringBootTest
@AutoConfigureMockMvc
public class APIVaccineOfficeVisitTest {

    private MockMvc                          mvc;

    @Autowired
    private WebApplicationContext            context;

    @Autowired
    private VaccineOfficeVisitService        vaccineOfficeVisitService;

    @Autowired
    private UserService                      userService;

    @Autowired
    private VaccineAppointmentRequestService appointmentRequestService;

    @Autowired
    private HospitalService                  hospitalService;

    @Autowired
    private CovidVaccineService              covidVaccineService;

    /**
     * Sets up test
     */
    @Before
    public void setup () {
        mvc = MockMvcBuilders.webAppContextSetup( context ).build();

        vaccineOfficeVisitService.deleteAll();

        appointmentRequestService.deleteAll();

        covidVaccineService.deleteAll();

        final User patient = new Patient( new UserForm( "patient", "123456", Role.ROLE_PATIENT, 1 ) );

        final User hcp = new Personnel( new UserForm( "hcp", "123456", Role.ROLE_HCP, 1 ) );

        final Patient antti = buildPatient( "Antti" );

        final Patient patient1 = buildPatient( "patient1" );

        final Patient patient2 = buildPatient( "patient2" );

        final Patient patient3 = buildPatient( "patient3" );

        final Patient patient4 = buildPatient( "patient4" );

        final Patient patient5 = buildPatient( "patient5" );

        final Patient babyPatient = buildBabyPatient( "babyPatient" );

        userService.saveAll(
                List.of( patient, hcp, antti, patient1, patient2, patient3, patient4, patient5, babyPatient ) );

        final Hospital hosp = new Hospital();
        hosp.setAddress( "123 Raleigh Road" );
        hosp.setState( State.NC );
        hosp.setZip( "27514" );
        hosp.setName( "iTrust Test Hospital 2" );

        hospitalService.save( hosp );

        final CovidVaccine pfizer = new CovidVaccine( "1111-1111-11", "desc", "pfizer", (short) 2,
                new DoseInterval( ChronoUnit.WEEKS, 4 ), 12, 80 );
        final CovidVaccine moderna = new CovidVaccine( "2222-2222-22", "desc", "moderna", (short) 2,
                new DoseInterval( ChronoUnit.WEEKS, 4 ), 15, 70 );
        final CovidVaccine jAndJ = new CovidVaccine( "3333-3333-33", "poison", "johnson & johnson", (short) 1, null, 12,
                80 );

        covidVaccineService.saveAll( List.of( pfizer, moderna, jAndJ ) );
    }

    private Patient buildPatient ( String name ) {
        final Patient antti = new Patient( new UserForm( name, "123456", Role.ROLE_PATIENT, 1 ) );

        antti.setAddress1( "1 Test Street" );
        antti.setAddress2( "Some Location" );
        antti.setBloodType( BloodType.APos );
        antti.setCity( "Viipuri" );
        final LocalDate date = LocalDate.of( 1977, 6, 15 );
        antti.setDateOfBirth( date );
        antti.setEmail( "antti@itrust.fi" );
        antti.setEthnicity( Ethnicity.Caucasian );
        antti.setFirstName( name );
        antti.setGender( Gender.Male );
        antti.setLastName( "Walhelm" );
        antti.setPhone( "123-456-7890" );
        antti.setState( State.NC );
        antti.setZip( "27514" );

        return antti;
    }

    private Patient buildBabyPatient ( String name ) {
        final Patient antti = new Patient( new UserForm( name, "123456", Role.ROLE_PATIENT, 1 ) );

        antti.setAddress1( "1 Test Street" );
        antti.setAddress2( "Some Location" );
        antti.setBloodType( BloodType.APos );
        antti.setCity( "Viipuri" );
        final LocalDate date = LocalDate.of( 2029, 6, 15 );
        antti.setDateOfBirth( date );
        antti.setEmail( "antti@itrust.fi" );
        antti.setEthnicity( Ethnicity.Caucasian );
        antti.setFirstName( name );
        antti.setGender( Gender.Male );
        antti.setLastName( "Walhelm" );
        antti.setPhone( "123-456-7890" );
        antti.setState( State.NC );
        antti.setZip( "27514" );

        return antti;
    }

    /**
     * Tests getting a non existent office visit and ensures that the correct
     * status is returned.
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    public void testGetNonExistentVaccineOfficeVisit () throws Exception {
        mvc.perform( get( "/api/v1/vaccineofficevisits/-1" ) ).andExpect( status().isNotFound() );
    }

    /**
     * Tests handling of errors when creating a visit for a pre-scheduled
     * appointment.
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    public void testPreScheduledVaccineOfficeVisit () throws Exception {

        final VaccineAppointmentRequestForm appointmentForm = new VaccineAppointmentRequestForm();

        // 2030-11-19 4:50 AM EST
        appointmentForm.setDate( "2030-11-19T04:50:00.000-05:00" );

        appointmentForm.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        appointmentForm.setStatus( Status.APPROVED.toString() );
        appointmentForm.setHcp( "hcp" );
        appointmentForm.setPatient( "antti" );
        appointmentForm.setComments( "Test appointment please ignore" );
        appointmentForm.setVaccine( "1111-1111-11" );

        appointmentRequestService.save( appointmentRequestService.build( appointmentForm ) );

        final VaccineOfficeVisitForm visit = new VaccineOfficeVisitForm();
        visit.setPreScheduled( "yes" );
        visit.setDate( "2030-11-19T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "antti" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "1111-1111-11" );
        visit.setDoseNumber( 1 );
        visit.setScheduled( true );

        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isOk() );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        vaccineOfficeVisitService.deleteAll();

        Assert.assertEquals( 0, vaccineOfficeVisitService.count() );

        final Patient antti = (Patient) userService.findByName( "antti" );
        antti.setVaccinesRecieved( null );
        userService.save( antti );

        visit.setDate( "2030-12-19T04:50:00.000-05:00" );
        // setting a pre-scheduled appointment that doesn't match should not
        // work.
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isBadRequest() );

    }

    /**
     * Tests VaccineOfficeVisitAPI
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    public void testUnscheduled () throws Exception {

        Assert.assertEquals( 0, vaccineOfficeVisitService.count() );

        // test unscheduled vaccine appt
        final VaccineOfficeVisitForm visit = new VaccineOfficeVisitForm();
        visit.setPreScheduled( "no" );
        visit.setDate( "2030-11-19T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient1" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "1111-1111-11" );
        visit.setDoseNumber( 1 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isOk() );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        /* Test getForHCP and getForHCPAndPatient */
        final VaccineOfficeVisit v = vaccineOfficeVisitService.build( visit );
        List<VaccineOfficeVisit> vList = vaccineOfficeVisitService.findByHcp( v.getHcp() );
        assertEquals( vList.get( 0 ).getHcp(), v.getHcp() );
        vList = vaccineOfficeVisitService.findByHcpAndPatient( v.getHcp(), v.getPatient() );
        assertEquals( vList.get( 0 ).getHcp(), v.getHcp() );
        assertEquals( vList.get( 0 ).getPatient(), v.getPatient() );

        // check if getting vaccine office visits for HCP works
        mvc.perform( get( "/api/v1/vaccineofficevisits/HCP" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        /*
         * We need the ID of the office visit that actually got _saved_ when
         * calling the API above. This will get it
         */
        final Long id = vaccineOfficeVisitService.findByPatient( userService.findByName( "patient1" ) ).get( 0 )
                .getId();

        visit.setId( id.toString() );

        // Second post should fail with a conflict since it already exists
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isConflict() );

        mvc.perform( get( "/api/v1/vaccineofficevisits/" + id ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );
    }

    @Test
    @Transactional
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    public void testMultipleDoses () throws Exception {
        // test unscheduled vaccine appt
        final VaccineOfficeVisitForm visit = new VaccineOfficeVisitForm();
        visit.setPreScheduled( "no" );
        visit.setDate( "2030-11-19T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient2" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "1111-1111-11" );
        visit.setDoseNumber( 1 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isOk() );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        // test second dose
        visit.setPreScheduled( "no" );
        visit.setDate( "2030-12-25T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient2" );
        visit.setNotes( "Test office visit 2" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "1111-1111-11" );
        visit.setDoseNumber( 2 );
        visit.setScheduled( false );

        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isOk() );

        Assert.assertEquals( 2, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        // test third dose

        visit.setPreScheduled( "no" );
        visit.setDate( "2031-02-03T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient2" );
        visit.setNotes( "Test office visit 2" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "1111-1111-11" );
        visit.setDoseNumber( 2 );
        visit.setScheduled( false );

        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isConflict() );

        Assert.assertEquals( 2, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

    }

    @Test
    @Transactional
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    public void testSingleDose () throws Exception {
        // test unscheduled vaccine appt
        final VaccineOfficeVisitForm visit = new VaccineOfficeVisitForm();
        visit.setPreScheduled( "no" );
        visit.setDate( "2030-11-19T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient3" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "3333-3333-33" );
        visit.setDoseNumber( 1 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isOk() );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        visit.setPreScheduled( "no" );
        visit.setDate( "2030-12-25T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient3" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "3333-3333-33" );
        visit.setDoseNumber( 1 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isConflict() );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

    }

    @Test
    @Transactional
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    public void testMixAndMatch () throws Exception {
        // test unscheduled vaccine appt
        final VaccineOfficeVisitForm visit = new VaccineOfficeVisitForm();
        visit.setPreScheduled( "no" );
        visit.setDate( "2030-11-19T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient4" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "1111-1111-11" );
        visit.setDoseNumber( 1 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isOk() );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        visit.setPreScheduled( "no" );
        visit.setDate( "2030-12-25T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient4" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "2222-2222-22" );
        visit.setDoseNumber( 2 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isConflict() );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        visit.setPreScheduled( "no" );
        visit.setDate( "2030-12-25T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient4" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "3333-3333-33" );
        visit.setDoseNumber( 2 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isConflict() );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        visit.setPreScheduled( "no" );
        visit.setDate( "2030-12-25T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient4" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "1111-1111-11" );
        visit.setDoseNumber( 1 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isConflict() );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        visit.setPreScheduled( "no" );
        visit.setDate( "2030-12-25T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient4" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "1111-1111-11" );
        visit.setDoseNumber( 2 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isOk() );

        Assert.assertEquals( 2, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

    }

    @Test
    @Transactional
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    public void testInvalidAge () throws Exception {
        final VaccineOfficeVisitForm visit = new VaccineOfficeVisitForm();
        visit.setPreScheduled( "no" );
        visit.setDate( "2030-11-19T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "babyPatient" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "3333-3333-33" );
        visit.setDoseNumber( 1 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isBadRequest() );

        Assert.assertEquals( 0, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

    }

    @Test
    @Transactional
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    public void testEdit () throws Exception {
        final VaccineOfficeVisitForm visit = new VaccineOfficeVisitForm();
        visit.setPreScheduled( "no" );
        visit.setDate( "2030-11-19T04:50:00.000-05:00" );
        visit.setHcp( "hcp" );
        visit.setPatient( "patient5" );
        visit.setNotes( "Test office visit" );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT.toString() );
        visit.setHospital( "iTrust Test Hospital 2" );
        visit.setVaccine( "3333-3333-33" );
        visit.setDoseNumber( 1 );
        visit.setScheduled( false );

        /* Create the Office Visit */
        mvc.perform( post( "/api/v1/vaccineofficevisits" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isOk() );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        mvc.perform( get( "/api/v1/vaccineofficevisits" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        final String id = vaccineOfficeVisitService.findByPatient( userService.findByName( "patient5" ) ).get( 0 )
                .getId().toString();

        visit.setId( id );

        visit.setNotes( "Test office visit, changed description" );

        // do a valid put, changing description
        mvc.perform( put( "/api/v1/vaccineofficevisits/" + id ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isOk() );

        assertEquals( "Test office visit, changed description",
                vaccineOfficeVisitService.findByPatient( userService.findByName( "patient5" ) ).get( 0 ).getNotes() );

        visit.setId( null );
        // do an invalid put
        mvc.perform( put( "/api/v1/vaccineofficevisits/" + id ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( visit ) ) ).andExpect( status().isNotFound() );

    }

}

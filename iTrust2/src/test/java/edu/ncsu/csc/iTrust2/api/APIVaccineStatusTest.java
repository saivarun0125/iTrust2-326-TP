package edu.ncsu.csc.iTrust2.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.ncsu.csc.iTrust2.forms.UserForm;
import edu.ncsu.csc.iTrust2.forms.VaccineOfficeVisitForm;
import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.DoseInterval;
import edu.ncsu.csc.iTrust2.models.Hospital;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.Personnel;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.enums.BloodType;
import edu.ncsu.csc.iTrust2.models.enums.Ethnicity;
import edu.ncsu.csc.iTrust2.models.enums.Gender;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.models.enums.State;
import edu.ncsu.csc.iTrust2.services.CovidVaccineService;
import edu.ncsu.csc.iTrust2.services.HospitalService;
import edu.ncsu.csc.iTrust2.services.PatientService;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.services.VaccineAppointmentRequestService;
import edu.ncsu.csc.iTrust2.services.VaccineOfficeVisitService;

/**
 * Test for API functionality for interacting with Patients status
 *
 * @author Kai Presler-Marshall
 *
 */
@RunWith ( SpringRunner.class )
@SpringBootTest
@AutoConfigureMockMvc
public class APIVaccineStatusTest {

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

    @Autowired
    private PatientService                   patientService;

    /**
     * Sets up tests
     */
    @Before
    public void setup () {
        mvc = MockMvcBuilders.webAppContextSetup( context ).build();

        vaccineOfficeVisitService.deleteAll();

        appointmentRequestService.deleteAll();

        covidVaccineService.deleteAll();

        User hcp = new Personnel( new UserForm( "hcp", "123456", Role.ROLE_HCP, 1 ) );

        final Personnel hcpPers = (Personnel) hcp;
        hcpPers.setFirstName( "Jeff" );
        hcpPers.setLastName( "Jefferson" );

        hcp = hcpPers;

        final Patient patient1 = buildPatient( "patient1" );
        userService.save( patient1 );
        userService.save( hcp );

        final Hospital hosp = new Hospital();
        hosp.setAddress( "123 Raleigh Road" );
        hosp.setState( State.NC );
        hosp.setZip( "27514" );
        hosp.setName( "iTrust Test Hospital 2" );
        hospitalService.save( hosp );

        final CovidVaccine pfizer = new CovidVaccine( "1111-1111-11", "desc", "pfizer", (short) 2,
                new DoseInterval( ChronoUnit.WEEKS, 4 ), 12, 80, true );
        final CovidVaccine moderna = new CovidVaccine( "2222-2222-22", "desc", "moderna", (short) 2,
                new DoseInterval( ChronoUnit.WEEKS, 4 ), 15, 70, true );
        final CovidVaccine jAndJ = new CovidVaccine( "3333-3333-33", "poison", "johnson & johnson", (short) 1, null, 12,
                80, true );
        covidVaccineService.saveAll( List.of( pfizer, moderna, jAndJ ) );

        final VaccineOfficeVisitForm form1 = new VaccineOfficeVisitForm();
        final LocalDate date = LocalDate.of( 2021, 11, 15 );
        form1.setDate( "2030-11-19T04:50:00.000-05:00" );
        form1.setDoseNumber( 1 );
        form1.setHcp( hcp.getUsername() );
        form1.setHospital( hosp.getName() );
        form1.setPatient( patient1.getUsername() );
        form1.setVaccine( moderna.getCode() );

        final VaccineOfficeVisitForm form2 = new VaccineOfficeVisitForm();
        final LocalDate date2 = LocalDate.of( 2021, 11, 20 );
        form2.setDate( "2030-11-19T04:50:00.000-05:00" );
        form2.setDoseNumber( 2 );
        form2.setHcp( hcp.getUsername() );
        form2.setHospital( hosp.getName() );
        form2.setPatient( patient1.getUsername() );
        form2.setVaccine( moderna.getCode() );

        vaccineOfficeVisitService.save( vaccineOfficeVisitService.build( form1 ) );
        vaccineOfficeVisitService.save( vaccineOfficeVisitService.build( form2 ) );

    }

    private Patient buildPatient ( final String name ) {
        final Patient antti = new Patient( new UserForm( name, "123456", Role.ROLE_PATIENT, 1 ) );

        antti.setAddress1( "1 Test Street" );
        antti.setAddress2( "Some Location" );
        antti.setBloodType( BloodType.APos );
        antti.setCity( "Viipuri" );
        final LocalDate date = LocalDate.of( 1977, 6, 15 );
        antti.setDateOfBirth( date );
        antti.setEmail( "antti@itrust.fi" );
        antti.setEthnicity( Ethnicity.Caucasian );
        antti.setFirstName( "Erik" );
        antti.setGender( Gender.Male );
        antti.setLastName( "Walhelm" );
        antti.setPhone( "123-456-7890" );
        antti.setState( State.NC );
        antti.setZip( "27514" );

        return antti;
    }

    /**
     * Tests vaccination status
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @WithMockUser ( username = "patient1", roles = { "PATIENT" } )
    public void testVaccineStatus () throws Exception {

        mvc.perform( get( "/api/v1/vaccinationstatus/test" ) ).andExpect( status().isOk() );

    }
    
    @Test
    @Transactional
    @WithMockUser( username = "patient1", roles = { "PATIENT" } )
    public void testUserVaccinated() throws Exception {
    	
    	mvc.perform( post("/api/v1/vaccinationstatus" ) ).andExpect( status().isOk() );
    }

    // /**
    // * Tests vaccination status
    // *
    // * @throws Exception
    // */
    // @Test
    // @Transactional
    // @WithMockUser ( username = "Antii", roles = { "Patient" } )
    // public void testVaccinationStatus () throws Exception {
    // mvc.perform( get( "/api/v1/vaccinationstatus" ) ).andExpect(
    // status().isOk() );
    // }
}

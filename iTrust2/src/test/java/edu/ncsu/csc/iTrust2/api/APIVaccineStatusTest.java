package edu.ncsu.csc.iTrust2.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.ncsu.csc.iTrust2.forms.VaccineOfficeVisitForm;
import edu.ncsu.csc.iTrust2.models.enums.AppointmentType;
import edu.ncsu.csc.iTrust2.services.VaccineOfficeVisitService;

/**
 * Class for testing CovidVaccine API.
 *
 * @author sai
 *
 */
@RunWith ( SpringRunner.class )
@SpringBootTest
@AutoConfigureMockMvc
public class APIVaccineStatusTest {

    private MockMvc                   mvc;

    @Autowired
    private WebApplicationContext     context;

    @Autowired
    private VaccineOfficeVisitService aoService;

    /**
     * Sets up tests
     */
    @Before
    public void setup () {
        mvc = MockMvcBuilders.webAppContextSetup( context ).build();
        aoService.deleteAll();

    }

    /**
     * Tests vaccination status of a patient
     *
     * @throws Exception
     */
    @Test
    @Transactional
    public void testVaccinationStatus () throws Exception {
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

        mvc.perform( get( "/api/v1/vaccinationstatus" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        // Assert.assertEquals( "hcp" );
    }

}

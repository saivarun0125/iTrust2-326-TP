package edu.ncsu.csc.iTrust2.unit;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc.iTrust2.TestConfig;
import edu.ncsu.csc.iTrust2.forms.CovidVaccineForm;
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
import edu.ncsu.csc.iTrust2.services.VaccineAppointmentRequestService;

/**
 * Vaccine Appointment Request testing
 *
 * @author Sai Maale
 *
 */
@RunWith ( SpringRunner.class )
@EnableAutoConfiguration
@SpringBootTest ( classes = TestConfig.class )
public class VaccineAppointmentRequestTest {

    @Autowired
    private VaccineAppointmentRequestService service;

    /**
     * Test creating a vaccine from the form
     */
    @Transactional
    @Test
    public void testCreateVaccine () {
        assertEquals( service.count(), 0 );

        final VaccineAppointmentRequest request = new VaccineAppointmentRequest();

        CovidVaccine CovidVaccine = null;
        final CovidVaccineForm form = new CovidVaccineForm();
        form.setNumDoses( (short) 1 );
        form.setName( "Pfizer" );
        form.setId( (long) 25 );
        final ArrayList<Integer> ageRangeList = new ArrayList<Integer>();
        ageRangeList.add( 18 );
        ageRangeList.add( 70 );
        form.setAgeRange( ageRangeList );
        final DoseInterval doseInterval = new DoseInterval( ChronoUnit.MONTHS, 1 );
        form.setDoseInterval( doseInterval );
        CovidVaccine = new CovidVaccine( form );
        request.setVaccine( CovidVaccine );
        assertEquals( request.getVaccine(), CovidVaccine );

        final User vaccinator = new Personnel( new UserForm( "vaccinator", "123", Role.ROLE_VACCINATOR, 1 ) );
        request.setHcp( vaccinator );

        assertEquals( request.getHcp(), vaccinator );

    }

    /**
     * Invalid Vaccinator Test
     */
    @Transactional
    @Test
    public void testInvalidVaccinator () {
        final VaccineAppointmentRequest request = new VaccineAppointmentRequest();
        final User patient = new Patient( new UserForm( "patient", "111", Role.ROLE_PATIENT, 1 ) );
        request.setPatient( patient );
        final User nonVaccinator = new Personnel( new UserForm( "er", "123", Role.ROLE_ER, 1 ) );
        request.setHcp( nonVaccinator );
        request.setDate( ZonedDateTime.parse( "2030-11-19T04:50:00.000-05:00" ) );
        request.setType( AppointmentType.VACCINE_APPOINTMENT );
        request.setComments( "hi" );
        final long id = 123;
        request.setId( id );
        request.setStatus( Status.APPROVED );
        CovidVaccine CovidVaccine = null;
        final CovidVaccineForm form = new CovidVaccineForm();
        form.setNumDoses( (short) 1 );
        form.setName( "J&J" );
        form.setId( (long) 25 );
        final ArrayList<Integer> ageRangeList = new ArrayList<Integer>();
        ageRangeList.add( 18 );
        ageRangeList.add( 70 );
        form.setAgeRange( ageRangeList );
        final DoseInterval doseInterval = new DoseInterval( ChronoUnit.MONTHS, 1 );
        form.setDoseInterval( doseInterval );
        CovidVaccine = new CovidVaccine( form );
        request.setVaccine( CovidVaccine );
        request.setHcp( nonVaccinator );

        final User vaccinator = new Personnel( new UserForm( "vaccinator", "123", Role.ROLE_VACCINATOR, 1 ) );
        request.setHcp( vaccinator );
        final VaccineAppointmentRequestForm vform = new VaccineAppointmentRequestForm( request );
        assertEquals( vform.getHcp(), vaccinator.getUsername() );
    }

    /**
     * VaccineAppointmentRequestForm class test.
     */
    @Transactional
    @Test
    public void testVaccineAppointmentRequestForm () {
        final VaccineAppointmentRequest request = new VaccineAppointmentRequest();
        final User patient = new Patient( new UserForm( "patient", "111", Role.ROLE_PATIENT, 1 ) );
        request.setPatient( patient );
        final User hcp = new Personnel( new UserForm( "hcp", "123", Role.ROLE_HCP, 1 ) );
        request.setDate( ZonedDateTime.parse( "2030-11-19T04:50:00.000-05:00" ) );
        request.setType( AppointmentType.VACCINE_APPOINTMENT );
        request.setComments( "hi" );
        final long id = 123456;
        request.setId( id );
        request.setStatus( Status.APPROVED );
        CovidVaccine vaccine = null;
        final CovidVaccineForm form = new CovidVaccineForm();
        form.setNumDoses( (short) 1 );
        form.setName( "J&J" );
        form.setId( (long) 25 );
        final ArrayList<Integer> ageRangeList = new ArrayList<Integer>();
        ageRangeList.add( 18 );
        ageRangeList.add( 70 );
        form.setAgeRange( ageRangeList );
        final DoseInterval doseInterval = new DoseInterval( ChronoUnit.MONTHS, 1 );
        form.setDoseInterval( doseInterval );
        vaccine = new CovidVaccine( form );
        request.setVaccine( vaccine );
        request.setHcp( hcp );

        final VaccineAppointmentRequestForm vform = new VaccineAppointmentRequestForm( request );
        assertEquals( vform.getVaccine(), vaccine.getName() );
        assertEquals( vform.getHcp(), "hcp" );
    }
}

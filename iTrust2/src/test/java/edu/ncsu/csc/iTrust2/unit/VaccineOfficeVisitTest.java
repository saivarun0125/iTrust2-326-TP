package edu.ncsu.csc.iTrust2.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.ncsu.csc.iTrust2.TestConfig;
import edu.ncsu.csc.iTrust2.forms.UserForm;
import edu.ncsu.csc.iTrust2.forms.VaccineOfficeVisitForm;
import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.DoseInterval;
import edu.ncsu.csc.iTrust2.models.Hospital;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.Personnel;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;
import edu.ncsu.csc.iTrust2.models.enums.AppointmentType;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.services.CovidVaccineService;
import edu.ncsu.csc.iTrust2.services.HospitalService;
import edu.ncsu.csc.iTrust2.services.ICDCodeService;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.services.VaccineOfficeVisitService;

@RunWith ( SpringRunner.class )
@EnableAutoConfiguration
@SpringBootTest ( classes = TestConfig.class )
public class VaccineOfficeVisitTest {

    @Autowired
    private VaccineOfficeVisitService vaccineOfficeVisitService;

    // @Autowired
    // private BasicHealthMetricsService basicHealthMetricsService;

    @Autowired
    private CovidVaccineService       covidVaccineService;

    @Autowired
    private HospitalService           hospitalService;

    @SuppressWarnings ( "rawtypes" )
    @Autowired
    private UserService               userService;

    @Autowired
    private ICDCodeService            icdCodeService;

    // @Autowired
    // private DrugService drugService;
    //
    // @Autowired
    // private PrescriptionService prescriptionService;

    @SuppressWarnings ( "unchecked" )
    @Before
    public void setup () {
        vaccineOfficeVisitService.deleteAll();

        final User hcp = new Personnel( new UserForm( "hcp", "123456", Role.ROLE_HCP, 1 ) );

        final User alice = new Patient( new UserForm( "AliceThirteen", "123456", Role.ROLE_PATIENT, 1 ) );

        userService.saveAll( List.of( hcp, alice ) );
        covidVaccineService.deleteAll();
    }

    @Test
    @Transactional
    public void testVaccineOfficeVisit () {
        Assert.assertEquals( 0, vaccineOfficeVisitService.count() );

        final Hospital hosp = new Hospital( "Dr. Jenkins' Insane Asylum", "123 Main St", "12345", "NC" );
        hospitalService.save( hosp );

        final VaccineOfficeVisit visit = new VaccineOfficeVisit();

        final CovidVaccine vaccine = new CovidVaccine();

        final List<Integer> ageRange = new ArrayList<Integer>();
        final DoseInterval doseInterval = new DoseInterval( ChronoUnit.DAYS, 30 );

        ageRange.add( 18 );
        ageRange.add( 70 );

        vaccine.setAgeRange( ageRange );
        vaccine.setCode( "0000-0000-00" );
        vaccine.setDescription( "Pfizer" );
        vaccine.setDoseInterval( doseInterval );
        vaccine.setName( "Pfizer COVID19 Vaccine" );
        vaccine.setNumDoses( (short) 2 );

        covidVaccineService.save( vaccine );

        visit.setVaccine( vaccine );
        visit.setType( AppointmentType.VACCINE_APPOINTMENT );
        visit.setHospital( hosp );
        visit.setPatient( userService.findByName( "AliceThirteen" ) );
        visit.setHcp( userService.findByName( "hcp" ) );
        visit.setDate( ZonedDateTime.now() );
        visit.setVaccine( vaccine );
        visit.setDoseNumber( 1 );
        visit.setScheduled( true );
        vaccineOfficeVisitService.save( visit );

        Assert.assertEquals( 1, vaccineOfficeVisitService.count() );

        VaccineOfficeVisit retrieved = vaccineOfficeVisitService.findAll().get( 0 );

        Assert.assertEquals( "Pfizer COVID19 Vaccine", retrieved.getVaccine().getName() );

        vaccineOfficeVisitService.delete( visit );

        assertEquals( vaccineOfficeVisitService.findAll().size(), 0 );

        final CovidVaccine vaccine2 = new CovidVaccine();

        final List<Integer> ageRange2 = new ArrayList<Integer>();
        final DoseInterval doseInterval2 = new DoseInterval( ChronoUnit.DAYS, 30 );

        ageRange2.add( 18 );
        ageRange2.add( 70 );

        vaccine2.setAgeRange( ageRange2 );
        vaccine2.setCode( "0100-0000-00" );
        vaccine2.setDescription( "Janssen" );
        vaccine2.setDoseInterval( doseInterval2 );
        vaccine2.setName( "Johnson and Johnson COVID19 Vaccine" );
        vaccine2.setNumDoses( (short) 1 );

        covidVaccineService.save( vaccine2 );
        visit.setVaccine( vaccine2 );

        vaccineOfficeVisitService.save( visit );

        retrieved = vaccineOfficeVisitService.findAll().get( 0 );

        Assert.assertEquals( "Johnson and Johnson COVID19 Vaccine", retrieved.getVaccine().getName() );
        Assert.assertEquals( "AliceThirteen", retrieved.getPatient().getUsername() );
        Assert.assertEquals( hosp.toString(), retrieved.getHospital().toString() );
        Assert.assertEquals( retrieved.getVaccine().toString(), vaccine2.toString() );
        Assert.assertEquals( retrieved.getHcp().getUsername(), "hcp" );

        // some error testing with null fields

        try {
            vaccine2.setName( null );
            visit.setVaccine( vaccine2 );
            vaccineOfficeVisitService.save( visit );
            Assert.fail();
        }
        catch ( final Exception e ) {
            // pass
        }

        vaccine2.setName( "Johnson and Johnson COVID19 Vaccine" );
        visit.setVaccine( vaccine2 );
        vaccineOfficeVisitService.save( visit );

        try {
            visit.setDoseNumber( null );
            vaccineOfficeVisitService.save( visit );
            Assert.fail();
        }
        catch ( final Exception e ) {
            // pass
        }

        try {
            visit.setDoseNumber( -1 );
            visit.validateDoseNumber();
            Assert.fail();
        }
        catch ( final Exception e ) {
            // pass
        }

        visit.setDoseNumber( 1 );
        vaccineOfficeVisitService.save( visit );

        // try {
        // vaccine2.setAgeRange( new ArrayList<Integer>() );
        // visit.setVaccine( vaccine2 );
        // vaccineOfficeVisitService.save( visit );
        // Assert.fail();
        // }
        // catch ( final Exception e ) {
        // // pass
        // }

        try {
            vaccine2.setAgeRange( new ArrayList<Integer>() );
            visit.setVaccine( vaccine2 );
            vaccineOfficeVisitService.build( new VaccineOfficeVisitForm( visit ) );
            Assert.fail();
        }
        catch ( final Exception e ) {
            // pass
        }

        vaccine2.setAgeRange( ageRange2 );
        visit.setVaccine( vaccine2 );
        vaccineOfficeVisitService.save( visit );

        try {
            vaccine2.setDoseInterval( null );
            visit.setVaccine( vaccine2 );
            vaccineOfficeVisitService.build( new VaccineOfficeVisitForm( visit ) );
            Assert.fail();
        }

        catch ( final Exception e ) {
            // pass
        }

        vaccine2.setDoseInterval( doseInterval2 );
        visit.setVaccine( vaccine2 );
        vaccineOfficeVisitService.save( visit );

        try {
            visit.setVaccine( null );
            vaccineOfficeVisitService.save( visit );
            Assert.fail();
        }
        catch ( final Exception e ) {
            // pass
        }

        CovidVaccine vaxTest = null;
        visit.setVaccine( vaxTest );
        try {
            visit.validateVaccine();
            Assert.fail();
        }
        catch ( final Exception e ) {
            assertEquals( e.getMessage(), "Vaccine must be entered" );
            // pass
        }

        vaxTest = new CovidVaccine();
        vaxTest.setAgeRange( ageRange2 );
        vaxTest.setCode( "0200-0000-00" );
        vaxTest.setDescription( "AH" );
        vaxTest.setDoseInterval( doseInterval2 );
        vaxTest.setName( "AH COVID19 Vaccine" );
        vaxTest.setNumDoses( (short) 2 );

        final List<Integer> ageRange3 = new ArrayList<Integer>();
        final DoseInterval doseInterval3 = null;

        vaxTest.setAgeRange( ageRange3 );
        visit.setVaccine( vaxTest );
        try {
            visit.validateVaccine();
            Assert.fail();
        }
        catch ( final Exception e ) {
            assertEquals( e.getMessage(), "The age range must consist of two whole numbers." );
            // pass
        }

        vaxTest.setAgeRange( ageRange2 );
        vaxTest.setDoseInterval( doseInterval3 );
        visit.setVaccine( vaxTest );
        try {
            visit.validateVaccine();
            Assert.fail();
        }
        catch ( final Exception e ) {
            assertEquals( e.getMessage(), "There must be a proper dose interval." );
            // pass
        }

        vaxTest.setDoseInterval( doseInterval2 );
        vaxTest.setNumDoses( (short) -1 );
        visit.setVaccine( vaxTest );
        try {
            visit.validateVaccine();
            Assert.fail();
        }
        catch ( final Exception e ) {
            assertEquals( e.getMessage(), "There must be a positive number of doses." );
            // pass
        }

        vaxTest.setNumDoses( (short) 2 );
        visit.setVaccine( vaxTest );
        visit.setType( AppointmentType.GENERAL_CHECKUP );
        try {
            visit.validateVaccine();
            Assert.fail();
        }
        catch ( final Exception e ) {
            assertEquals( e.getMessage(), "Vaccines can only be entered into vacicnation appointments." );
            // pass
        }

    }

    @Test
    @Transactional
    public void testVaccineOfficeVisitForm () {

        final Hospital hosp = new Hospital( "Dr. Jenkins' Mad Eyes", "123 Main St", "12345", "NC" );
        hospitalService.save( hosp );

        final VaccineOfficeVisit visit = new VaccineOfficeVisit();

        final CovidVaccine vaccine = new CovidVaccine();

        final List<Integer> ageRange = new ArrayList<Integer>();
        final DoseInterval doseInterval = new DoseInterval( ChronoUnit.DAYS, 30 );

        ageRange.add( 20 );
        ageRange.add( 30 );

        vaccine.setAgeRange( ageRange );
        vaccine.setCode( "0000-0000-00" );
        vaccine.setDescription( "Pfizer" );
        vaccine.setDoseInterval( doseInterval );
        vaccine.setName( "Pfizer COVID19 Vaccine" );
        vaccine.setNumDoses( (short) 2 );

        covidVaccineService.save( vaccine );

        visit.setType( AppointmentType.VACCINE_APPOINTMENT );
        visit.setHospital( hosp );
        visit.setPatient( userService.findByName( "AliceThirteen" ) );
        visit.setHcp( userService.findByName( "hcp" ) );
        visit.setDate( ZonedDateTime.now() );
        visit.setVaccine( vaccine );
        visit.setDoseNumber( 1 );
        visit.setScheduled( true );
        visit.setAppointment( null );

        vaccineOfficeVisitService.save( visit );

        assertEquals( 1, vaccineOfficeVisitService.count() );

        final VaccineOfficeVisit retrieved = vaccineOfficeVisitService.findAll().get( 0 );

        assertNotNull( retrieved );

        assertEquals( "Pfizer COVID19 Vaccine", retrieved.getVaccine().getName() );

        assertNotNull( retrieved.getVaccine() );
        assertNull( retrieved.getAppointment() );

        assertEquals( 1, (int) retrieved.getDoseNumber() );

        assertTrue( retrieved.isScheduled() );

    }

}

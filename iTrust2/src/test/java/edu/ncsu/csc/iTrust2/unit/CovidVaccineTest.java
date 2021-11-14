package edu.ncsu.csc.iTrust2.unit;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

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
import edu.ncsu.csc.iTrust2.forms.CovidVaccineForm;
import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.DoseInterval;
import edu.ncsu.csc.iTrust2.services.CovidVaccineService;

@RunWith ( SpringRunner.class )
@EnableAutoConfiguration
@SpringBootTest ( classes = TestConfig.class )
public class CovidVaccineTest {

    @Autowired
    private CovidVaccineService service;

    @Before
    public void setup () {
        service.deleteAll();
    }

    @Transactional
    @Test
    public void testSavingCovidVaccine () {
        // make sure that there are no covid vaccine stored in the database
        System.out.println( "test" );
        Assert.assertEquals( 0, service.count() );

        // create a new covid vaccine by using a covid vaccine form
        final CovidVaccineForm pfizerForm = new CovidVaccineForm();
        pfizerForm.setCode( "0000-0000-00" );
        pfizerForm.setDescription( "Covid shot" );
        pfizerForm.setName( "Pfizer" );
        ArrayList<Integer> ageRange = new ArrayList<Integer>();
        ageRange.add( 12 );
        ageRange.add( 100 );
        pfizerForm.setAgeRange( ageRange );
        pfizerForm.setNumDoses( (short) 2 );
        DoseInterval doseInterval = new DoseInterval( ChronoUnit.MONTHS, 1 );
        pfizerForm.setDoseInterval( doseInterval );
        final CovidVaccine pfizer = new CovidVaccine( pfizerForm );

        // save the first covid vaccine in the database
        service.save( pfizer );

        // ensure that there exists one vaccine in the database
        Assert.assertEquals( 1, service.count() );

        // get the vaccine and check to make sure that the values are the same
        Assert.assertTrue( service.existsByCode( "0000-0000-00" ) );
        CovidVaccine returnVaccine = service.findByCode( "0000-0000-00" );
        Assert.assertEquals( pfizer, returnVaccine );
        Assert.assertTrue( "0000-0000-00".equals( returnVaccine.getCode() ) );
        Assert.assertTrue( returnVaccine.getDescription().equals( "Covid shot" ) );
        Assert.assertTrue( returnVaccine.getName().equals( "Pfizer" ) );
        Assert.assertEquals( 12, returnVaccine.getAgeRange().get( 0 ).intValue() );
        Assert.assertEquals( 100, returnVaccine.getAgeRange().get( 1 ).intValue() );
        Assert.assertEquals( 2, returnVaccine.getNumDoses() );
        Assert.assertEquals( ChronoUnit.MONTHS, returnVaccine.getDoseInterval().getIntervalType() );
        Assert.assertEquals( 1, returnVaccine.getDoseInterval().getIntervalAmount() );

        // create and add another vaccine to the database
        final CovidVaccineForm modernaForm = new CovidVaccineForm();
        modernaForm.setCode( "1111-1111-11" );
        modernaForm.setDescription( "Covid shot Number Two" );
        modernaForm.setName( "Moderna" );
        ageRange = new ArrayList<Integer>();
        ageRange.add( 18 );
        ageRange.add( 100 );
        modernaForm.setAgeRange( ageRange );
        modernaForm.setNumDoses( (short) 3 );
        doseInterval = new DoseInterval( ChronoUnit.WEEKS, 2 );
        modernaForm.setDoseInterval( doseInterval );
        final CovidVaccine moderna = new CovidVaccine( modernaForm );

        service.save( moderna );

        // ensure that there exists one vaccine in the database
        Assert.assertEquals( 2, service.count() );

        // ensure that you can get the newly added vaccine and that the values
        // are the same
        Assert.assertTrue( service.existsByCode( "1111-1111-11" ) );
        returnVaccine = service.findByCode( "1111-1111-11" );
        Assert.assertEquals( moderna, returnVaccine );
        Assert.assertTrue( "1111-1111-11".equals( returnVaccine.getCode() ) );
        Assert.assertTrue( returnVaccine.getDescription().equals( "Covid shot Number Two" ) );
        Assert.assertTrue( returnVaccine.getName().equals( "Moderna" ) );
        Assert.assertEquals( 18, returnVaccine.getAgeRange().get( 0 ).intValue() );
        Assert.assertEquals( 100, returnVaccine.getAgeRange().get( 1 ).intValue() );
        Assert.assertEquals( 3, returnVaccine.getNumDoses() );
        Assert.assertEquals( ChronoUnit.WEEKS, returnVaccine.getDoseInterval().getIntervalType() );
        Assert.assertEquals( 2, returnVaccine.getDoseInterval().getIntervalAmount() );

        // ensure that the other vaccine wasn't changed
        returnVaccine = service.findByCode( "0000-0000-00" );
        Assert.assertEquals( pfizer, returnVaccine );
        Assert.assertTrue( "0000-0000-00".equals( returnVaccine.getCode() ) );
        Assert.assertTrue( returnVaccine.getDescription().equals( "Covid shot" ) );
        Assert.assertTrue( returnVaccine.getName().equals( "Pfizer" ) );
        Assert.assertEquals( 12, returnVaccine.getAgeRange().get( 0 ).intValue() );
        Assert.assertEquals( 100, returnVaccine.getAgeRange().get( 1 ).intValue() );
        Assert.assertEquals( 2, returnVaccine.getNumDoses() );
        Assert.assertEquals( ChronoUnit.MONTHS, returnVaccine.getDoseInterval().getIntervalType() );
        Assert.assertEquals( 1, returnVaccine.getDoseInterval().getIntervalAmount() );
    }

    @Transactional
    @Test
    public void testDeleteVaccineFromDatabase () {
        // make sure that there are no covid vaccine stored in the database
        System.out.println( "test" );
        Assert.assertEquals( 0, service.count() );

        // create a new covid vaccine by using a covid vaccine form
        final CovidVaccineForm pfizerForm = new CovidVaccineForm();
        pfizerForm.setCode( "0000-0000-00" );
        pfizerForm.setDescription( "Covid shot" );
        pfizerForm.setName( "Pfizer" );
        ArrayList<Integer> ageRange = new ArrayList<Integer>();
        ageRange.add( 12 );
        ageRange.add( 100 );
        pfizerForm.setAgeRange( ageRange );
        pfizerForm.setNumDoses( (short) 2 );
        DoseInterval doseInterval = new DoseInterval( ChronoUnit.MONTHS, 1 );
        pfizerForm.setDoseInterval( doseInterval );
        final CovidVaccine pfizer = new CovidVaccine( pfizerForm );

        // save the first covid vaccine in the database
        service.save( pfizer );

        // ensure that there exists one vaccine in the database
        Assert.assertEquals( 1, service.count() );

        // create and add another vaccine to the database
        final CovidVaccineForm modernaForm = new CovidVaccineForm();
        modernaForm.setCode( "1111-1111-11" );
        modernaForm.setDescription( "Covid shot Number Two" );
        modernaForm.setName( "Moderna" );
        ageRange = new ArrayList<Integer>();
        ageRange.add( 18 );
        ageRange.add( 100 );
        modernaForm.setAgeRange( ageRange );
        modernaForm.setNumDoses( (short) 3 );
        doseInterval = new DoseInterval( ChronoUnit.WEEKS, 2 );
        modernaForm.setDoseInterval( doseInterval );
        final CovidVaccine moderna = new CovidVaccine( modernaForm );

        // save the second vaccine to the database
        service.save( moderna );

        // ensure that there exists one vaccine in the database
        Assert.assertEquals( 2, service.count() );

        // add a third vacccine to the database
        final CovidVaccineForm sputnikForm = new CovidVaccineForm();
        sputnikForm.setCode( "2222-2222-22" );
        sputnikForm.setDescription( "Covid shot Number Three" );
        sputnikForm.setName( "Sputnik" );
        ageRange = new ArrayList<Integer>();
        ageRange.add( 33 );
        ageRange.add( 45 );
        sputnikForm.setAgeRange( ageRange );
        sputnikForm.setNumDoses( (short) 1 );
        doseInterval = new DoseInterval( ChronoUnit.WEEKS, 2 );
        sputnikForm.setDoseInterval( doseInterval );
        final CovidVaccine sputnik = new CovidVaccine( sputnikForm );

        // save the thrid vaccine to the database
        service.save( sputnik );

        // ensure that there are now three vaccines in the database
        Assert.assertEquals( 3, service.count() );

        // ensure that the vaccines exist by their NDC
        Assert.assertTrue( service.existsByCode( "0000-0000-00" ) );
        Assert.assertTrue( service.existsByCode( "1111-1111-11" ) );
        Assert.assertTrue( service.existsByCode( "2222-2222-22" ) );

        // remove moderna vaccine
        service.delete( moderna );

        // ensure that there are now two vaccines in the database
        Assert.assertEquals( 2, service.count() );

        // ensure that moderna was removed
        Assert.assertTrue( service.existsByCode( "0000-0000-00" ) );
        Assert.assertFalse( service.existsByCode( "1111-1111-11" ) );
        Assert.assertTrue( service.existsByCode( "2222-2222-22" ) );

        // remove the pfizer vaccine
        service.delete( pfizer );

        // ensure that pfizer was removed
        Assert.assertFalse( service.existsByCode( "0000-0000-00" ) );
        Assert.assertFalse( service.existsByCode( "1111-1111-11" ) );
        Assert.assertTrue( service.existsByCode( "2222-2222-22" ) );

        // remove the last vaccine (sputnik)
        service.delete( sputnik );

        // ensure that sputnik was removed
        Assert.assertFalse( service.existsByCode( "0000-0000-00" ) );
        Assert.assertFalse( service.existsByCode( "1111-1111-11" ) );
        Assert.assertFalse( service.existsByCode( "2222-2222-22" ) );

    }

    @Transactional
    @Test
    public void testCovidVaccineToCovidVaccineForm () {
        // create a new covid vaccine by using a covid vaccine form
        final CovidVaccineForm pfizerForm = new CovidVaccineForm();
        pfizerForm.setCode( "0000-0000-00" );
        pfizerForm.setDescription( "Covid shot" );
        pfizerForm.setName( "Pfizer" );
        final ArrayList<Integer> ageRange = new ArrayList<Integer>();
        ageRange.add( 12 );
        ageRange.add( 100 );
        pfizerForm.setAgeRange( ageRange );
        pfizerForm.setNumDoses( (short) 2 );
        final DoseInterval doseInterval = new DoseInterval( ChronoUnit.MONTHS, 1 );
        pfizerForm.setDoseInterval( doseInterval );
        final CovidVaccine pfizer = new CovidVaccine( pfizerForm );

        // create a new form by using the Covid Vaccine object in the
        // contructor
        final CovidVaccineForm newForm = new CovidVaccineForm( pfizer );

        // ensure that the state of the newly created form is the same as the
        // old form
        Assert.assertTrue( newForm.getCode().equals( pfizerForm.getCode() ) );
        Assert.assertTrue( newForm.getDescription().equals( pfizerForm.getDescription() ) );
        Assert.assertTrue( newForm.getName().equals( pfizerForm.getName() ) );
        Assert.assertEquals( 12, newForm.getAgeRange().get( 0 ).intValue() );
        Assert.assertEquals( 100, newForm.getAgeRange().get( 1 ).intValue() );
        Assert.assertEquals( 2, newForm.getNumDoses() );
        Assert.assertEquals( newForm.getDoseInterval().getIntervalType(),
                pfizerForm.getDoseInterval().getIntervalType() );
        Assert.assertEquals( newForm.getDoseInterval().getIntervalAmount(),
                pfizerForm.getDoseInterval().getIntervalAmount() );

    }

    @Test
    public void testDoseInterval () {
        final DoseInterval vaccineInterval = new DoseInterval();
        vaccineInterval.setIntervalAmount( 2 );
        vaccineInterval.setIntervalType( ChronoUnit.MONTHS );

        Assert.assertEquals( ChronoUnit.MONTHS, vaccineInterval.getIntervalType() );
        Assert.assertEquals( 2, vaccineInterval.getIntervalAmount() );

    }

}

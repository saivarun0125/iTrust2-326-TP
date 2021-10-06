package edu.ncsu.csc.iTrust2.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.ncsu.csc.iTrust2.TestConfig;
import edu.ncsu.csc.iTrust2.forms.SatisfactionSurveyForm;
import edu.ncsu.csc.iTrust2.forms.UserForm;
import edu.ncsu.csc.iTrust2.forms.display.SatisfactionSurveyStatistics;
import edu.ncsu.csc.iTrust2.models.BasicHealthMetrics;
import edu.ncsu.csc.iTrust2.models.Diagnosis;
import edu.ncsu.csc.iTrust2.models.Drug;
import edu.ncsu.csc.iTrust2.models.Hospital;
import edu.ncsu.csc.iTrust2.models.ICDCode;
import edu.ncsu.csc.iTrust2.models.OfficeVisit;
import edu.ncsu.csc.iTrust2.models.OphthalmologyMetrics;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.Personnel;
import edu.ncsu.csc.iTrust2.models.Prescription;
import edu.ncsu.csc.iTrust2.models.SatisfactionSurvey;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.enums.AppointmentType;
import edu.ncsu.csc.iTrust2.models.enums.HouseholdSmokingStatus;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.models.enums.WaitTime;
import edu.ncsu.csc.iTrust2.services.BasicHealthMetricsService;
import edu.ncsu.csc.iTrust2.services.DrugService;
import edu.ncsu.csc.iTrust2.services.HospitalService;
import edu.ncsu.csc.iTrust2.services.ICDCodeService;
import edu.ncsu.csc.iTrust2.services.OfficeVisitService;
import edu.ncsu.csc.iTrust2.services.OphthalmologyMetricsService;
import edu.ncsu.csc.iTrust2.services.PrescriptionService;
import edu.ncsu.csc.iTrust2.services.SatisfactionSurveyService;
import edu.ncsu.csc.iTrust2.services.SatisfactionSurveyStatisticsService;
import edu.ncsu.csc.iTrust2.services.UserService;

@RunWith ( SpringRunner.class )
@EnableAutoConfiguration
@SpringBootTest ( classes = TestConfig.class )
public class SatisfactionSurveyTest {

    @Autowired
    private OfficeVisitService        officeVisitService;

    @Autowired
    private BasicHealthMetricsService basicHealthMetricsService;

    @Autowired
    private OphthalmologyMetricsService ophthalmologyMetricsService;

    @Autowired
    private HospitalService           hospitalService;

    @Autowired
    private UserService               userService;

    @Autowired
    private ICDCodeService            icdCodeService;

    @Autowired
    private DrugService               drugService;

    @Autowired
    private PrescriptionService       prescriptionService;

    @Autowired
    private SatisfactionSurveyService satisfactionSurveyService;

    @Autowired
    private SatisfactionSurveyStatisticsService satisfactionSurveyStatisticsService;

    @Before
    public void setup () {
        officeVisitService.deleteAll();

        final User oph = new Personnel( new UserForm( "oph", "123456", Role.ROLE_OPH, 1 ) );

        final User alice = new Patient( new UserForm( "AliceThirteen", "123456", Role.ROLE_PATIENT, 1 ) );

        userService.saveAll( List.of( oph, alice ) );
    }

    @Test
    @Transactional
    public void testSurveyFlow () {
        Assert.assertEquals( 0, officeVisitService.count() );

        final Hospital hosp = new Hospital( "Dr. Jenkins' Insane Asylum", "123 Main St", "12345", "NC" );
        hospitalService.save( hosp );

        final OfficeVisit visit = new OfficeVisit();

        final BasicHealthMetrics bhm = new BasicHealthMetrics();

        bhm.setDiastolic( 150 );
        bhm.setDiastolic( 100 );
        bhm.setHcp( userService.findByName( "oph" ) );
        bhm.setPatient( userService.findByName( "AliceThirteen" ) );
        bhm.setHdl( 75 );
        bhm.setHeight( 75f );
        bhm.setHouseSmokingStatus( HouseholdSmokingStatus.NONSMOKING );

        basicHealthMetricsService.save( bhm );
        visit.setBasicHealthMetrics( bhm );

        final OphthalmologyMetrics om = new OphthalmologyMetrics();
        om.setAxisLeft( 1.0f );
        om.setAxisRight( 1.0f );
        om.setCylinderLeft( 1.0f );
        om.setCylinderRight( 1.0f );
        om.setSphereLeft( 0.0f );
        om.setSphereRight( 0.0f );
        om.setVisualAcuityLeft( 20 );
        om.setVisualAcuityRight( 20 );

        ophthalmologyMetricsService.save(om);
        visit.setOphthalmologyMetrics( om );

        visit.setBasicHealthMetrics( bhm );
        visit.setType( AppointmentType.GENERAL_OPHTHALMOLOGY );
        visit.setHospital( hosp );
        visit.setPatient( userService.findByName( "AliceThirteen" ) );
        visit.setHcp( userService.findByName( "AliceThirteen" ) );
        visit.setDate( ZonedDateTime.now() );
        officeVisitService.save( visit );

        final List<Diagnosis> diagnoses = new Vector<Diagnosis>();

        final ICDCode code = new ICDCode();
        code.setCode( "O21" );
        code.setDescription( "Cataracts" );
        code.setIsOphthalmology(true);

        icdCodeService.save( code );

        final Diagnosis diagnosis = new Diagnosis();

        diagnosis.setCode( code );
        diagnosis.setNote( "This is pretty bad" );
        diagnosis.setVisit( visit );

        diagnoses.add( diagnosis );

        visit.setDiagnoses( diagnoses );

        officeVisitService.save( visit );

        final Drug drug = new Drug();

        drug.setCode( "1235-4321-89" );
        drug.setDescription( "Visine for Red eye" );
        drug.setName( "Vi2O8" );
        drugService.save( drug );

        final Prescription pres = new Prescription();
        pres.setDosage( 3 );
        pres.setDrug( drug );

        final LocalDate now = LocalDate.now();
        pres.setEndDate( now.plus( Period.ofWeeks( 5 ) ) );
        pres.setPatient( userService.findByName( "AliceThirteen" ) );
        pres.setStartDate( now );
        pres.setRenewals( 5 );

        prescriptionService.save( pres );

        final List<Prescription> pr = new ArrayList<>();
        pr.add( pres );
        visit.setPrescriptions( pr );

        officeVisitService.save( visit );

        Assert.assertEquals( 1, officeVisitService.count() );

        OfficeVisit retrieved = officeVisitService.findAll().get( 0 );

        assertNull(retrieved.getSatisfactionSurvey());


        SatisfactionSurveyForm surveyForm = new SatisfactionSurveyForm();
        surveyForm.setOfficeVisitId(retrieved.getId());
        surveyForm.setVisitSatisfaction(4);
        surveyForm.setTreatmentSatisfaction(3);
        surveyForm.setExaminationResponseTime(WaitTime.FIFTEEN_TO_TWENTY);
        surveyForm.setWaitingRoomTime(WaitTime.FIVE_TO_TEN);
        surveyForm.setComments("It was very good");

        SatisfactionSurvey survey = satisfactionSurveyService.build(surveyForm);
        satisfactionSurveyService.save(survey);
        assertNotNull(survey.getId());
        assertEquals(1, satisfactionSurveyService.findAll().size());

        retrieved.setSatisfactionSurvey(survey);
        officeVisitService.save(retrieved);

        retrieved = officeVisitService.findAll().get( 0 );
        assertNotNull(retrieved.getSatisfactionSurvey());

        SatisfactionSurveyStatistics statisticsHcp = satisfactionSurveyStatisticsService.findForHcp(visit.getHcp());
        assertNotNull(statisticsHcp);

        assertEquals(visit.getHcp().getUsername(), statisticsHcp.getHcp().getUsername());
        assertEquals(4, statisticsHcp.getAverageVisitSatisfaction().intValue());
        assertEquals(3, statisticsHcp.getAverageTreatmentSatisfaction().intValue());
        assertEquals(WaitTime.FIVE_TO_TEN, statisticsHcp.getAverageWaitingRoomTime());
        assertEquals(WaitTime.FIFTEEN_TO_TWENTY, statisticsHcp.getAverageExaminationResponseTime());

        assertNotNull(statisticsHcp.getNotes());
        assertEquals(1, statisticsHcp.getNotes().size());
        assertEquals("It was very good", statisticsHcp.getNotes().get(0).getComments());
        assertEquals(3, statisticsHcp.getNotes().get(0).getAverageSatisfaction().intValue());


    }

}

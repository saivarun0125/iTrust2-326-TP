package edu.ncsu.csc.iTrust2.services;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import edu.ncsu.csc.iTrust2.forms.VaccineOfficeVisitForm;
import edu.ncsu.csc.iTrust2.models.AppointmentRequest;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineAppointmentRequest;
import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;
import edu.ncsu.csc.iTrust2.models.enums.AppointmentType;
import edu.ncsu.csc.iTrust2.repositories.VaccineOfficeVisitRepository;

/**
 * Like the OfficeVisitService class, but specifically for VaccineOfficeVisit.
 *
 * @author Kon Buchanan
 *
 */
@Component
@Transactional
public class VaccineOfficeVisitService extends Service<VaccineOfficeVisit, Long> {

    /**
     * Repository for CRUD operations
     */
    @Autowired
    private VaccineOfficeVisitRepository     repository;

    /**
     * User service
     */
    @Autowired
    private UserService<User>                userService;

    /**
     * AppointmentRequest service
     */
    @Autowired
    private VaccineAppointmentRequestService vaccineAppointmentRequestService;

    /**
     * Hospital Service
     */
    @Autowired
    private HospitalService                  hospitalService;

    // /**
    // * BasicHealthMetrics service
    // */
    // @Autowired
    // private BasicHealthMetricsService bhmService;
    //
    // /**
    // * OphthalmologyMetrics service
    // */
    // @Autowired
    // private OphthalmologyMetricsService omService;
    //
    // /**
    // * Prescription service
    // */
    // @Autowired
    // private PrescriptionService prescriptionService;
    //
    // /**
    // * Diagnosis service
    // */
    // @Autowired
    // private DiagnosisService diagnosisService;

    /** Covid Vaccine Service */
    @Autowired
    private CovidVaccineService              covidVaccineService;

    @Override
    protected JpaRepository<VaccineOfficeVisit, Long> getRepository () {
        return repository;
    }

    /**
     * Finds all VaccineOfficeVisits created by the specified HCP
     *
     * @param hcp
     *            HCP to search for
     * @return Matching OfficeVisits
     */
    public List<VaccineOfficeVisit> findByHcp ( final User hcp ) {
        return repository.findByHcp( hcp );
    }

    /**
     * Finds all VaccineOfficeVisits for the specified Patient
     *
     * @param patient
     *            Patient to search for
     * @return Matching OfficeVisits
     */
    public List<VaccineOfficeVisit> findByPatient ( final User patient ) {
        return repository.findByPatient( patient );
    }

    /**
     * Find all VaccineOfficeVisits for both the specified Patient and HCP
     *
     * @param hcp
     *            HCP to search for
     * @param patient
     *            Patient to search for
     * @return List of visits found
     */
    public List<VaccineOfficeVisit> findByHcpAndPatient ( final User hcp, final User patient ) {
        return repository.findByHcpAndPatient( hcp, patient );
    }

    /**
     * Builds an VaccineOfficeVisit based on the deserialised
     * VaccineOfficeVisitForm
     *
     * @param ovf
     *            Form to build from
     * @return Constructed VaccineOfficeVisit
     */
    public VaccineOfficeVisit build ( final VaccineOfficeVisitForm ovf ) {
        final VaccineOfficeVisit ov = new VaccineOfficeVisit();

        ov.setPatient( userService.findByName( ovf.getPatient() ) );
        ov.setHcp( userService.findByName( ovf.getHcp() ) );
        ov.setNotes( ovf.getNotes() );
        ov.setDoseNumber( ovf.getDoseNumber() );
        ov.setScheduled( ovf.isScheduled() );
        ov.setVaccine( covidVaccineService.findByCode( ovf.getVaccine() ) );

        ov.validateDoseNumber();
        AppointmentType at = null;
        try {
            at = AppointmentType.valueOf( ovf.getType() );
        }
        catch ( final NullPointerException npe ) {
            at = AppointmentType.VACCINE_APPOINTMENT;
        }
        ov.setType( at );
        ov.validateVaccine();

        if ( ovf.getId() != null ) {
            ov.setId( Long.parseLong( ovf.getId() ) );
        }

        final ZonedDateTime visitDate = ZonedDateTime.parse( ovf.getDate() );
        ov.setDate( visitDate );

        if ( ovf.isScheduled() ) {
            final List<VaccineAppointmentRequest> requests = vaccineAppointmentRequestService
                    .findByHcpAndPatient( ov.getHcp(), ov.getPatient() );
            try {
                final AppointmentRequest match = requests.stream().filter( e -> e.getDate().equals( ov.getDate() ) )
                        .collect( Collectors.toList() )
                        .get( 0 ); /*
                                    * We should have one and only one
                                    * appointment for the provided HCP & patient
                                    * and the time specified
                                    */
                ov.setAppointment( match );
            }
            catch ( final Exception e ) {
                throw new IllegalArgumentException( "Marked as scheduled but no match can be found" + e.toString() );
            }

        }

        System.out.println( "set hospital" );
        ov.setHospital( hospitalService.findByName( ovf.getHospital() ) );

        final Patient p = (Patient) ov.getPatient();
        if ( p == null || p.getDateOfBirth() == null ) {
            return ov; // we're done, patient can't be tested against
        }

        if ( ov.getDoseNumber() == null ) {
            throw new IllegalArgumentException( "Dose number must be filled." );
        }

        final LocalDate dob = p.getDateOfBirth();
        int age = ov.getDate().getYear() - dob.getYear();
        // Remove the -1 when changing the dob to OffsetDateTime
        if ( ov.getDate().getMonthValue() < dob.getMonthValue() ) {
            age -= 1;
        }
        else if ( ov.getDate().getMonthValue() == dob.getMonthValue() ) {
            if ( ov.getDate().getDayOfMonth() < dob.getDayOfMonth() ) {
                age -= 1;
            }
        }
        // TODO: Do Dob testing relevent to Covid Vaccines
        if ( age < ov.getVaccine().getAgeRange().get( 0 ) || age > ov.getVaccine().getAgeRange().get( 1 ) ) {
            throw new IllegalArgumentException( "Patient's age must be within vaccine's age range" );
        }
        return ov;
    }
}

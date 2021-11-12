package edu.ncsu.csc.iTrust2.services;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

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

    // /**
    // * User service
    // */
    // @Autowired
    // private UserService<User> userService;

    /**
     * AppointmentRequest service
     */
    @Autowired
    private VaccineAppointmentRequestService vaccineAppointmentRequestService;

    // /**
    // * Hospital Service
    // */
    // @Autowired
    // private HospitalService hospitalService;
    //
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
    public VaccineOfficeVisit build ( final VaccineOfficeVisit ovf ) {
        final VaccineOfficeVisit ov = new VaccineOfficeVisit();

        ov.setPatient( ovf.getPatient() );
        ov.setHcp( ovf.getHcp() );
        ov.setNotes( ovf.getNotes() );
        ov.setDoseNumber( ovf.getDoseNumber() );
        ov.setScheduled( ovf.isScheduled() );
        ov.setVaccine( covidVaccineService.findByCode( ovf.getVaccine().getCode() ) );

        ov.validateDoseNumber();
        ov.validateVaccine();

        if ( ovf.getId() != null ) {
            ov.setId( ( ovf.getId() ) );
        }

        final ZonedDateTime visitDate = ovf.getDate();
        ov.setDate( visitDate );

        AppointmentType at = null;
        try {
            at = ovf.getType();
        }
        catch ( final NullPointerException npe ) {
            at = AppointmentType.GENERAL_CHECKUP; /*
                                                   * If for some reason we don't
                                                   * have a type, default to
                                                   * general checkup
                                                   */
        }
        ov.setType( at );

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
        ov.setHospital( ovf.getHospital() );
        // ov.setBasicHealthMetrics( bhmService.build( ovf ) );
        // ov.setOphthalmologyMetrics( omService.build( ovf ) );
        // associate all diagnoses with this visit
        // if ( ovf.getDiagnoses() != null ) {
        // ov.setDiagnoses(
        // ovf.getDiagnoses().stream().map( diagnosisService::build ).collect(
        // Collectors.toList() ) );
        // for ( final Diagnosis d : ov.getDiagnoses() ) {
        // d.setVisit( ov );
        // }
        // }

        // ov.validateDiagnoses();
        // ov.validateOphthalmology();

        // final List<PrescriptionForm> ps = ovf.getPrescriptions();
        // if ( ps != null ) {
        // ov.setPrescriptions( ps.stream().map( prescriptionService::build
        // ).collect( Collectors.toList() ) );
        // }

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

        if ( age < 3 ) {
            ov.validateUnder3();
        }
        else if ( age < 12 ) {
            ov.validateUnder12();
        }
        else {
            ov.validate12AndOver();
        }

        return ov;
    }
}

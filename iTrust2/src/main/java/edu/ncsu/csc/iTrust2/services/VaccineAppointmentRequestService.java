
package edu.ncsu.csc.iTrust2.services;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import edu.ncsu.csc.iTrust2.forms.VaccineAppointmentRequestForm;
import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineAppointmentRequest;
import edu.ncsu.csc.iTrust2.models.enums.AppointmentType;
import edu.ncsu.csc.iTrust2.models.enums.Status;
import edu.ncsu.csc.iTrust2.repositories.CovidVaccineRepository;
import edu.ncsu.csc.iTrust2.repositories.VaccineAppointmentRequestRepository;

/**
 * Service class for interacting with AppointmentRequest model, performing CRUD
 * tasks with database and building a persistence object from a Form.
 *
 * @author Sai Maale
 *
 */
@Component
@Transactional
public class VaccineAppointmentRequestService extends Service<VaccineAppointmentRequest, Long> {

    /** Repository for CRUD tasks */
    @Autowired
    private VaccineAppointmentRequestRepository repository;

    /** Repository for Vaccine CRUD tasks */
    @Autowired
    private CovidVaccineRepository              repos;

    /** UserService for CRUD operations on User */
    @Autowired
    private UserService<User>                   userService;

    /** VaccineService for CRUD operations on Vaccines */
    @Autowired
    private CovidVaccineService                 vaccineService;

    @Override
    protected JpaRepository<VaccineAppointmentRequest, Long> getRepository () {
        return repository;
    }

    /**
     * Find all vaccine appointment requests for a given Patient
     *
     * @param patient
     *            Patient for lookups
     * @return Matching requests
     */
    public List<VaccineAppointmentRequest> findByPatient ( final User patient ) {
        return repository.findByPatient( patient );
    }

    /**
     * Find all vaccine appointment requests for a given HCP
     *
     * @param hcp
     *            HCP for lookups
     * @return Matching requests
     */
    public List<VaccineAppointmentRequest> findByHcp ( final User hcp ) {
        return repository.findByHcp( hcp );
    }

    /**
     * Find all vaccine appointment requests for a given HCP and patient
     *
     * @param hcp
     *            HCP for lookups
     * @param patient
     *            Patient for lookups
     * @return Matching requests
     */
    public List<VaccineAppointmentRequest> findByHcpAndPatient ( final User hcp, final User patient ) {
        return repository.findByHcpAndPatient( hcp, patient );
    }

    /**
     * Builds a VaccineAppointmentRequest
     *
     * @param raf
     *            AppointmentRequestForm containing data to build an AR from
     * @return Built AppointmentRequest
     */
    public VaccineAppointmentRequest build ( final VaccineAppointmentRequestForm raf ) {
        final VaccineAppointmentRequest ar = new VaccineAppointmentRequest();

        ar.setPatient( userService.findByName( raf.getPatient() ) );

        final Patient p = (Patient) ar.getPatient();
        if ( p == null || p.getDateOfBirth() == null ) {
            throw new IllegalArgumentException( "Patient must have a set age to request a vaccination appointment" );
        }

        ar.setHcp( userService.findByName( raf.getHcp() ) );

        ar.setComments( raf.getComments() );

        final ZonedDateTime requestDate = ZonedDateTime.parse( raf.getDate() );

        if ( requestDate.isBefore( ZonedDateTime.now() ) ) {
            throw new IllegalArgumentException( "Cannot request an appointment before the current time" );
        }
        else {
            ar.setDate( requestDate );
        }
        final CovidVaccine vaccine = repos.findByCode( raf.getVaccine() );
        ar.setVaccine( vaccine );

        Status s = null;
        try {
            s = Status.valueOf( raf.getStatus() );
        }
        catch ( final NullPointerException npe ) {
            s = Status.PENDING; /*
                                 * Incoming AppointmentRequests will come in
                                 * from the form with no status. Set status to
                                 * Pending until it is adjusted further
                                 */
        }
        ar.setStatus( s );
        AppointmentType at = null;
        try {
            at = AppointmentType.valueOf( raf.getType() );
        }
        catch ( final NullPointerException npe ) {
            at = AppointmentType.VACCINE_APPOINTMENT;
        }
        ar.setType( at );

        final LocalDate dob;
        int age;
        try {
            dob = p.getDateOfBirth();
            age = ar.getDate().getYear() - dob.getYear();
            // Remove the -1 when changing the dob to OffsetDateTime
            if ( ar.getDate().getMonthValue() < dob.getMonthValue() ) {
                age -= 1;
            }
            else if ( ar.getDate().getMonthValue() == dob.getMonthValue() ) {
                if ( ar.getDate().getDayOfMonth() < dob.getDayOfMonth() ) {
                    age -= 1;
                }
            }
        }
        catch ( final Exception e ) {
            throw new IllegalArgumentException( "Patient must have a set age to request a vaccination appointment" );
        }
        // TODO: Do Dob testing relevent to Covid Vaccines
        if ( age < ar.getVaccine().getAgeRange().get( 0 ) || age > ar.getVaccine().getAgeRange().get( 1 ) ) {
            throw new IllegalArgumentException( "Patient's age must be within vaccine's age range" );
        }

        return ar;
    }

}

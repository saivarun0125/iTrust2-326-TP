
package edu.ncsu.csc.iTrust2.services;

import java.time.ZonedDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import edu.ncsu.csc.iTrust2.forms.VaccineAppointmentRequestForm;
import edu.ncsu.csc.iTrust2.models.CovidVaccine;
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
        ar.setHcp( userService.findByName( raf.getHcp() ) );

        ar.setComments( raf.getComments() );

        final ZonedDateTime requestDate = ZonedDateTime.parse( raf.getDate() );
        if ( requestDate.isBefore( ZonedDateTime.now() ) ) {
            throw new IllegalArgumentException( "Cannot request an appointment before the current time" );
        }
        ar.setDate( requestDate );
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
            at = AppointmentType.GENERAL_CHECKUP; /*
                                                   * If for some reason we don't
                                                   * have a type, default to
                                                   * general checkup
                                                   */
        }
        ar.setType( at );

        return ar;
    }

}

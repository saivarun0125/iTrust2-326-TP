package edu.ncsu.csc.iTrust2.controllers.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc.iTrust2.forms.VaccineOfficeVisitForm;
import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineDose;
import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;
import edu.ncsu.csc.iTrust2.models.enums.TransactionType;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.services.VaccineOfficeVisitService;
import edu.ncsu.csc.iTrust2.utils.LoggerUtil;

/**
 * API controller for interacting with the VaccineOfficeVisit model. Provides
 * standard CRUD routes as appropriate for different user types
 *
 * @author Kai Presler-Marshall
 * @author Robby Rivenbark
 */
@RestController
@SuppressWarnings ( { "unchecked", "rawtypes" } )
public class APIVaccineOfficeVisitController extends APIController {

    /** VaccineOfficeVisit service */
    @Autowired
    private VaccineOfficeVisitService vaccineOfficeVisitService;

    /** User service */
    @Autowired
    private UserService<User>         userService;

    /** LoggerUtil */
    @Autowired
    private LoggerUtil                loggerUtil;

    /**
     * Retrieves a list of all VaccineOfficeVisits in the database
     *
     * @return list of VaccineOfficeVisits
     */
    @GetMapping ( BASE_PATH + "/vaccineofficevisits" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP')" )
    public List<VaccineOfficeVisit> getVaccineOfficeVisits () {
        loggerUtil.log( TransactionType.VIEW_ALL_VACCINE_OFFICE_VISITS, LoggerUtil.currentUser() );
        return vaccineOfficeVisitService.findAll();
    }

    /**
     * Retrieves all of the VaccineOfficeVisits for the current HCP.
     *
     * @return all of the VaccineOfficeVisits for the current HCP.
     */
    @GetMapping ( BASE_PATH + "/vaccineofficevisits/HCP" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP')" )
    public List<VaccineOfficeVisit> getVaccineOfficeVisitsForHCP () {
        final User self = userService.findByName( LoggerUtil.currentUser() );
        loggerUtil.log( TransactionType.VIEW_ALL_VACCINE_OFFICE_VISITS, self );
        final List<VaccineOfficeVisit> visits = vaccineOfficeVisitService.findByHcp( self );
        return visits;
    }

    /**
     * Retrieves a list of all VaccineOfficeVisits in the database for the
     * current patient
     *
     * @return list of VaccineOfficeVisits
     */
    @GetMapping ( BASE_PATH + "/vaccineofficevisits/myvaccineofficevisits" )
    @PreAuthorize ( "hasAnyRole('ROLE_PATIENT')" )
    public List<VaccineOfficeVisit> getMyVaccineOfficeVisits () {
        final User self = userService.findByName( LoggerUtil.currentUser() );
        loggerUtil.log( TransactionType.VACCINE_OFFICE_VISIT_PATIENT_VIEW, self );
        return vaccineOfficeVisitService.findByPatient( self );
    }

    /**
     * Retrieves a specific VaccineOfficeVisit in the database, with the given
     * ID
     *
     * @param id
     *            ID of the VaccineOfficeVisit to retrieve
     * @return list of VaccineOfficeVisits
     */
    @GetMapping ( BASE_PATH + "/vaccineofficevisits/{id}" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP')" )
    public ResponseEntity getVaccineOfficeVisit ( @PathVariable final Long id ) {
        final User self = userService.findByName( LoggerUtil.currentUser() );
        loggerUtil.log( TransactionType.VACCINE_OFFICE_VISIT_HCP_VIEW, self );
        if ( !vaccineOfficeVisitService.existsById( id ) ) {
            return new ResponseEntity( HttpStatus.NOT_FOUND );
        }

        return new ResponseEntity( vaccineOfficeVisitService.findById( id ), HttpStatus.OK );
    }

    /**
     * Creates and saves a new VaccineOfficeVisit from the RequestBody provided.
     *
     * @param visitForm
     *            The VaccineOfficeVisit to be validated and saved
     * @return response
     */
    @PostMapping ( BASE_PATH + "/vaccineofficevisits" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP')" )
    public ResponseEntity createVaccineOfficeVisit ( @RequestBody final VaccineOfficeVisitForm visitForm ) {
        try {
            final VaccineOfficeVisit visit = vaccineOfficeVisitService.build( visitForm );

            if ( null != visit.getId() && vaccineOfficeVisitService.existsById( visit.getId() ) ) {
                return new ResponseEntity(
                        errorResponse( "Vaccine Office visit with the id " + visit.getId() + " already exists" ),
                        HttpStatus.CONFLICT );
            }
            // check if patient is fully vaccinated
            final Patient patient = (Patient) visit.getPatient();
            final int isFullyVaxxed = this.isFullyVaxxed( patient, visit.getVaccine() );

            // if not fully vaxxed, vaccine may be adminisered. Add vaccine to
            // patient's vaccines recieved.
            if ( isFullyVaxxed == 0 ) {
                if ( !this.correctDoseNumber( visit.getDoseNumber(), patient ) ) {
                    return new ResponseEntity( errorResponse( "Invalid Dose Number" ), HttpStatus.CONFLICT );
                }
                vaccineOfficeVisitService.save( visit );
                loggerUtil.log( TransactionType.VACCINE_OFFICE_VISIT_CREATE, LoggerUtil.currentUser(),
                        visit.getPatient().getUsername() );
                final CovidVaccine vax = visit.getVaccine();
                // We do this so we don't get duplicates in a patient's
                // vaccination list.
                final VaccineDose vaxToAdd = new VaccineDose( vax.getCode(), visit.getDoseNumber(), vax.getNumDoses() );
                patient.getVaccinesRecieved().add( vaxToAdd );
                userService.save( patient );

                return new ResponseEntity( visit, HttpStatus.OK );
            }
            // otherwise, return an appropriate response
            else if ( isFullyVaxxed == 1 ) {
                return new ResponseEntity( errorResponse( "Patient is already fully vaccinated" ),
                        HttpStatus.CONFLICT );
            }
            else {
                return new ResponseEntity( errorResponse( "Patient cannot mix and match vaccines" ),
                        HttpStatus.CONFLICT );
            }

        }
        catch ( final Exception e ) {
            e.printStackTrace();
            System.out.println( e.getMessage() );
            // System.out.println( "exception!!" );
            return new ResponseEntity(
                    errorResponse(
                            "Could not validate or save the VaccineOfficeVisit provided due to " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Creates and saves a new VaccineOfficeVisit from the RequestBody provided.
     *
     * @param id
     *            ID of the VaccineOfficeVisit to update
     * @param visitForm
     *            The VaccineOfficeVisit to be validated and saved
     * @return response
     */
    @PutMapping ( BASE_PATH + "/vaccineofficevisits/{id}" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP')" )
    public ResponseEntity updateVaccineOfficeVisit ( @PathVariable final Long id,
            @RequestBody final VaccineOfficeVisitForm visitForm ) {
        try {
            final VaccineOfficeVisit visit = vaccineOfficeVisitService.build( visitForm );

            if ( null == visit.getId() || !vaccineOfficeVisitService.existsById( visit.getId() ) ) {
                return new ResponseEntity(
                        errorResponse( "Vaccine office visit with the id " + visit.getId() + " doesn't exist" ),
                        HttpStatus.NOT_FOUND );
            }
            vaccineOfficeVisitService.save( visit );
            loggerUtil.log( TransactionType.VACCINE_OFFICE_VISIT_CREATE, LoggerUtil.currentUser(),
                    visit.getPatient().getUsername() );
            return new ResponseEntity( visit, HttpStatus.OK );
        }
        catch (

        final Exception e ) {
            e.printStackTrace();
            return new ResponseEntity(
                    errorResponse(
                            "Could not validate or save the VaccineOfficeVisit provided due to " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * 1 means fully vaxxed, 0 means not fully vaxxed, 2 means attempted mixing
     * of vaccines
     *
     * @param patient
     *            the patient
     * @param vaccineToBeAdministered
     *            the vaccine
     * @return indication of vaccination
     */
    private int isFullyVaxxed ( Patient patient, CovidVaccine vaccineToBeAdministered ) {
        // if the patient hasn't received any vaccines, patient is not fully
        // vaxxed
        if ( patient.getVaccinesRecieved() == null ) {
            patient.setVaccinesRecieved( new ArrayList<VaccineDose>() );
            return 0;
        }

        // This isn't currently needed but would help the system be extended in
        // the future. E.g. if you wanted to add more types of vaccines you
        // could make specific types of vaccine doses extend vaccine dose and
        // filter them like this
        final ArrayList<VaccineDose> vaccinesRecieved = new ArrayList<VaccineDose>();
        for ( final VaccineDose vaccine : patient.getVaccinesRecieved() ) {
            if ( vaccine.getClass().getName().equals( "edu.ncsu.csc.iTrust2.models.VaccineDose" ) ) {
                vaccinesRecieved.add( vaccine );
            }
        }

        if ( vaccinesRecieved.size() == 0 ) {
            return 0;
        }

        // checks for if the size equals one
        if ( vaccinesRecieved.size() == 1 ) {
            // if vaccine recieved was a one dose vaccine, patient is fully
            // vaxxed
            if ( vaccinesRecieved.get( 0 ).getNumDoses() == (short) 1 ) {
                return 1;
            }
            // check to see if the vaccine to be administered is the same as the
            // one the patient already has
            else {
                if ( vaccineToBeAdministered.getCode().equals( vaccinesRecieved.get( 0 ).getCode() ) ) {
                    return 0;
                }
                else {
                    return 2;
                }
            }
        }
        return 1;
    }

    private boolean correctDoseNumber ( int doseNumber, Patient patient ) {
        if ( patient.getVaccinesRecieved().size() == 0 && doseNumber == 1 ) {
            return true;
        }
        else if ( patient.getVaccinesRecieved().size() == 1 && doseNumber == 2 ) {
            return true;
        }
        else {
            return false;
        }
    }

}

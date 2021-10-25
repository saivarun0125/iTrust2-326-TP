package edu.ncsu.csc.iTrust2.controllers.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc.iTrust2.forms.CovidVaccineForm;
import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.enums.TransactionType;
import edu.ncsu.csc.iTrust2.services.CovidVaccineService;
import edu.ncsu.csc.iTrust2.utils.LoggerUtil;

/**
 * Provides REST endpoints that deal with CovidVaccines. Exposes functionality
 * to add, edit, fetch, and delete CovidVaccine.
 *
 * @author Connor
 * @author Kai Presler-Marshall
 */
@SuppressWarnings ( { "unchecked", "rawtypes" } )
@RestController
public class APICovidVaccineController extends APIController {

    /** CovidVaccine service */
    @Autowired
    private CovidVaccineService service;

    /** LoggerUtil */
    @Autowired
    private LoggerUtil          loggerUtil;

    /**
     * Adds a new CovidVaccine to the system. Requires admin permissions.
     * Returns an error message if something goes wrong.
     *
     * @param form
     *            the CovidVaccine form
     * @return the created CovidVaccine
     */
    @PreAuthorize ( "hasRole('ROLE_ADMIN')" )
    @PostMapping ( BASE_PATH + "/CovidVaccines" )
    public ResponseEntity addCovidVaccine ( @RequestBody final CovidVaccineForm form ) {
        try {
            final CovidVaccine CovidVaccine = new CovidVaccine( form );

            // Make sure code does not conflict with existing CovidVaccines
            if ( service.existsByCode( CovidVaccine.getCode() ) ) {
                loggerUtil.log( TransactionType.COVID_VACCINE_CREATE, LoggerUtil.currentUser(),
                        "Conflict: Covid Vaccine with code " + CovidVaccine.getCode() + " already exists" );
                return new ResponseEntity(
                        errorResponse( "Covid Vaccine with code " + CovidVaccine.getCode() + " already exists" ),
                        HttpStatus.CONFLICT );
            }

            service.save( CovidVaccine );
            loggerUtil.log( TransactionType.COVID_VACCINE_CREATE, LoggerUtil.currentUser(),
                    "Covid Vaccine " + CovidVaccine.getCode() + " created" );
            return new ResponseEntity( CovidVaccine, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            loggerUtil.log( TransactionType.COVID_VACCINE_CREATE, LoggerUtil.currentUser(),
                    "Failed to create Covid Vaccine" );
            return new ResponseEntity( errorResponse( "Could not add CovidVaccine: " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Edits a CovidVaccine in the system. The id stored in the form must match
     * an existing CovidVaccine, and changes to NDCs cannot conflict with
     * existing NDCs. Requires admin permissions.
     *
     * @param form
     *            the edited CovidVaccine form
     * @return the edited CovidVaccine or an error message
     */
    @PreAuthorize ( "hasRole('ROLE_ADMIN')" )
    @PutMapping ( BASE_PATH + "/CovidVaccines" )
    public ResponseEntity editCovidVaccine ( @RequestBody final CovidVaccineForm form ) {
        try {
            // Check for existing CovidVaccine in database
            final CovidVaccine savedCovidVaccine = service.findById( form.getId() );
            if ( savedCovidVaccine == null ) {
                return new ResponseEntity( errorResponse( "No Covid Vaccine found with code " + form.getCode() ),
                        HttpStatus.NOT_FOUND );
            }

            final CovidVaccine covidVaccine = new CovidVaccine( form );

            // If the code was changed, make sure it is unique
            final CovidVaccine sameCode = service.findByCode( covidVaccine.getCode() );
            if ( sameCode != null && !sameCode.getId().equals( savedCovidVaccine.getId() ) ) {
                return new ResponseEntity(
                        errorResponse( "Covid Vaccine with code " + covidVaccine.getCode() + " already exists" ),
                        HttpStatus.CONFLICT );
            }

            covidVaccine.setId( savedCovidVaccine.getId() );

            service.save( covidVaccine ); /* Overwrite existing CovidVaccine */

            loggerUtil.log( TransactionType.COVID_VACCINE_EDIT, LoggerUtil.currentUser(),
                    "Covid Vaccine with id " + covidVaccine.getId() + " edited" );
            return new ResponseEntity( covidVaccine, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            loggerUtil.log( TransactionType.COVID_VACCINE_EDIT, LoggerUtil.currentUser(),
                    "Failed to edit Covid Vaccine" );
            return new ResponseEntity( errorResponse( "Could not update Covid Vaccine: " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Deletes the CovidVaccine with the id matching the given id. Requires
     * admin permissions.
     *
     * @param id
     *            the id of the CovidVaccine to delete
     * @return the id of the deleted CovidVaccine
     */
    @PreAuthorize ( "hasRole('ROLE_ADMIN')" )
    @DeleteMapping ( BASE_PATH + "/CovidVaccines/{id}" )
    public ResponseEntity deleteCovidVaccine ( @PathVariable final String id ) {
        try {
            final CovidVaccine CovidVaccine = service.findById( Long.parseLong( id ) );
            if ( CovidVaccine == null ) {
                loggerUtil.log( TransactionType.COVID_VACCINE_DELETE, LoggerUtil.currentUser(),
                        "Could not find Covid Vaccine with id " + id );
                return new ResponseEntity( errorResponse( "No Covid Vaccine found with id " + id ),
                        HttpStatus.NOT_FOUND );
            }
            service.delete( CovidVaccine );
            loggerUtil.log( TransactionType.COVID_VACCINE_DELETE, LoggerUtil.currentUser(),
                    "Deleted Covid Vaccine with id " + CovidVaccine.getId() );
            return new ResponseEntity( id, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            loggerUtil.log( TransactionType.COVID_VACCINE_DELETE, LoggerUtil.currentUser(),
                    "Failed to delete CovidVaccine" );
            return new ResponseEntity( errorResponse( "Could not delete Covid Vaccine: " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Gets a list of all the CovidVaccines in the system.
     *
     * @return a list of CovidVaccines
     */
    @GetMapping ( BASE_PATH + "/CovidVaccines" )
    public List<CovidVaccine> getCovidVaccines () {
        loggerUtil.log( TransactionType.COVID_VACCINE_VIEW, LoggerUtil.currentUser(),
                "Fetched list of Covid Vaccines" );
        return service.findAll();
    }

}

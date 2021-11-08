package edu.ncsu.csc.iTrust2.controllers.api;

import java.util.List;
import java.util.stream.Collectors;

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

import edu.ncsu.csc.iTrust2.forms.VaccineAppointmentRequestForm;
import edu.ncsu.csc.iTrust2.models.AppointmentRequest;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineAppointmentRequest;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.models.enums.Status;
import edu.ncsu.csc.iTrust2.models.enums.TransactionType;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.services.VaccineAppointmentRequestService;
import edu.ncsu.csc.iTrust2.utils.LoggerUtil;

/**
 * Class that provides REST API endpoints for the AppointmentRequest model. In
 * all requests made to this controller, the {id} provided is a numeric ID that
 * is the primary key of the appointment request in question
 *
 * @author Kai Presler-Marshall
 * @author Matt Dzwonczyk
 * 
 * @author Weston Greene
 */
@RestController
@SuppressWarnings ( { "unchecked", "rawtypes" } )
public class APIVaccineAppointmentRequestController extends APIController {

    /**
     * AppointmentRequest service
     */
    @Autowired
    private VaccineAppointmentRequestService service;

    /** LoggerUtil */
    @Autowired
    private LoggerUtil                loggerUtil;

    /** User service */
    @Autowired
    private UserService<User>         userService;

    /**
     * Retrieves a list of all VaccineAppointmentRequests in the database
     *
     * @return list of vaccine appointment requests
     */
    @GetMapping ( BASE_PATH + "/vaccineappointmentrequests" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP')" )
    public List<VaccineAppointmentRequest> getVaccineAppointmentRequests () {
        final List<VaccineAppointmentRequest> requests = service.findAll();

        requests.stream().map( AppointmentRequest::getPatient ).distinct().forEach( e -> loggerUtil
                .log( TransactionType.APPOINTMENT_REQUEST_VIEWED, LoggerUtil.currentUser(), e.getUsername() ) );

        return requests;
    }

    /**
     * Retrieves the VaccineAppointmentRequests specified by the username provided
     *
     * @return list of vaccine appointment requests for the logged in patient
     */
    @GetMapping ( BASE_PATH + "/vaccineappointmentrequest" )
    @PreAuthorize ( "hasAnyRole('ROLE_PATIENT')" )
    public List<VaccineAppointmentRequest> getVaccineAppointmentRequestsForPatient () {
        final User patient = userService.findByName( LoggerUtil.currentUser() );
        return service.findByPatient( patient ).stream().filter( e -> e.getStatus().equals( Status.PENDING ) )
                .collect( Collectors.toList() );
    }

    /**
     * Retrieves the VaccineAppointmentRequests specified by the username provided
     *
     * @return list of appointment requests for the logged in HCP
     */
    @GetMapping ( BASE_PATH + "/vaccineappointmentrequestForHCP" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP')" )
    public List<VaccineAppointmentRequest> getVaccineAppointmentRequestsForHCP () {

        final User hcp = userService.findByName( LoggerUtil.currentUser() );

        return service.findByHcp( hcp ).stream().filter( e -> e.getStatus().equals( Status.PENDING ) )
                .collect( Collectors.toList() );

    }

    /**
     * Retrieves the VaccineAppointmentRequest specified by the ID provided
     *
     * @param id
     *            The (numeric) ID of the AppointmentRequest desired
     * @return The AppointmentRequest corresponding to the ID provided or
     *         HttpStatus.NOT_FOUND if no such AppointmentRequest could be found
     */
    @GetMapping ( BASE_PATH + "/vaccineappointmentrequests/{id}" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP', 'ROLE_PATIENT')" )
    public ResponseEntity getVaccineAppointmentRequest ( @PathVariable ( "id" ) final Long id ) {
        final VaccineAppointmentRequest request = service.findById( id );
        if ( null != request ) {
            loggerUtil.log( TransactionType.APPOINTMENT_REQUEST_VIEWED, request.getPatient(), request.getHcp() );

            /* Patient can't look at anyone else's requests */
            final User self = userService.findByName( LoggerUtil.currentUser() );
            if ( self.getRoles().contains( Role.ROLE_PATIENT ) && !request.getPatient().equals( self ) ) {
                return new ResponseEntity( HttpStatus.UNAUTHORIZED );
            }
        }
        return null == request
                ? new ResponseEntity( errorResponse( "No AppointmentRequest found for id " + id ),
                        HttpStatus.NOT_FOUND )
                : new ResponseEntity( request, HttpStatus.OK );
    }

    /**
     * Creates an VaccineAppointmentRequest from the RequestBody provided. Record is
     * automatically saved in the database.
     *
     * @param vaccinerequestForm
     *            The VaccineAppointmentRequestForm to be parsed into an
     *            VaccineAppointmentRequest and stored
     * @return The parsed and validated AppointmentRequest created from the Form
     *         provided, HttpStatus.CONFLICT if a Request already exists with
     *         the ID of the provided request, or HttpStatus.BAD_REQUEST if
     *         another error occurred while parsing or saving the Request
     *         provided
     */
    @PostMapping ( BASE_PATH + "/vaccineappointmentrequests" )
    @PreAuthorize ( "hasRole('ROLE_PATIENT')" )
    public ResponseEntity createVaccineAppointmentRequest ( @RequestBody final VaccineAppointmentRequestForm vaccineRequestForm ) {
        try {
            final VaccineAppointmentRequest request = service.build( vaccineRequestForm );
            if ( null != service.findById( request.getId() ) ) {
                return new ResponseEntity(
                        errorResponse( "AppointmentRequest with the id " + request.getId() + " already exists" ),
                        HttpStatus.CONFLICT );
            }
            service.save( request );
            loggerUtil.log( TransactionType.APPOINTMENT_REQUEST_SUBMITTED, request.getPatient(), request.getHcp() );
            return new ResponseEntity( request, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            return new ResponseEntity( errorResponse( "Error occurred while validating or saving "
                    + vaccineRequestForm.toString() + " because of " + e.getMessage() ), HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Deletes the VaccineAppointmentRequest with the id provided. This will remove all
     * traces from the system and cannot be reversed.
     *
     * @param id
     *            The id of the VaccineAppointmentRequest to delete
     * @return response
     */
    @DeleteMapping ( BASE_PATH + "/vaccineappointmentrequests/{id}" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP', 'ROLE_PATIENT')" )
    public ResponseEntity deleteVaccineAppointmentRequest ( @PathVariable final Long id ) {
        final VaccineAppointmentRequest request = service.findById( id );
        if ( null == request ) {
            return new ResponseEntity( errorResponse( "No AppointmentRequest found for id " + id ),
                    HttpStatus.NOT_FOUND );
        }

        /* Patient can't look at anyone else's requests */
        final User self = userService.findByName( LoggerUtil.currentUser() );
        if ( self.getRoles().contains( Role.ROLE_PATIENT ) && !request.getPatient().equals( self ) ) {
            return new ResponseEntity( HttpStatus.UNAUTHORIZED );
        }
        try {
            service.delete( request );
            loggerUtil.log( TransactionType.APPOINTMENT_REQUEST_DELETED, request.getPatient(), request.getHcp() );
            return new ResponseEntity( id, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            return new ResponseEntity(
                    errorResponse( "Could not delete " + request.toString() + " because of " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }

    }

    /**
     * Updates the VaccineAppointmentRequest with the id provided by overwriting it
     * with the new VaccineAppointmentRequest that is provided. If the ID provided does
     * not match the ID set in the VaccineAppointmentRequest provided, the update will
     * not take place
     *
     * @param id
     *            The ID of the VaccineAppointmentRequest to be updated
     * @param requestF
     *            The updated VaccineAppointmentRequestForm to parse, validate, and
     *            save
     * @return The VaccineAppointmentRequest that is created from the Form that is
     *         provided
     */
    @PutMapping ( BASE_PATH + "/vaccineappointmentrequests/{id}" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP', 'ROLE_PATIENT')" )
    public ResponseEntity updateAppointmentRequest ( @PathVariable final Long id,
            @RequestBody final VaccineAppointmentRequestForm requestF ) {
        try {
            final VaccineAppointmentRequest request = service.build( requestF );
            request.setId( id );

            if ( null != request.getId() && !id.equals( request.getId() ) ) {
                return new ResponseEntity(
                        errorResponse( "The ID provided does not match the ID of the AppointmentRequest provided" ),
                        HttpStatus.CONFLICT );
            }

            /* Patient can't look at anyone else's requests */
            final User self = userService.findByName( LoggerUtil.currentUser() );
            if ( self.getRoles().contains( Role.ROLE_PATIENT ) && !request.getPatient().equals( self ) ) {
                return new ResponseEntity( HttpStatus.UNAUTHORIZED );
            }

            final VaccineAppointmentRequest dbRequest = service.findById( id );
            if ( null == dbRequest ) {
                return new ResponseEntity( errorResponse( "No AppointmentRequest found for id " + id ),
                        HttpStatus.NOT_FOUND );
            }

            service.save( request );
            loggerUtil.log( TransactionType.APPOINTMENT_REQUEST_UPDATED, request.getPatient(), request.getHcp() );
            if ( request.getStatus().getCode() == Status.APPROVED.getCode() ) {
                loggerUtil.log( TransactionType.APPOINTMENT_REQUEST_APPROVED, request.getPatient(), request.getHcp() );
            }
            else {
                loggerUtil.log( TransactionType.APPOINTMENT_REQUEST_DENIED, request.getPatient(), request.getHcp() );
            }

            return new ResponseEntity( request, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            return new ResponseEntity(
                    errorResponse( "Could not update " + requestF.toString() + " because of " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }

    }

    /**
     * View VaccineAppointments will retrieve and display all appointments for the
     * logged-in HCP that are in "approved" status
     *
     *
     * @return The page to display for the user
     */
    @GetMapping ( BASE_PATH + "/vaccineAppointments" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP')" )
    public List<VaccineAppointmentRequest> upcomingVaccineAppointments () {
        final User hcp = userService.findByName( LoggerUtil.currentUser() );

        final List<VaccineAppointmentRequest> appointment = service.findByHcp( hcp ).stream()
                .filter( e -> e.getStatus().equals( Status.APPROVED ) ).collect( Collectors.toList() );
        /* Log the event */
        appointment.stream().map( AppointmentRequest::getPatient ).distinct().forEach( e -> loggerUtil
                .log( TransactionType.APPOINTMENT_REQUEST_VIEWED, LoggerUtil.currentUser(), e.getUsername() ) );
        return appointment;
    }

}

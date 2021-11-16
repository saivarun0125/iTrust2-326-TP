package edu.ncsu.csc.iTrust2.controllers.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import edu.ncsu.csc.iTrust2.forms.PatientForm;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;
import edu.ncsu.csc.iTrust2.models.enums.TransactionType;
import edu.ncsu.csc.iTrust2.services.CovidVaccineService;
import edu.ncsu.csc.iTrust2.services.PatientService;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.services.VaccineOfficeVisitService;
import edu.ncsu.csc.iTrust2.utils.LoggerUtil;

/**
 * Controller responsible for providing various REST API endpoints for the
 * Patient model.
 *
 * @author Kai Presler-Marshall
 *
 */
@RestController
@SuppressWarnings ( { "rawtypes", "unchecked" } )
public class APIPatientController extends APIController {

    /**
     * Patient service
     */
    @Autowired
    private PatientService            patientService;

    /** User service */
    @Autowired
    private VaccineOfficeVisitService vaccineOfficeVisitService;

    /**
     * User Service
     */
    @Autowired
    private UserService               userService;

    /**
     * Vaccine Service
     */
    @Autowired
    private CovidVaccineService       covidVaccineService;

    /**
     * LoggerUtil
     */
    @Autowired
    private LoggerUtil                loggerUtil;

    /**
     * Retrieves and returns a list of all Patients stored in the system
     *
     * @return list of patients
     */
    @GetMapping ( BASE_PATH + "/patients" )
    public List<Patient> getPatients () {
        final List<Patient> patients = patientService.findAll();
        return patients;
    }

    /**
     * If you are logged in as a patient, then you can use this convenience
     * lookup to find your own information without remembering your id. This
     * allows you the shorthand of not having to look up the id in between.
     *
     * @return The patient object for the currently authenticated user.
     */
    @GetMapping ( BASE_PATH + "/patient" )
    @PreAuthorize ( "hasRole('ROLE_PATIENT')" )
    public ResponseEntity getPatient () {
        final User self = userService.findByName( LoggerUtil.currentUser() );
        final Patient patient = (Patient) patientService.findByName( self.getUsername() );
        if ( patient == null ) {
            return new ResponseEntity( errorResponse( "Could not find a patient entry for you, " + self.getUsername() ),
                    HttpStatus.NOT_FOUND );
        }
        else {
            loggerUtil.log( TransactionType.VIEW_DEMOGRAPHICS, self );
            return new ResponseEntity( patient, HttpStatus.OK );
        }
    }

    /**
     * Retrieves and returns the Patient with the username provided
     *
     * @param username
     *            The username of the Patient to be retrieved, as stored in the
     *            Users table
     * @return response
     */
    @GetMapping ( BASE_PATH + "/patients/{username}" )
    @PreAuthorize ( "hasAnyRole('ROLE_HCP')" )
    public ResponseEntity getPatient ( @PathVariable ( "username" ) final String username ) {
        final Patient patient = (Patient) patientService.findByName( username );
        if ( patient == null ) {
            return new ResponseEntity( errorResponse( "No Patient found for username " + username ),
                    HttpStatus.NOT_FOUND );
        }
        else {
            loggerUtil.log( TransactionType.PATIENT_DEMOGRAPHICS_VIEW, LoggerUtil.currentUser(), username,
                    "HCP retrieved demographics for patient with username " + username );
            return new ResponseEntity( patient, HttpStatus.OK );
        }
    }

    /**
     * Updates the Patient with the id provided by overwriting it with the new
     * Patient record that is provided. If the ID provided does not match the ID
     * set in the Patient provided, the update will not take place
     *
     * @param id
     *            The username of the Patient to be updated
     * @param patientF
     *            The updated Patient to save
     * @return response
     */
    @PutMapping ( BASE_PATH + "/patients/{id}" )
    public ResponseEntity updatePatient ( @PathVariable final String id, @RequestBody final PatientForm patientF ) {
        // check that the user is an HCP or a patient with username equal to id
        boolean userEdit = false; // true if user edits his or her own
                                  // demographics, false if hcp edits them
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final SimpleGrantedAuthority hcp = new SimpleGrantedAuthority( "ROLE_HCP" );
        try {
            userEdit = !auth.getAuthorities().contains( hcp );
            if ( !auth.getName().equals( id ) && userEdit ) {
                return new ResponseEntity( errorResponse( "You do not have permission to edit this record" ),
                        HttpStatus.UNAUTHORIZED );
            }

        }
        catch ( final Exception e ) {
            return new ResponseEntity( HttpStatus.UNAUTHORIZED );
        }

        try {
            final Patient patient = (Patient) patientService.findByName( id );

            // Shouldn't be possible but let's check anyways
            if ( null == patient ) {
                return new ResponseEntity( errorResponse( "Patient not found" ), HttpStatus.NOT_FOUND );
            }

            patient.update( patientF );

            final User dbPatient = patientService.findByName( id );
            if ( null == dbPatient ) {
                return new ResponseEntity( errorResponse( "No Patient found for id " + id ), HttpStatus.NOT_FOUND );
            }
            patientService.save( patient );

            // Log based on whether user or hcp edited demographics
            if ( userEdit ) {
                loggerUtil.log( TransactionType.EDIT_DEMOGRAPHICS, LoggerUtil.currentUser(),
                        "User with username " + patient.getUsername() + "updated their demographics" );
            }
            else {
                loggerUtil.log( TransactionType.PATIENT_DEMOGRAPHICS_EDIT, LoggerUtil.currentUser(),
                        patient.getUsername(),
                        "HCP edited demographics for patient with username " + patient.getUsername() );
            }
            return new ResponseEntity( patient, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            return new ResponseEntity(
                    errorResponse( "Could not update " + patientF.toString() + " because of " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Get the patient's zip code to autofill Find Expert Form
     *
     * @return Response entity with status and response data (zip or error
     *         message)
     *
     */
    @GetMapping ( BASE_PATH + "/patient/findexperts/getzip" )
    @PreAuthorize ( "hasRole( 'ROLE_PATIENT')" )
    public ResponseEntity getPatientZip () {
        final String user = LoggerUtil.currentUser();
        if ( user == null ) {
            return new ResponseEntity( errorResponse( "Patient not found" ), HttpStatus.NOT_FOUND );
        }
        final String zip = ( (Patient) patientService.findByName( user ) ).getZip();
        if ( zip == null ) {
            return new ResponseEntity( errorResponse( "Patient does not have zip stored" ), HttpStatus.NO_CONTENT );
        }
        else {
            final String[] zipParts = zip.split( "-" );
            return new ResponseEntity( zipParts, HttpStatus.OK );

        }

    }

    /**
     * Retrieves a list of all VaccineOfficeVisit information by patient in the
     * database
     *
     * @return list of vaccine office visit information of a patient
     */
    @GetMapping ( BASE_PATH + "/patient/vaccinationstatus" )
    @PreAuthorize ( "hasAnyRole('ROLE_PATIENT')" )
    public ResponseEntity getVaccinationStatus () {
        final User self = userService.findByName( LoggerUtil.currentUser() );
        loggerUtil.log( TransactionType.VACCINE_OFFICE_VISIT_PATIENT_VIEW, self );
        if ( self == null ) {
            return new ResponseEntity( errorResponse( "Patient not found" ), HttpStatus.NOT_FOUND );
        }
        final List<VaccineOfficeVisit> listOfficeVisits = vaccineOfficeVisitService.findByPatient( self );

        final List<Object> list = new ArrayList<>();

        for ( int i = 0; i < listOfficeVisits.size(); i++ ) {

            list.add( listOfficeVisits.get( i ).getDate() );
            list.add( listOfficeVisits.get( i ).getVaccine() );
            list.add( listOfficeVisits.get( i ).getHcp() );
            list.add( listOfficeVisits.get( i ).getDoseNumber() );

        }
        return new ResponseEntity( list, HttpStatus.OK );

    }

    /**
     * return status
     *
     * @return vaccinatino status of patient
     */
    @GetMapping ( BASE_PATH + "/patient/generatecertificate" )
    @PreAuthorize ( "hasRole('ROLE_PATIENT')" )
    public ResponseEntity generateCertificate () {
        final User self = userService.findByName( LoggerUtil.currentUser() );
        boolean fullyVaccinated = false;
        if ( self == null ) {
            return new ResponseEntity( errorResponse( "Patient not found" ), HttpStatus.NOT_FOUND );
        }

        final Patient p = (Patient) self;
        final List<VaccineOfficeVisit> listOfficeVisits = vaccineOfficeVisitService.findByPatient( self );

        final String path = "certificates/" + self.getId() + "_vax_cert.pdf";
        final File f = new File( path );
        f.getParentFile().mkdirs();

        try {
            final PdfWriter writer = new PdfWriter( path );
            final PdfDocument pdf = new PdfDocument( writer );
            final Document document = new Document( pdf );
            final Paragraph para = new Paragraph( "Vaccination Record\n" )
                    .add( "Patient: " + p.getFirstName() + " " + p.getLastName() );
            if ( ( listOfficeVisits.size() == 2 ) && ( listOfficeVisits.get( 0 ).getVaccine().getNumDoses() == 2 ) ) {
                fullyVaccinated = true;
            }
            else if ( ( listOfficeVisits.size() == 1 )
                    && ( listOfficeVisits.get( 0 ).getVaccine().getNumDoses() == 1 ) ) {
                fullyVaccinated = true;
            }
            para.add( "\nFully Vaccinated: " + fullyVaccinated + "" ).add( "\n" );

            if ( null != listOfficeVisits.get( 0 ) ) {
                para.add( "Vaccine Received: " + covidVaccineService.findById( p.getVaccineId() ).getName() );
                para.add( "First Dose: " + listOfficeVisits.get( 0 ).getDate().toString() + "\n" );
                para.add( "Administered by: " + listOfficeVisits.get( 0 ).getHcp().toString() + "\n" );
            }
            if ( null != listOfficeVisits.get( 1 ) ) {
                para.add( "Second Dose: " + listOfficeVisits.get( 1 ).toString() );
                para.add( "Administered by: " + listOfficeVisits.get( 1 ).getHcp().toString() + "\n" );
            }
            document.add( para );
            document.close();
        }
        catch ( final FileNotFoundException e ) {
            return new ResponseEntity( e.getMessage(), HttpStatus.EXPECTATION_FAILED );
        }

        return new ResponseEntity( path, HttpStatus.OK );
    }
}

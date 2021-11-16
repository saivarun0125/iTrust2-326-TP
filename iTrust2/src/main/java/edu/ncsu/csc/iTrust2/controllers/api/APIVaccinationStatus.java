package edu.ncsu.csc.iTrust2.controllers.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;
import edu.ncsu.csc.iTrust2.models.enums.TransactionType;
import edu.ncsu.csc.iTrust2.services.CovidVaccineService;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.services.VaccineOfficeVisitService;
import edu.ncsu.csc.iTrust2.utils.LoggerUtil;

/**
 * Controller responsible for providing various REST API endpoints for the
 * Vaccination status model.
 *
 * @author Kai Presler-Marshall
 *
 */
@RestController
@SuppressWarnings ( { "rawtypes", "unchecked" } )
public class APIVaccinationStatus extends APIController {

    /** LoggerUtil */
    @Autowired
    private LoggerUtil                loggerUtil;

    /** User service */
    @Autowired
    private UserService<User>         userService;

    /** User service */
    @Autowired
    private VaccineOfficeVisitService vaccineOfficeVisitService;

    /**
     * Vaccine Service
     */
    @Autowired
    private CovidVaccineService       covidVaccineService;

    /**
     * Retrieves a list of all VaccineOfficeVisit information by patient in the
     * database
     *
     * @return list of vaccine office visit information of a patient
     */
    @GetMapping ( BASE_PATH + "patient/vaccinationstatus" )
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
    @GetMapping ( BASE_PATH + "/vaccinationstatus/generatecertificate" )
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

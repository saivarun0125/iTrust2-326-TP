package edu.ncsu.csc.iTrust2.controllers.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Paragraph;

import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.Personnel;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;
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

    // testing todo:
    // invalid tests
    // mix and match tests

    /**
     * return status
     *
     * @return vaccinatino status of patient
     */
    @GetMapping ( BASE_PATH + "/vaccinationstatus/test" )
    @PreAuthorize ( "hasRole('ROLE_PATIENT')" )
    public ResponseEntity generateCertificateLocally () {
        final User self = userService.findByName( LoggerUtil.currentUser() );
        final boolean fullyVaccinated = false;
        if ( self == null ) {
            return new ResponseEntity( errorResponse( "Patient not found" ), HttpStatus.NOT_FOUND );
        }

        final Patient p = (Patient) self;
        final List<VaccineOfficeVisit> listOfficeVisits = vaccineOfficeVisitService.findByPatient( self );

        final Map<String, Integer> doseAmounts = new HashMap<String, Integer>();

        for ( final VaccineOfficeVisit visit : listOfficeVisits ) {
            // if the current visit's vaccine isn't in the map, add it and set
            // it's initial dose to 1
            if ( !doseAmounts.containsKey( visit.getVaccine().getCode() ) ) {
                doseAmounts.put( visit.getVaccine().getCode(), 1 );
            }
            else {
                // if the current' visit's vaccine is in the map, increment the
                // dose count
                doseAmounts.put( visit.getVaccine().getCode(), doseAmounts.get( visit.getVaccine().getCode() ) + 1 );
            }
        }

        boolean isFullyVaccinated = false;

        // check for the mix and match string in any of the office visits
        final String mixAndMatchString = " This vaccine dose now certifies " + self
                + " as fully vaccinated as per the CDC and FDA's guidelines on interchangeably dosing with Covid19 vaccines.";
        for ( final VaccineOfficeVisit visit : listOfficeVisits ) {
            if ( visit.getNotes() != null && visit.getNotes().contains( mixAndMatchString ) ) {
                isFullyVaccinated = true;
            }
        }

        // check for current dosage == vaccine dose number
        for ( final VaccineOfficeVisit visit : listOfficeVisits ) {
            if ( visit.getVaccine() != null
                    && visit.getVaccine().getNumDoses() == doseAmounts.get( visit.getVaccine().getCode() ) ) {
                isFullyVaccinated = true;
            }
        }

        final String path = "certificates/" + self.getId() + "_vax_cert.pdf";
        final File f = new File( path );
        f.getParentFile().mkdirs();

        try {
            final PdfWriter writer = new PdfWriter( path );
            final PdfDocument pdf = new PdfDocument( writer );
            final Document document = new Document( pdf );

            final Style style = new Style();
            style.setBold();

            // header style for the title
            final Style headerStyle = new Style();
            headerStyle.setBold();
            headerStyle.setFontSize( 30 );

            // sub header style for the subheader that has the patients first
            // and last name
            final Style subHeaderStyle = new Style();
            subHeaderStyle.setUnderline();
            subHeaderStyle.setFontSize( 15 );

            // style for the body
            final Style bodyStyle = new Style();
            bodyStyle.setFontSize( 10 );

            // create the header and add style
            final Paragraph header = new Paragraph( "Vaccination Record\n" );
            header.addStyle( headerStyle );

            // create the subheader that has the patient's name and set style
            final Paragraph subHeader = new Paragraph()
                    .add( "Patient: " + p.getFirstName() + " " + p.getLastName() + "\n" );
            subHeader.addStyle( subHeaderStyle );

            // create the body of the pdf
            final Paragraph body = new Paragraph();

            final Paragraph vaccinationStatus = new Paragraph();
            // set up red color
            final Style redText = new Style();
            final Color red = new DeviceRgb( 171, 55, 55 );
            redText.setFontColor( red );
            redText.setBold();
            // set up green color
            final Style greenText = new Style();
            final Color green = new DeviceRgb( 56, 168, 86 );
            greenText.setFontColor( green );
            greenText.setBold();
            // lets the user know if they are fully vaccinated
            if ( isFullyVaccinated ) {
                vaccinationStatus.add( p.getFirstName() + " " + p.getLastName() + " is fully vaccinated.\n" );
                vaccinationStatus.addStyle( greenText );
            }
            else {
                vaccinationStatus.add( p.getFirstName() + " " + p.getLastName() + " is NOT fully vaccinated.\n" );
                vaccinationStatus.addStyle( redText );
            }

            document.add( header );
            document.add( subHeader );
            document.add( vaccinationStatus );

            // list out the all the doses that the patient has recieved
            for ( final VaccineOfficeVisit visit : listOfficeVisits ) {
                body.add( "______________________________________________________________________\n" );
                body.add( visit.getVaccine().getName() + ": \n" );
                body.add( "Dose number: " + visit.getDoseNumber() + "\n" );
                final DateTimeFormatter format = DateTimeFormatter.ofPattern( "MM/dd/yyyy" );
                body.add( "Date: " + visit.getDate().format( format ) + "\n" );
                body.add( "Hospital: " + visit.getHospital() + "\n" );
                final Personnel hcpPers = (Personnel) visit.getHcp();
                body.add( "Vaccinator: " + hcpPers.getFirstName() + " " + hcpPers.getLastName() + "\n" );
            }

            body.add( "______________________________________________________________________\n" );

            document.add( body );
            document.close();
        }
        catch ( final FileNotFoundException e ) {
            return new ResponseEntity( e.getMessage(), HttpStatus.EXPECTATION_FAILED );
        }

        return new ResponseEntity( path, HttpStatus.OK );
    }
    
    /**
     * Endpoint for determining if the user has been vaccinated at
     * least once - used for enabling or disabling the ability of
     * the user to generate the PDF
     * 
     * @return ResponseEntity with error if the user was not in the system,
     * 		   an entity with true if the user has been vaccinated at
     * 		   least once, and false if the user has not been vaccinated
     */
    @PostMapping( value = BASE_PATH + "/vaccinationstatus" )
    @PreAuthorize( "hasRole('ROLE_PATIENT')" )
    public ResponseEntity<Boolean> userHasBeenVaccinatedOnce() {
    	final User self = userService.findByName( LoggerUtil.currentUser() );
        if ( self == null ) {
            return new ResponseEntity( errorResponse( "Patient not found" ), HttpStatus.NOT_FOUND );
        }
        final List<VaccineOfficeVisit> listOfficeVisits = vaccineOfficeVisitService.findByPatient( self );
        
        return new ResponseEntity( !listOfficeVisits.isEmpty(), HttpStatus.OK );
    }

    /**
     * return status
     *
     * @return vaccinatino status of patient
     */
    @GetMapping ( value = BASE_PATH + "/vaccinationstatus", produces = MediaType.APPLICATION_PDF_VALUE )
    @PreAuthorize ( "hasRole('ROLE_PATIENT')" )
    public ResponseEntity<ByteArrayResource> generateCertificate () {
        final User self = userService.findByName( LoggerUtil.currentUser() );
        final boolean fullyVaccinated = false;
        if ( self == null ) {
            return new ResponseEntity( errorResponse( "Patient not found" ), HttpStatus.NOT_FOUND );
        }

        final Patient p = (Patient) self;
        final List<VaccineOfficeVisit> listOfficeVisits = vaccineOfficeVisitService.findByPatient( self );

        final Map<String, Integer> doseAmounts = new HashMap<String, Integer>();

        for ( final VaccineOfficeVisit visit : listOfficeVisits ) {
            // if the current visit's vaccine isn't in the map, add it and set
            // it's initial dose to 1
            if ( !doseAmounts.containsKey( visit.getVaccine().getCode() ) ) {
                doseAmounts.put( visit.getVaccine().getCode(), 1 );
            }
            else {
                // if the current' visit's vaccine is in the map, increment the
                // dose count
                doseAmounts.put( visit.getVaccine().getCode(), doseAmounts.get( visit.getVaccine().getCode() ) + 1 );
            }
        }

        boolean isFullyVaccinated = false;
        boolean isMixAndMatch = false;

        // check for the mix and match string in any of the office visits
        final String mixAndMatchString = " This vaccine dose now certifies " + self.getUsername()
                + " as fully vaccinated as per the CDC and FDA's guidelines on interchangeably dosing with Covid19 vaccines.";
        for ( final VaccineOfficeVisit visit : listOfficeVisits ) {
            if ( visit.getNotes() != null && visit.getNotes().contains( mixAndMatchString ) ) {
                isFullyVaccinated = true;
                isMixAndMatch = true;
            }
        }

        // check for current dosage == vaccine dose number
        for ( final VaccineOfficeVisit visit : listOfficeVisits ) {
            if ( visit.getVaccine() != null
                    && visit.getVaccine().getNumDoses() == doseAmounts.get( visit.getVaccine().getCode() ) ) {
                isFullyVaccinated = true;
            }
        }

        final String path = "certificates/" + self.getId() + "_vax_cert.pdf";
        final File f = new File( path );
        f.getParentFile().mkdirs();

        ResponseEntity<ByteArrayResource> response;

        try {
            final PdfWriter writer = new PdfWriter( path );
            final PdfDocument pdf = new PdfDocument( writer );
            final Document document = new Document( pdf );

            final Style style = new Style();
            style.setBold();

            // header style for the title
            final Style headerStyle = new Style();
            headerStyle.setBold();
            headerStyle.setFontSize( 30 );

            // sub header style for the subheader that has the patients first
            // and last name
            final Style subHeaderStyle = new Style();
            subHeaderStyle.setUnderline();
            subHeaderStyle.setFontSize( 15 );

            // style for the body
            final Style bodyStyle = new Style();
            bodyStyle.setFontSize( 10 );

            // create the header and add style
            final Paragraph header = new Paragraph( "Vaccination Record\n" );
            header.addStyle( headerStyle );

            // create the subheader that has the patient's name and set style
            final Paragraph subHeader = new Paragraph()
                    .add( "Patient: " + p.getFirstName() + " " + p.getLastName() + "\n" );
            subHeader.addStyle( subHeaderStyle );

            // create the body of the pdf
            final Paragraph body = new Paragraph();

            final Paragraph vaccinationStatus = new Paragraph();
            // set up red color
            final Style redText = new Style();
            final Color red = new DeviceRgb( 171, 55, 55 );
            redText.setFontColor( red );
            redText.setBold();
            // set up green color
            final Style greenText = new Style();
            final Color green = new DeviceRgb( 56, 168, 86 );
            greenText.setFontColor( green );
            greenText.setBold();
            // lets the user know if they are fully vaccinated
            if ( isFullyVaccinated && isMixAndMatch ) {
                vaccinationStatus.add( p.getFirstName() + " " + p.getLastName()
                        + " is fully vaccinated as per the FDA and CDC's guidelines on interchangeably dosing with Covid19 vaccinations.\n" );
                vaccinationStatus.addStyle( greenText );
            }
            else if ( isFullyVaccinated ) {
                vaccinationStatus.add( p.getFirstName() + " " + p.getLastName() + " is fully vaccinated.\n" );
                vaccinationStatus.addStyle( greenText );
            }
            else {
                vaccinationStatus.add( p.getFirstName() + " " + p.getLastName() + " is NOT fully vaccinated.\n" );
                vaccinationStatus.addStyle( redText );
            }

            document.add( header );
            document.add( subHeader );
            document.add( vaccinationStatus );

            // list out the all the doses that the patient has recieved
            for ( final VaccineOfficeVisit visit : listOfficeVisits ) {
                body.add( "______________________________________________________________________\n" );
                body.add( visit.getVaccine().getName() + ": \n" );
                body.add( "Dose number: " + visit.getDoseNumber() + "\n" );
                final DateTimeFormatter format = DateTimeFormatter.ofPattern( "MM/dd/yyyy" );
                body.add( "Date: " + visit.getDate().format( format ) + "\n" );
                body.add( "Hospital: " + visit.getHospital() + "\n" );
                final Personnel hcpPers = (Personnel) visit.getHcp();
                body.add( "Vaccinator: " + hcpPers.getFirstName() + " " + hcpPers.getLastName() + "\n" );
            }

            body.add( "______________________________________________________________________\n" );

            document.add( body );
            document.close();

            final Path pdfLocation = Paths.get( path );
            byte[] pdfBytes;
            try {
                pdfBytes = Files.readAllBytes( pdfLocation );
            }
            catch ( final IOException e ) {
                return new ResponseEntity( e.getMessage(), HttpStatus.EXPECTATION_FAILED );
            }

            final ByteArrayResource resource = new ByteArrayResource( pdfBytes );

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType( MediaType.APPLICATION_PDF );
            // Here you have to set the actual filename of your pdf
            final String filename = self.getId() + "_vax_cert.pdf";
            headers.add( HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + filename );

            response = new ResponseEntity<ByteArrayResource>( resource, headers, HttpStatus.OK );
        }
        catch ( final FileNotFoundException e ) {
            return new ResponseEntity( e.getMessage(), HttpStatus.EXPECTATION_FAILED );
        }

        return response;
    }

}

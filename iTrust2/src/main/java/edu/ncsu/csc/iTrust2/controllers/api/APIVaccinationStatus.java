package edu.ncsu.csc.iTrust2.controllers.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;
import edu.ncsu.csc.iTrust2.models.enums.TransactionType;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.services.VaccineOfficeVisitService;
import edu.ncsu.csc.iTrust2.utils.LoggerUtil;

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
     * Retrieves a list of all VaccineOfficeVisit information by patient in the
     * database
     *
     * @return list of vaccine office visit information of a patient
     */
    @GetMapping ( BASE_PATH + "/vaccinationstatus" )
    @PreAuthorize ( "hasAnyRole('ROLE_PATIENT')" )
    public List<Object> getVaccinationStatus () {
        final User self = userService.findByName( LoggerUtil.currentUser() );
        loggerUtil.log( TransactionType.VACCINE_OFFICE_VISIT_PATIENT_VIEW, self );
        final List<VaccineOfficeVisit> listOfficeVisits = vaccineOfficeVisitService.findByPatient( self );

        final List<Object> list = new ArrayList<>();

        list.add( self );

        for ( int i = 0; i < listOfficeVisits.size(); i++ ) {

            list.add( listOfficeVisits.get( i ).getHcp() );
            list.add( listOfficeVisits.get( i ).getDate() );
            list.add( listOfficeVisits.get( i ).getVaccine() );
            list.add( listOfficeVisits.get( i ).getDoseNumber() );
        }
        return list;

    }

}

package edu.ncsu.csc.iTrust2.forms;

import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.VaccineAppointmentRequest;
import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;

/**
 *
 * @author Kon Buchanan
 *
 */
public class VaccineOfficeVisitForm extends OfficeVisitForm {

    /** */
    private CovidVaccine              vaccine;
    /** */
    private Integer                   doseNumber;
    /** */
    private boolean                   scheduled;
    /** */
    private VaccineAppointmentRequest appointment;

    /**
     * Don't use this one. For Hibernate/Thymeleaf
     */
    public VaccineOfficeVisitForm () {

    }

    /**
     *
     * @param ov
     */
    public VaccineOfficeVisitForm ( final VaccineOfficeVisit ov ) {
        super( ov );
        setPatient( ov.getPatient().toString() );
        setHcp( ov.getHcp().toString() );
        setDate( ov.getDate().toString() );
        setNotes( ov.getNotes() );
        setId( ov.getId().toString() );
        setScheduled( ov.isScheduled() );
        setVaccine( ov.getVaccine() );
        setDoseNumber( ov.getDoseNumber() );
        setAppointment( ov.getAppointment() );
    }

    /**
     *
     * @param appointment
     */
    public void setAppointment ( final VaccineAppointmentRequest appointment ) {
        this.appointment = appointment;
    }

    /**
     *
     * @param vaccine
     */
    public void setVaccine ( final CovidVaccine vaccine ) {
        this.vaccine = vaccine;
    }

    /**
     *
     * @return
     */
    public CovidVaccine getVaccine () {
        return vaccine;
    }

    /**
     *
     * @param dose
     */
    public void setDoseNumber ( final Integer dose ) {
        this.doseNumber = dose;
    }

    /**
     *
     * @return
     */
    public Integer getDoseNumber () {
        return doseNumber;
    }

    /**
     *
     * @param scheduled
     */
    public void setScheduled ( final boolean scheduled ) {
        this.scheduled = scheduled;
    }

    /**
     *
     * @return
     */
    public boolean isScheduled () {
        return scheduled;
    }

}

package edu.ncsu.csc.iTrust2.forms;

import javax.validation.constraints.NotEmpty;

import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;

/**
 * Vaccine Office Visit form used to document a Vaccine Office Visit by the HCP.
 * This will be validated and converted to a VaccineOfficeVisit to be stored in
 * the database.
 *
 * @author Kon Buchanan
 */
public class VaccineOfficeVisitForm extends OfficeVisitForm {
    /**
     * Serial Version of the Form. For the Serializable
     */
    private static final long serialVersionUID = 1L;

    /** The vaccine field for the vaccine office visit */
    private String            vaccine;

    /** Which dose of the vaccine this is */
    @NotEmpty
    private Integer           doseNumber;

    /** Whether or not this office visit is scheduled */
    private boolean           scheduled;

    /** The appointment request connected to this office visit */
    private Long              appointment;

    /**
     * Don't use this one. For Hibernate/Thymeleaf
     */
    public VaccineOfficeVisitForm () {

    }

    /**
     * Creates a VaccineOfficeVisitForm from the provided VaccineOfficeVisit
     *
     * @param ov
     *            - the vaccine office visit provided
     */
    public VaccineOfficeVisitForm ( final VaccineOfficeVisit ov ) {
        super( ov );
        setScheduled( ov.isScheduled() );
        setVaccine( ov.getVaccine().getCode() );
        setDoseNumber( ov.getDoseNumber() );
        setAppointment( ov.getAppointment().getId() );
    }

    /**
     * Sets the appointment for the VaccineOfficeVisitForm.
     *
     * @param appointment
     *            - the appointment to set
     */
    public void setAppointment ( final Long appointment ) {
        this.appointment = appointment;
    }

    /**
     * Retrieves the appointment associated with the VaccineOfficeVisitForm
     *
     * @return the associated appointment
     */
    public Long getAppointment () {
        return appointment;
    }

    /**
     * Sets the vaccine for the VaccineOfficeVisitForm.
     *
     * @param vaccine
     *            - the vaccine to set
     */
    public void setVaccine ( final String vaccine ) {
        this.vaccine = vaccine;
    }

    /**
     * Retrieves the vaccine associated with the VaccineOfficeVisitForm
     *
     * @return the associated vaccine
     */
    public String getVaccine () {
        return vaccine;
    }

    /**
     * Sets the dose number for the VaccineOfficeVisitForm.
     *
     * @param dose
     *            - the dose number to set
     */
    public void setDoseNumber ( final Integer dose ) {
        this.doseNumber = dose;
    }

    /**
     * Retrieves the dose number associated with the VaccineOfficeVisitForm
     *
     * @return the associated dose number
     */
    public Integer getDoseNumber () {
        return doseNumber;
    }

    /**
     * Sets if the office visit is scheduled or not
     *
     * @param scheduled
     *            - true or false, depending on if it is scheduled or not
     */
    public void setScheduled ( final boolean scheduled ) {
        this.scheduled = scheduled;
    }

    /**
     * Checks if the office visit is scheduled or not
     *
     * @return true if scheduled, false otherwise
     */
    public boolean isScheduled () {
        return scheduled;
    }

}

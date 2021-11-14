package edu.ncsu.csc.iTrust2.models;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import edu.ncsu.csc.iTrust2.models.enums.AppointmentType;
import edu.ncsu.csc.iTrust2.utils.ValidationUtil;

/**
 * Creates an office visit that is specific to vaccinations
 *
 * @author Kon Buchanan
 *
 */
@Entity
public class VaccineOfficeVisit extends OfficeVisit {

    /**
     * For Hibernate
     */
    public VaccineOfficeVisit () {

    }

    /** Covid Vaccine that is requested */
    @NotNull
    @ManyToOne
    @JoinColumn ( name = "vaccine_id" )
    private CovidVaccine              vaccine;

    /** The dose number. */
    private Integer                   doseNumber;

    /** True if scheduled; false otherwise. */
    private boolean                   scheduled;

    /** The appointment request attached to the office visit */
    private VaccineAppointmentRequest appointment;

    /**
     * Sets the appointment for the VaccineOfficeVisit.
     *
     * @param appointment
     *            - the appointment to set
     */
    public void setAppointment ( final VaccineAppointmentRequest appointment ) {
        this.appointment = appointment;
    }

    /**
     * Retrieves the appointment associated with the VaccineOfficeVisit
     *
     * @return the associated appointment
     */
    @Override
    public VaccineAppointmentRequest getAppointment () {
        return appointment;
    }

    /**
     * Sets the vaccine for the VaccineOfficeVisit.
     *
     * @param vaccine
     *            - the vaccine to set
     */
    public void setVaccine ( final CovidVaccine vaccine ) {
        this.vaccine = vaccine;
    }

    /**
     * Retrieves the vaccine associated with the VaccineOfficeVisit
     *
     * @return the associated vaccine
     */
    public CovidVaccine getVaccine () {
        return vaccine;
    }

    /**
     * Sets the dose number for the VaccineOfficeVisit.
     *
     * @param dose
     *            - the dose number to set
     */
    public void setDoseNumber ( final Integer dose ) {
        if ( dose == null ) {
            throw new IllegalArgumentException( "Dose number must be filled." );
        }
        this.doseNumber = dose;
    }

    /**
     * Retrieves the dose number associated with the VaccineOfficeVisit
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

    /**
     * Validates the fields of a vaccine
     */
    public void validateVaccine () {
        if ( this.vaccine == null ) {
            throw new IllegalArgumentException( "Vaccine must be entered" );
        }

        if ( this.vaccine.getAgeRange().size() != 2 || this.vaccine.getAgeRange() == null ) {
            throw new IllegalArgumentException( "The age range must consist of two whole numbers." );
        }

        if ( this.vaccine.getDoseInterval() == null ) {
            if ( this.vaccine.getNumDoses() != 1 ) {
                throw new IllegalArgumentException( "There must be a proper dose interval." );
            }
        }

        if ( this.vaccine.getNumDoses() < 1 ) {
            throw new IllegalArgumentException( "There must be a positive number of doses." );
        }

        if ( this.getType() != AppointmentType.VACCINE_APPOINTMENT ) {
            throw new IllegalArgumentException( "Vaccines can only be entered into vacicnation appointments." );
        }

        ValidationUtil.validate( vaccine );
    }

    /**
     * Validates the dose number for the office visit
     */
    public void validateDoseNumber () {
        if ( this.doseNumber < 1 ) {
            throw new IllegalArgumentException( "There must be a positive number of doses." );
        }

        ValidationUtil.validate( doseNumber );
    }

}

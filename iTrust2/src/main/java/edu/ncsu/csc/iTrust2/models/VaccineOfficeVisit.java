package edu.ncsu.csc.iTrust2.models;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
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

    private VaccineAppointmentRequest appointment;

    /**
     *
     * @param appointment
     */
    public void setAppointment ( final VaccineAppointmentRequest appointment ) {
        this.appointment = appointment;
    }

    /**
     *
     */
    @Override
    public VaccineAppointmentRequest getAppointment () {
        return appointment;
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
        if ( dose == null ) {
            throw new IllegalArgumentException( "Dose number must be filled." );
        }
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

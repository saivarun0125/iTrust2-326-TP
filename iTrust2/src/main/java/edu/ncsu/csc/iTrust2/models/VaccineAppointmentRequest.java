package edu.ncsu.csc.iTrust2.models;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * Backing object for the Covid Vaccine Appointment Request system. This is the
 * object that is actually stored in the database and reflects the persistent
 * information we have on the Covid vaccine appointment request.
 *
 * @author Sai Maale
 *
 */
@Entity
public class VaccineAppointmentRequest extends AppointmentRequest {

    /**
     * For Hibernate
     */
    public VaccineAppointmentRequest () {

    }

    /**
     * Covid Vaccine that is requested
     */
    @NotNull
    @ManyToOne
    @JoinColumn ( name = "vaccine_id" )
    private CovidVaccine vaccine;

    private Patient      patient;

    /**
     * Retrieves the requested covid vaccination
     *
     * @return covid vaccine being requested
     */
    public CovidVaccine getVaccine () {
        return vaccine;
    }

    /**
     * Sets the covid vaccine for the appointment to the passed in covid vaccine
     *
     * @param vaccine
     *            that is set
     */
    public void setVaccine ( final CovidVaccine vaccine ) {
        this.vaccine = vaccine;
    }

    private void validAgeRange () {

    }

}

package edu.ncsu.csc.iTrust2.forms;

import javax.validation.constraints.NotNull;

import edu.ncsu.csc.iTrust2.models.VaccineAppointmentRequest;

/**
 * Form to create a Covid vaccine appointment for a user
 *
 * @author Sai Maale
 *
 */
public class VaccineAppointmentRequestForm extends AppointmentRequestForm {

    /** The covid vaccine of the appt request */
    @NotNull ( message = "Invalid Vaccine" )
    private String covidVaccine;

    /**
     * Don't use this one. For Hibernate/Thymeleaf
     */
    public VaccineAppointmentRequestForm () {
    }

    /**
     * Constructor for Vaccine Appointment Request. Uses the Vaccine Appointment
     * Request object
     *
     * @param request
     *            covid vaccine appointment request to populate form
     *
     */
    public VaccineAppointmentRequestForm ( final VaccineAppointmentRequest request ) {
        super( request );
        setVaccine( request.getVaccine().getName() );
    }

    /**
     * gets the covid vaccine requested
     *
     * @return CovidVaccine
     */
    public String getVaccine () {
        return covidVaccine;
    }

    /**
     * Sets Covid Vaccine
     *
     * @param vaccine
     *            that is set
     *
     */
    public void setVaccine ( final String vaccine ) {
        this.covidVaccine = vaccine;
    }

}

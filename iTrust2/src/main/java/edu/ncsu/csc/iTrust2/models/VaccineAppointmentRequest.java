package edu.ncsu.csc.iTrust2.models;

import java.time.LocalDate;
import java.time.Period;

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
        System.out.println( "validate age" );
        System.out.println( vaccine );
        System.out.println( super.getPatient() );
        System.out.println( validAgeRange( vaccine ) );

        if ( validAgeRange( vaccine ) ) {
            this.vaccine = vaccine;
        }
        else {
            throw new IllegalArgumentException( "User does not meet age requirement for vaccine" );
        }

    }

    private boolean validAgeRange ( final CovidVaccine vaccine ) {
        final User user = super.getPatient();
        final Patient patient = (Patient) super.getPatient();
        final LocalDate dateOfBirthDate = patient.getDateOfBirth();
        final LocalDate dateOfAppointment = super.getDate().toLocalDate();

        final Period period = Period.between( dateOfBirthDate, dateOfAppointment );

        if ( period.getYears() < vaccine.getAgeRange().get( 0 )
                || period.getYears() > vaccine.getAgeRange().get( 1 ) ) {
            return false;
        }
        else {
            return true;
        }

    }

}

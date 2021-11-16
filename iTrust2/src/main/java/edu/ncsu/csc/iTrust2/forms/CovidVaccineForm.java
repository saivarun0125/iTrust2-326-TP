package edu.ncsu.csc.iTrust2.forms;

import java.util.List;

import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.DoseInterval;

/**
 * Form used for frontend data entry
 *
 * @author Jack Randle
 *
 */
public class CovidVaccineForm extends DrugForm {
    /**
     * number of doses for the vaccine
     */
    private short         numDoses;

    /**
     * number of doses required for the vaccine
     */
    private DoseInterval  doseInterval;

    /**
     * age range for the vaccine
     */
    private List<Integer> ageRange;

    /** true if the vaccine is available */
    private boolean       available;

    /**
     * Empty constructor for filling in fields without a CovidVaccine object.
     */
    public CovidVaccineForm () {
    }

    /**
     * Constructs a new form with information from the given drug.
     *
     * @param vaccine
     *            the drug object
     */
    public CovidVaccineForm ( final CovidVaccine vaccine ) {
        setId( vaccine.getId() );
        setName( vaccine.getName() );
        setCode( vaccine.getCode() );
        setDescription( vaccine.getDescription() );
        setNumDoses( vaccine.getNumDoses() );
        setDoseInterval( vaccine.getDoseInterval() );
        setAgeRange( vaccine.getAgeRange() );
    }

    /**
     * returns if vaccine is available
     *
     * @return the available
     */
    public boolean getAvailable () {
        return available;
    }

    /**
     * sets if the vaccine is available
     *
     * @param available
     *            the available to set
     */
    public void setAvailable ( final boolean available ) {
        this.available = available;
    }

    /**
     * return the number of doses
     *
     * @return the numDoses
     */
    public short getNumDoses () {
        return numDoses;
    }

    /**
     * set the number of doses
     *
     * @param numDoses
     *            the numDoses to set
     */
    public void setNumDoses ( final short numDoses ) {
        this.numDoses = numDoses;
    }

    /**
     * return the dose interval
     *
     * @return the doseInterval
     */
    public DoseInterval getDoseInterval () {
        return doseInterval;
    }

    /**
     * set the dose interval
     *
     * @param doseInterval
     *            the doseInterval to set
     */
    public void setDoseInterval ( final DoseInterval doseInterval ) {
        this.doseInterval = doseInterval;
    }

    /**
     * get the age range
     *
     * @return the ageRange
     */
    public List<Integer> getAgeRange () {
        return ageRange;
    }

    /**
     * set the age range
     *
     * @param ageRange
     *            the ageRange to set
     */
    public void setAgeRange ( final List<Integer> ageRange ) {
        this.ageRange = ageRange;
    }

}

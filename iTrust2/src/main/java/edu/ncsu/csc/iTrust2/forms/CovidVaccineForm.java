package edu.ncsu.csc.iTrust2.forms;

import java.util.List;

import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.models.DoseInterval;

public class CovidVaccineForm extends DrugForm {
    private short         numDoses;

    private DoseInterval  doseInterval;

    private List<Integer> ageRange;

    /**
     * Empty constructor for filling in fields without a CovidVaccine object.
     */
    public CovidVaccineForm () {
    }

    /**
     * Constructs a new form with information from the given drug.
     *
     * @param drug
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
     * @return the numDoses
     */
    public short getNumDoses () {
        return numDoses;
    }

    /**
     * @param numDoses
     *            the numDoses to set
     */
    public void setNumDoses ( final short numDoses ) {
        this.numDoses = numDoses;
    }

    /**
     * @return the doseInterval
     */
    public DoseInterval getDoseInterval () {
        return doseInterval;
    }

    /**
     * @param doseInterval
     *            the doseInterval to set
     */
    public void setDoseInterval ( final DoseInterval doseInterval ) {
        this.doseInterval = doseInterval;
    }

    /**
     * @return the ageRange
     */
    public List<Integer> getAgeRange () {
        return ageRange;
    }

    /**
     * @param ageRange
     *            the ageRange to set
     */
    public void setAgeRange ( final List<Integer> ageRange ) {
        this.ageRange = ageRange;
    }

}

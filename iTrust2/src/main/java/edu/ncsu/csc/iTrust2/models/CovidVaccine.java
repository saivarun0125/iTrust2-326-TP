package edu.ncsu.csc.iTrust2.models;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import edu.ncsu.csc.iTrust2.forms.CovidVaccineForm;

@Entity
public class CovidVaccine extends Drug {

    @Min ( 0 )
    private short         numDoses;

    @NotNull
    @Embedded
    private DoseInterval  doseInterval;

    @ElementCollection
    private List<Integer> ageRange;

    public CovidVaccine () {

    }

    public CovidVaccine ( final CovidVaccineForm cf ) {
        setCode( cf.getCode() );
        setDescription( cf.getDescription() );
        setName( cf.getName() );
        setNumDoses( cf.getNumDoses() );
        setDoseInterval( cf.getDoseInterval() );
        setAgeRange( cf.getAgeRange() );
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

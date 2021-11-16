package edu.ncsu.csc.iTrust2.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.validation.constraints.Min;

import edu.ncsu.csc.iTrust2.forms.CovidVaccineForm;

/**
 * Object that stores state and behavior for a vaccine
 *
 * @author Jack
 *
 */
@Entity
public class CovidVaccine extends Drug {

    /**
     * number of doses for the vaccine
     */
    @Min ( 0 )
    private short         numDoses;

    /**
     * dose interval for the vaccine
     */
    @Embedded
    private DoseInterval  doseInterval;

    /**
     * age range for the vaccine
     */
    @ElementCollection
    private List<Integer> ageRange;

    /**
     * default constructor for hibernate
     */
    public CovidVaccine () {

    }

    /**
     * construct covid vaccine from a covid vaccine form
     *
     * @param cf
     *            covid vaccine form to construct from
     */
    public CovidVaccine ( final CovidVaccineForm cf ) {
        setCode( cf.getCode() );
        setDescription( cf.getDescription() );
        setName( cf.getName() );
        setNumDoses( cf.getNumDoses() );
        setDoseInterval( cf.getDoseInterval() );
        setAgeRange( cf.getAgeRange() );
    }

    /**
     * Constructor that builds a covid vaccine with
     *
     * @param code
     *            code to set
     * @param desc
     *            desc to set
     * @param name
     *            name to set
     * @param numDoses
     *            num doses to set
     * @param doseInterval
     *            interval to set
     * @param ageRangeBottom
     *            range bottom to set
     * @param ageRangeTop
     *            range top to set
     */
    public CovidVaccine ( final String code, final String desc, final String name, final short numDoses,
            final DoseInterval doseInterval, final int ageRangeBottom, final int ageRangeTop ) {
        setCode( code );
        setDescription( desc );
        setName( name );
        setNumDoses( numDoses );
        setDoseInterval( doseInterval );
        this.ageRange = new ArrayList<Integer>();
        this.ageRange.add( 0, ageRangeBottom );
        this.ageRange.add( 1, ageRangeTop );
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
     * get the dose interval
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

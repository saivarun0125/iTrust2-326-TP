package edu.ncsu.csc.iTrust2.models;

import javax.persistence.Embeddable;

@Embeddable
public class VaccineDose {
    private String code;

    private int    doseNumber;

    private short  numDoses;

    public VaccineDose () {

    }

    public VaccineDose ( String code, int doseNumber, short numDoses ) {
        this.setCode( code );
        this.setDoseNumber( doseNumber );
        this.setNumDoses( numDoses );
    }

    public String getCode () {
        return code;
    }

    public int getDoseNumber () {
        return doseNumber;
    }

    public void setDoseNumber ( int doseNumber ) {
        this.doseNumber = doseNumber;
    }

    public void setCode ( String code ) {
        this.code = code;
    }

    public short getNumDoses () {
        return numDoses;
    }

    public void setNumDoses ( short numDoses ) {
        this.numDoses = numDoses;
    }
}

package edu.ncsu.csc.iTrust2.models;

import java.time.temporal.ChronoUnit;

public class DoseInterval {

    private ChronoUnit intervalType;

    private int        intervalAmount;

    /**
     * for hibernate
     */
    public DoseInterval () {

    }

    public DoseInterval ( final ChronoUnit intervalType, final int intervalAmount ) {
        super();
        this.intervalType = intervalType;
        this.intervalAmount = intervalAmount;
    }

    /**
     * @return the intervalType
     */
    public ChronoUnit getIntervalType () {
        return intervalType;
    }

    /**
     * @param intervalType
     *            the intervalType to set
     */
    public void setIntervalType ( final ChronoUnit intervalType ) {
        this.intervalType = intervalType;
    }

    /**
     * @return the intervalAmount
     */
    public int getIntervalAmount () {
        return intervalAmount;
    }

    /**
     * @param intervalAmount
     *            the intervalAmount to set
     */
    public void setIntervalAmount ( final int intervalAmount ) {
        this.intervalAmount = intervalAmount;
    }

}

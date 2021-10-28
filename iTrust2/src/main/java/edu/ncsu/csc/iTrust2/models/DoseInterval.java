package edu.ncsu.csc.iTrust2.models;

import java.time.temporal.ChronoUnit;

import javax.persistence.Embeddable;

/**
 * object that represents a dose interval, used now for vaccines
 *
 * @author Jack
 *
 */
@Embeddable
public class DoseInterval {

    /**
     * type of the interval
     */
    private ChronoUnit intervalType;

    /**
     * amount of the interval
     */
    private Integer    intervalAmount;

    /**
     * for hibernate
     */
    public DoseInterval () {

    }

    /**
     * construct a dose interval with the provided interval type and interval
     * amount
     *
     * @param intervalType
     *            interval type to construct with
     * @param intervalAmount
     *            interval amount to construct with
     */
    public DoseInterval ( final ChronoUnit intervalType, final int intervalAmount ) {
        super();
        this.intervalType = intervalType;
        this.intervalAmount = intervalAmount;
    }

    /**
     * return the interval type
     *
     * @return the intervalType
     */
    public ChronoUnit getIntervalType () {
        return intervalType;
    }

    /**
     * set the interval type
     *
     * @param intervalType
     *            the intervalType to set
     */
    public void setIntervalType ( final ChronoUnit intervalType ) {
        this.intervalType = intervalType;
    }

    /**
     * get the interval amount
     *
     * @return the intervalAmount
     */
    public int getIntervalAmount () {
        return intervalAmount;
    }

    /**
     * set the interval amount
     *
     * @param intervalAmount
     *            the intervalAmount to set
     */
    public void setIntervalAmount ( final int intervalAmount ) {
        this.intervalAmount = intervalAmount;
    }

}

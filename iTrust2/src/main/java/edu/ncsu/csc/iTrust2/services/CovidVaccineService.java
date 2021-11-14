package edu.ncsu.csc.iTrust2.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import edu.ncsu.csc.iTrust2.models.CovidVaccine;
import edu.ncsu.csc.iTrust2.repositories.CovidVaccineRepository;

/**
 * service for CRUD operation on covidvaccine objects
 *
 * @author Jack
 *
 */
@Component
@Transactional
public class CovidVaccineService extends Service<CovidVaccine, Long> {

    /** Repository for CRUD operations */
    @Autowired
    private CovidVaccineRepository repository;

    @Override
    protected JpaRepository<CovidVaccine, Long> getRepository () {
        return repository;
    }

    /**
     * Checks if a CovidVaccine with the provided code exists
     *
     * @param code
     *            Code to check
     * @return If CovidVaccine with this code exists
     */
    public boolean existsByCode ( final String code ) {
        return repository.existsByCode( code );
    }

    /**
     * Finds a CovidVaccine with the provided code
     *
     * @param code
     *            Code to check
     * @return CovidVaccine, if found
     */
    public CovidVaccine findByCode ( final String code ) {
        return repository.findByCode( code );
    }

}

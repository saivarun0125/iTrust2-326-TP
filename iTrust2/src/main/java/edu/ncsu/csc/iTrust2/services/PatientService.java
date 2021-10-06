package edu.ncsu.csc.iTrust2.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.repositories.PatientRepository;

/**
 * Service class for interacting with Patient model, performing CRUD tasks with
 * database.
 *
 * @author Kai Presler-Marshall
 * @param <T>
 *            Type of user
 *
 */
@Component
@Transactional
public class PatientService <T extends User> extends UserService<Patient> {

    /**
     * Repository for CRUD operations
     */
    @Autowired
    private PatientRepository<Patient> repository;

    @Override
    protected JpaRepository<Patient, String> getRepository () {
        return repository;
    }

}

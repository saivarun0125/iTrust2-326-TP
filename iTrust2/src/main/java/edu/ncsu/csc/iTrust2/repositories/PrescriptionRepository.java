package edu.ncsu.csc.iTrust2.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ncsu.csc.iTrust2.models.Prescription;
import edu.ncsu.csc.iTrust2.models.User;

/**
 * Repository for interacting with Prescription model. Method implementations
 * generated by Spring
 *
 * @author Kai Presler-Marshall
 *
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    /**
     * Find all Prescriptions for a given Patient
     * 
     * @param patient
     *            User to find prescriptions for
     * @return Matching Prescriptions
     */
    public List<Prescription> findByPatient ( final User patient );

}

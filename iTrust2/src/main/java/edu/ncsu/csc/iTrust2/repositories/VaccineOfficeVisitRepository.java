package edu.ncsu.csc.iTrust2.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineOfficeVisit;

/**
 *
 * @author Kon Buchanan
 *
 */
public interface VaccineOfficeVisitRepository extends JpaRepository<VaccineOfficeVisit, Long> {

    /**
     * Find vaccine office visits for a given patient
     *
     * @param hcp
     *            HCP to search by
     * @return Matching visits
     */
    public List<VaccineOfficeVisit> findByHcp ( User hcp );

    /**
     * Find vaccine office visits for a given HCP
     *
     * @param patient
     *            Patient to search by
     * @return Matching visits
     */
    public List<VaccineOfficeVisit> findByPatient ( User patient );

    /**
     * Find vaccine office visits for a given HCP and patient
     *
     * @param hcp
     *            HCP to search by
     * @param patient
     *            Patient to search by
     * @return Matching visits
     */
    public List<VaccineOfficeVisit> findByHcpAndPatient ( User hcp, User patient );
}

package edu.ncsu.csc.iTrust2.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.ncsu.csc.iTrust2.TestConfig;
import edu.ncsu.csc.iTrust2.forms.DrugForm;
import edu.ncsu.csc.iTrust2.forms.OfficeVisitForm;
import edu.ncsu.csc.iTrust2.forms.OphthalmologyVisitForm;
import edu.ncsu.csc.iTrust2.forms.PrescriptionForm;
import edu.ncsu.csc.iTrust2.forms.UserForm;
import edu.ncsu.csc.iTrust2.models.Drug;
import edu.ncsu.csc.iTrust2.models.OphthalmologyMetrics;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.Prescription;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.services.DrugService;
import edu.ncsu.csc.iTrust2.services.OphthalmologyMetricsService;
import edu.ncsu.csc.iTrust2.services.PatientService;
import edu.ncsu.csc.iTrust2.services.PrescriptionService;

@RunWith ( SpringRunner.class )
@EnableAutoConfiguration
@SpringBootTest ( classes = TestConfig.class )
public class PrescriptionTest {

    @Autowired
    private DrugService drugService;

    @Autowired
    private PrescriptionService service;

    @Autowired
    private PatientService patientService;

    @Test
    public void testBuildFromForm () {

        if (!drugService.existsByCode("1234-5678-90")) {
            DrugForm drugForm = new DrugForm();
            drugForm.setCode("1234-5678-90");
            drugForm.setDescription("Tylenol - Acetominophen");
            drugForm.setName("Tylenol");
            drugService.save(new Drug(drugForm));
        }
        if (!patientService.existsByName("headachepatient")) {
            patientService.save(new Patient(new UserForm("headachepatient", "patient", Role.ROLE_PATIENT, 1)));
        }

        final PrescriptionForm pf = new PrescriptionForm();
        pf.setDrug("1234-5678-90");
        pf.setDosage(50);
        pf.setPatient("headachepatient");
        pf.setStartDate("2021-01-01");
        pf.setEndDate("2022-02-02");

        Prescription prescription = service.build(pf);
        assertNotNull(prescription);
        assertNotNull(prescription.getDrug());
        assertEquals("Tylenol", prescription.getDrug().getName());
    }
}

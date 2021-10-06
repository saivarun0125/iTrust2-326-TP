package edu.ncsu.csc.iTrust2.unit;

import java.time.ZonedDateTime;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.ncsu.csc.iTrust2.TestConfig;
import edu.ncsu.csc.iTrust2.forms.DiagnosisListForm;
import edu.ncsu.csc.iTrust2.forms.UserForm;
import edu.ncsu.csc.iTrust2.models.Diagnosis;
import edu.ncsu.csc.iTrust2.models.ICDCode;
import edu.ncsu.csc.iTrust2.models.OfficeVisit;
import edu.ncsu.csc.iTrust2.models.Personnel;
import edu.ncsu.csc.iTrust2.models.enums.Role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Class to test that DiagnosisList and DiagnosisListForms are created from each
 * other properly.
 *
 * @author bvolpat
 *
 */
@RunWith ( SpringRunner.class )
@EnableAutoConfiguration
@SpringBootTest ( classes = TestConfig.class )
public class DiagnosisListTest {

    @Test
    @Transactional
    public void testCodes () {

        final Diagnosis diag = new Diagnosis();

        final ICDCode icdCode = new ICDCode();
        icdCode.setCode( "H26.9" );
        icdCode.setDescription( "Cataracts" );
        icdCode.setIsOphthalmology( true );

        final OfficeVisit officeVisit = new OfficeVisit();
        officeVisit.setId( 1L );
        officeVisit.setDate( ZonedDateTime.now() );
        officeVisit.setHcp( new Personnel( new UserForm( "hcp", "hcp", Role.ROLE_HCP, 1 ) ) );

        diag.setCode( icdCode );
        diag.setNote( "Good note" );
        diag.setVisit( officeVisit );

        final DiagnosisListForm form = new DiagnosisListForm( diag );
        assertNotNull( form );
        assertEquals( diag.getVisit().getDate(), form.getVisitDate() );
        assertEquals( "Good note", form.getNote() );
        assertEquals( "hcp", form.getHcp().getUsername() );
    }

}

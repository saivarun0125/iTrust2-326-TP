package edu.ncsu.csc.iTrust2.unit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.ncsu.csc.iTrust2.TestConfig;
import edu.ncsu.csc.iTrust2.forms.OfficeVisitForm;
import edu.ncsu.csc.iTrust2.forms.OphthalmologyVisitForm;
import edu.ncsu.csc.iTrust2.models.OphthalmologyMetrics;
import edu.ncsu.csc.iTrust2.services.OphthalmologyMetricsService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith ( SpringRunner.class )
@EnableAutoConfiguration
@SpringBootTest ( classes = TestConfig.class )
public class OphthalmologyMetricsTest {

    @Autowired
    private OphthalmologyMetricsService service;

    @Test
    public void testBuildFromForm () {

        final OphthalmologyVisitForm ov = new OphthalmologyVisitForm();
        ov.setAxisLeft( 1.0f );
        ov.setAxisRight( 1.0f );
        ov.setCylinderLeft( 1.0f );
        ov.setCylinderRight( 1.0f );
        ov.setSphereLeft( 0.0f );
        ov.setSphereRight( 0.0f );
        ov.setVisualAcuityLeft( 20 );
        ov.setVisualAcuityRight( 20 );

        final OfficeVisitForm officeVisitForm = new OfficeVisitForm();
        officeVisitForm.setOphthalmologyVisitForm( ov );

        final OphthalmologyMetrics ophthalmologyMetrics = service.build( officeVisitForm );
        assertNotNull( ophthalmologyMetrics );
        assertEquals( ophthalmologyMetrics.getAxisLeft(), 1.0f, 1e-7 );
        assertEquals( ophthalmologyMetrics.getAxisRight(), 1.0f, 1e-7 );
        assertEquals( ophthalmologyMetrics.getCylinderLeft(), 1.0f, 1e-7 );
        assertEquals( ophthalmologyMetrics.getCylinderRight(), 1.0f, 1e-7 );
        assertEquals( ophthalmologyMetrics.getSphereLeft(), 0.0f, 1e-7 );
        assertEquals( ophthalmologyMetrics.getSphereRight(), 0.0f, 1e-7 );
        assertEquals( ophthalmologyMetrics.getVisualAcuityLeft(), Integer.valueOf( 20 ) );
        assertEquals( ophthalmologyMetrics.getVisualAcuityRight(), Integer.valueOf( 20 ) );

        final OphthalmologyMetrics ophthalmologyMetricsSecond = service.build( officeVisitForm );
        assertNotNull( ophthalmologyMetricsSecond );
        assertEquals( ophthalmologyMetrics, ophthalmologyMetricsSecond );
        assertEquals( ophthalmologyMetrics.hashCode(), ophthalmologyMetricsSecond.hashCode() );

    }
}

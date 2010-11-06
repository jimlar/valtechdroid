package se.jimlar;

import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class APIResponseParserTest extends TestCase {
    private APIResponseParser parser;

    protected void setUp() throws Exception {
        super.setUp();
        parser = new APIResponseParser();
    }

    public void testShouldGiveEmptyListForEmptyJSON() throws Exception {
        List<Employee> employees = parser.parseEmployees("[]");
        assertNotNull(employees);
        assertEquals(0, employees.size());
    }

    public void testReadsValtechTestFile() throws Exception {
        List<Employee> employees = parser.parseEmployees(testData());
        assertEquals(1, employees.size());
        assertEquals("Jimmy", employees.get(0).getFirstName());
        assertEquals("Larsson", employees.get(0).getLastName());
        assertEquals("+46 701653474", employees.get(0).getPhone());
        assertEquals("jimmy.larsson@valtech.se", employees.get(0).getEmail());
    }

    private String testData() throws IOException {
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/employees.json");
            return IOUtils.toString(in, "utf-8");
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}

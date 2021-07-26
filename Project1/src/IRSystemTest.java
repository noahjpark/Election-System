import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * IR System unit Tests
 *
 * These tests are fully automatic and do not require any manual steps.
 *
 * @author Justin Lam
 */
public class IRSystemTest {

    private final ByteArrayOutputStream systemOut = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    /**
     * Redirects any standard system output to a print steam.
     */
    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(systemOut));
        // delete the files before so there are only one set of media/audit files after running algorithm
        IRTestHelpers.deleteElectionOutputFiles();
    }

    /**
     * Restores system output to display
     */
    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        // delete the files so there are not a bunch of them floating around
        IRTestHelpers.deleteElectionOutputFiles();
    }

    /**
     * Tests the given IR csv example from the instructor.
     */
    @Test
    public void givenIRTest() {
        String[] args = new String[]{"testing/testFiles/givenIR.csv"};
        Eligere.main(args);

        // get the example output files
        String expectedOutput = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/ExpectedGivenIROutput.txt");
        String expectedAudit = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/ExpectedGivenIRAudit.txt");
        String expectedMedia = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/ExpectedGivenIRMedia.txt");

        assertNotNull(expectedOutput);
        assertNotNull(expectedAudit);
        assertNotNull(expectedMedia);

        // find the new media and audit files
        String mediaName = null, auditName = null, currentFileName;
        File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
        File[] listOfFiles = currentDirectory.listFiles();
        assertNotNull(listOfFiles);

        for (File file : listOfFiles) {
            currentFileName = file.getName();
            String[] currentSplit = currentFileName.split("_");
            if ("IRMediaReport".equals(currentSplit[0])) mediaName = currentFileName;
            else if ("IRAuditFile".equals(currentSplit[0])) auditName = currentFileName;
        }

        // get contents of media and audit files
        String actualMedia = IRTestHelpers.getAllFileContents(mediaName);
        String actualAudit = IRTestHelpers.getAllFileContents(auditName);

        assertNotNull(actualMedia);
        assertNotNull(actualAudit);

        // output match
        String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");
        assertEquals(expectedOutput, actualOutput);

        // audit match
        assertEquals(expectedAudit, actualAudit);

        // media match
        assertEquals(expectedMedia, actualMedia);
    }

    /**
     * Tests the given large IR example from the instructor with a 100000 ballots.
     */
    @Test
    public void givenBigIRTest() {
        String[] args = new String[]{"testing/testFiles/givenBigIR.csv"};
        Eligere.main(args);

        // get the example output files
        String expectedOutput = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/ExpectedGivenBigIROutput.txt");
        String expectedAudit = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/ExpectedGivenBigIRAudit.txt");
        String expectedMedia = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/ExpectedGivenBigIRMedia.txt");

        assertNotNull(expectedOutput);
        assertNotNull(expectedAudit);
        assertNotNull(expectedMedia);

        // find the new media and audit files
        String mediaName = null, auditName = null, currentFileName;
        File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
        File[] listOfFiles = currentDirectory.listFiles();
        assertNotNull(listOfFiles);

        for (File file : listOfFiles) {
            currentFileName = file.getName();
            String[] currentSplit = currentFileName.split("_");
            if ("IRMediaReport".equals(currentSplit[0])) mediaName = currentFileName;
            else if ("IRAuditFile".equals(currentSplit[0])) auditName = currentFileName;
        }

        // get contents of media and audit files
        String actualMedia = IRTestHelpers.getAllFileContents(mediaName);
        String actualAudit = IRTestHelpers.getAllFileContents(auditName);

        assertNotNull(actualMedia);
        assertNotNull(actualAudit);

        // output match
        String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");
        assertEquals(expectedOutput, actualOutput);

        // audit match
        assertEquals(expectedAudit, actualAudit);

        // media match
        assertEquals(expectedMedia, actualMedia);
    }

    /**
     * Tests a csv where there is only one candidate.
     */
    @Test
    public void oneCandidateTest() {
        String[] args = new String[]{"testing/testFiles/oneCandidateIR.csv"};
        Eligere.main(args);

        // get the example output files
        String expectedOutput = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedOneCandidateIROutput.txt");
        String expectedAudit = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedOneCandidateIRAudit.txt");
        String expectedMedia = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedOneCandidateIRMedia.txt");

        assertNotNull(expectedOutput);
        assertNotNull(expectedAudit);
        assertNotNull(expectedMedia);

        // find the new media and audit files
        String mediaName = null, auditName = null, currentFileName;
        File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
        File[] listOfFiles = currentDirectory.listFiles();
        assertNotNull(listOfFiles);

        for (File file : listOfFiles) {
            currentFileName = file.getName();
            String[] currentSplit = currentFileName.split("_");
            if ("IRMediaReport".equals(currentSplit[0])) mediaName = currentFileName;
            else if ("IRAuditFile".equals(currentSplit[0])) auditName = currentFileName;
        }

        // get contents of media and audit files
        String actualMedia = IRTestHelpers.getAllFileContents(mediaName);
        String actualAudit = IRTestHelpers.getAllFileContents(auditName);

        assertNotNull(actualMedia);
        assertNotNull(actualAudit);

        // output match
        expectedOutput = expectedOutput.replaceAll("\n", "").replaceAll("\r", "");
        String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");
        assertEquals(expectedOutput, actualOutput);

        // audit match
        assertEquals(expectedAudit, actualAudit);

        // media match
        assertEquals(expectedMedia, actualMedia);
    }

    /**
     * Tests a 100000 ballot csv with a large number of candidates.
     */
    @Test
    public void bigIRTest() {
        String[] args = new String[]{"testing/testFiles/bigRandomIR.csv"};

        //times the system
        long startTime = System.nanoTime();
        Eligere.main(args);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/ (long) 1000000;
        //checks if system finished running within 8 minutes.
        boolean durationLessThan8Minutes = duration < 480000;
        assertTrue(durationLessThan8Minutes);

        // get the example output files
        String expectedOutput = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedBigIROutput.txt");
        String expectedAudit = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedBigIRAudit.txt");
        String expectedMedia = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedBigIRMedia.txt");

        assertNotNull(expectedOutput);
        assertNotNull(expectedAudit);
        assertNotNull(expectedMedia);

        // find the new media and audit files
        String mediaName = null, auditName = null, currentFileName;
        File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
        File[] listOfFiles = currentDirectory.listFiles();
        assertNotNull(listOfFiles);

        for (File file : listOfFiles) {
            currentFileName = file.getName();
            String[] currentSplit = currentFileName.split("_");
            if ("IRMediaReport".equals(currentSplit[0])) mediaName = currentFileName;
            else if ("IRAuditFile".equals(currentSplit[0])) auditName = currentFileName;
        }

        // get contents of media and audit files
        String actualMedia = IRTestHelpers.getAllFileContents(mediaName);
        String actualAudit = IRTestHelpers.getAllFileContents(auditName);

        assertNotNull(actualMedia);
        assertNotNull(actualAudit);

        // output match
        String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");
        assertEquals(expectedOutput, actualOutput);

        // audit match
        assertEquals(expectedAudit, actualAudit);

        // media match
        assertEquals(expectedMedia, actualMedia);
    }

    /**
     * Tests a csv where two candidates tie for most votes.
     */
    @Test
    public void winnerTieTest() {
        String[] args = new String[]{"testing/testFiles/winnerTieIR.csv"};
        Eligere.main(args);

        // get the example output files
        String expectedOutput1 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedTieIROutput_1.txt");
        String expectedAudit1 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedTieIRAudit_1.txt");
        String expectedMedia1 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedTieIRMedia_1.txt");
        String expectedOutput2 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedTieIROutput_2.txt");
        String expectedAudit2 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedTieIRAudit_2.txt");
        String expectedMedia2 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedTieIRMedia_2.txt");

        assertNotNull(expectedOutput1);
        assertNotNull(expectedAudit1);
        assertNotNull(expectedMedia1);
        assertNotNull(expectedOutput2);
        assertNotNull(expectedAudit2);

        // find the new media and audit files
        String mediaName = null, auditName = null, currentFileName;
        File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
        File[] listOfFiles = currentDirectory.listFiles();
        assertNotNull(listOfFiles);

        for (File file : listOfFiles) {
            currentFileName = file.getName();
            String[] currentSplit = currentFileName.split("_");
            if ("IRMediaReport".equals(currentSplit[0])) mediaName = currentFileName;
            else if ("IRAuditFile".equals(currentSplit[0])) auditName = currentFileName;
        }

        // get contents of media and audit files
        String actualMedia = IRTestHelpers.getAllFileContents(mediaName);
        String actualAudit = IRTestHelpers.getAllFileContents(auditName);

        assertNotNull(actualMedia);
        assertNotNull(actualAudit);

        // output match
        String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");
        assertTrue(expectedOutput1.equals(actualOutput) || expectedOutput2.equals(actualOutput));

        // audit match
        assertTrue(expectedAudit1.equals(actualAudit) || expectedAudit2.equals(actualAudit));

        // media match
        assertTrue(expectedMedia1.equals(actualMedia) || expectedMedia2.equals(actualMedia));
    }

    /**
     * Tests a csv where two candidates tie for least number of votes.
     */
    @Test
    public void loserTieTest() {
        String[] args = new String[]{"testing/testFiles/loserTieIR.csv"};
        Eligere.main(args);

        // get the example output files
        String expectedOutput1 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedLoserTieIROutput_1.txt");
        String expectedAudit1 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedLoserTieIRAudit_1.txt");
        String expectedMedia1 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedLoserTieIRMedia_1.txt");
        String expectedOutput2 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedLoserTieIROutput_2.txt");
        String expectedAudit2 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedLoserTieIRAudit_2.txt");
        String expectedMedia2 = IRTestHelpers.getAllFileContents("testing/ExampleIRFiles/" +
                "ExpectedLoserTieIRMedia_2.txt");

        assertNotNull(expectedOutput1);
        assertNotNull(expectedAudit1);
        assertNotNull(expectedMedia1);
        assertNotNull(expectedOutput2);
        assertNotNull(expectedAudit2);

        // find the new media and audit files
        String mediaName = null, auditName = null, currentFileName;
        File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
        File[] listOfFiles = currentDirectory.listFiles();
        assertNotNull(listOfFiles);

        for (File file : listOfFiles) {
            currentFileName = file.getName();
            String[] currentSplit = currentFileName.split("_");
            if ("IRMediaReport".equals(currentSplit[0])) mediaName = currentFileName;
            else if ("IRAuditFile".equals(currentSplit[0])) auditName = currentFileName;
        }

        // get contents of media and audit files
        String actualMedia = IRTestHelpers.getAllFileContents(mediaName);
        String actualAudit = IRTestHelpers.getAllFileContents(auditName);

        assertNotNull(actualMedia);
        assertNotNull(actualAudit);

        // output match
        String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");
        assertTrue(expectedOutput1.equals(actualOutput) || expectedOutput2.equals(actualOutput));

        // audit match
        assertTrue(expectedAudit1.equals(actualAudit) || expectedAudit2.equals(actualAudit));

        // media match
        assertTrue(expectedMedia1.equals(actualMedia) || expectedMedia2.equals(actualMedia));
    }
}


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OPL System unit Tests
 *
 * These tests are fully automatic and do not require any manual steps.
 *
 * @author Noah Park
 */
public class OPLSystemTest {

  private final ByteArrayOutputStream systemOut = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  /**
   * Redirects any standard system output to a print steam.
   */
  @BeforeEach
  public void setUp() {
    System.setOut(new PrintStream(systemOut));
    // delete the files before so there are only one set of media/audit files after running algorithm
    OPLTestHelpers.deleteElectionOutputFiles();
  }

  /**
   * Restores system output to display
   */
  @AfterEach
  public void tearDown() {
    System.setOut(originalOut);
    // delete the files so there are not a bunch of them floating around
    OPLTestHelpers.deleteElectionOutputFiles();
  }

  /**
   * Tests the given OPL example from the instructor.
   */
  @Test
  public void givenOPLTest() {
    String[] args = new String[]{"testing/testFiles/givenOPL.csv"};
    Eligere.main(args);

    // get the example output files
    String expectedOutput = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedGivenOPLOutput.txt");
    String expectedAudit = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedGivenOPLAudit.txt");
    String expectedMedia = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedGivenOPLMedia.txt");

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
      if ("OPLMediaReport".equals(currentSplit[0])) mediaName = currentFileName;
      else if ("OPLAuditFile".equals(currentSplit[0])) auditName = currentFileName;
    }

    // get contents of media and audit files
    String actualMedia = OPLTestHelpers.getAllFileContents(mediaName);
    String actualAudit = OPLTestHelpers.getAllFileContents(auditName);

    assertNotNull(actualMedia);
    assertNotNull(actualAudit);

    // output match
    expectedOutput = expectedOutput.replaceAll("\n", "").replaceAll("\r", "");
    String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");
    assertEquals(expectedOutput, actualOutput);

    // audit match
    expectedAudit = expectedAudit.replaceAll("\n", "").replaceAll("\r", "");
    actualAudit = actualAudit.replaceAll("\n", "").replaceAll("\r", "");
    assertEquals(expectedAudit, actualAudit);

    // media match
    expectedMedia = expectedMedia.replaceAll("\n", "").replaceAll("\r", "");
    actualMedia = actualMedia.replaceAll("\n", "").replaceAll("\r", "");
    assertEquals(expectedMedia, actualMedia);
  }

  /**
   * Tests a 100,000 ballot csv.
   */
  @Test
  public void bigOPLTest() {
    String[] args = new String[]{ "testing/testFiles/bigOPL2.csv" };
    Eligere.main(args);

    // get the example output files
    String expectedOutput = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedBigOPLOutput.txt");
    String expectedAudit = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedBigOPLAudit.txt");
    String expectedMedia = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedBigOPLMedia.txt");

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
      if ("OPLMediaReport".equals(currentSplit[0])) mediaName = currentFileName;
      else if ("OPLAuditFile".equals(currentSplit[0])) auditName = currentFileName;
    }

    // get contents of media and audit files
    String actualMedia = OPLTestHelpers.getAllFileContents(mediaName);
    String actualAudit = OPLTestHelpers.getAllFileContents(auditName);

    assertNotNull(actualMedia);
    assertNotNull(actualAudit);

    // output match
    expectedOutput = expectedOutput.replaceAll("\n", "").replaceAll("\r", "");
    String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");
    assertEquals(expectedOutput, actualOutput);

    // audit match
    expectedAudit = expectedAudit.replaceAll("\n", "").replaceAll("\r", "");
    actualAudit = actualAudit.replaceAll("\n", "").replaceAll("\r", "");
    assertEquals(expectedAudit, actualAudit);

    // media match
    expectedMedia = expectedMedia.replaceAll("\n", "").replaceAll("\r", "");
    actualMedia = actualMedia.replaceAll("\n", "").replaceAll("\r", "");
    assertEquals(expectedMedia, actualMedia);
  }

  /**
   * Tests two candidates tying in an OPL election.
   */
  @Test
  public void candidateTieOPLTest() {
    String[] args = new String[]{ "testing/testFiles/candidateTieOPL.csv" };
    Eligere.main(args);

    // get the example output files
    String expectedOutput = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedCandidateTieOPLOutput.txt");
    String expectedOutput2 = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedCandidateTieOPLOutput2.txt");
    String expectedAudit = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedCandidateTieOPLAudit.txt");
    String expectedAudit2 = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedCandidateTieOPLAudit2.txt");
    String expectedMedia = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedCandidateTieOPLMedia.txt");
    String expectedMedia2 = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedCandidateTieOPLMedia2.txt");

    assertNotNull(expectedOutput);
    assertNotNull(expectedOutput2);
    assertNotNull(expectedAudit);
    assertNotNull(expectedAudit2);
    assertNotNull(expectedMedia);
    assertNotNull(expectedMedia2);

    // find the new media and audit files
    String mediaName = null, auditName = null, currentFileName;
    File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
    File[] listOfFiles = currentDirectory.listFiles();
    assertNotNull(listOfFiles);

    for (File file : listOfFiles) {
      currentFileName = file.getName();
      String[] currentSplit = currentFileName.split("_");
      if ("OPLMediaReport".equals(currentSplit[0])) mediaName = currentFileName;
      else if ("OPLAuditFile".equals(currentSplit[0])) auditName = currentFileName;
    }

    // get contents of media and audit files
    String actualMedia = OPLTestHelpers.getAllFileContents(mediaName);
    String actualAudit = OPLTestHelpers.getAllFileContents(auditName);

    assertNotNull(actualMedia);
    assertNotNull(actualAudit);

    // output match
    expectedOutput = expectedOutput.replaceAll("\n", "").replaceAll("\r", "");
    expectedOutput2 = expectedOutput2.replaceAll("\n", "").replaceAll("\r", "");
    String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");
    assertTrue(expectedOutput.equals(actualOutput) || expectedOutput2.equals(actualOutput));

    // audit match
    expectedAudit = expectedAudit.replaceAll("\n", "").replaceAll("\r", "");
    expectedAudit2 = expectedAudit2.replaceAll("\n", "").replaceAll("\r", "");
    actualAudit = actualAudit.replaceAll("\n", "").replaceAll("\r", "");
    assertTrue(expectedAudit.equals(actualAudit) || expectedAudit2.equals(actualAudit));

    // media match
    expectedMedia = expectedMedia.replaceAll("\n", "").replaceAll("\r", "");
    expectedMedia2 = expectedMedia2.replaceAll("\n", "").replaceAll("\r", "");
    actualMedia = actualMedia.replaceAll("\n", "").replaceAll("\r", "");
    assertTrue(expectedMedia.equals(actualMedia) || expectedMedia2.equals(actualMedia));
  }

  /**
   * Tests two parties tying in an OPL election.
   */
  @Test
  public void partyTieOPLTest() {
    String[] args = new String[]{ "testing/testFiles/partyTieOPL.csv" };
    Eligere.main(args);

    // get the example output files
    String expectedOutput = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedPartyTieOPLOutput.txt");
    String expectedOutput2 = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedPartyTieOPLOutput2.txt");
    String expectedAudit = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedPartyTieOPLAudit.txt");
    String expectedAudit2 = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedPartyTieOPLAudit2.txt");
    String expectedMedia = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedPartyTieOPLMedia.txt");
    String expectedMedia2 = OPLTestHelpers.getAllFileContents("testing/ExampleFiles/ExpectedPartyTieOPLMedia2.txt");

    assertNotNull(expectedOutput);
    assertNotNull(expectedOutput2);
    assertNotNull(expectedAudit);
    assertNotNull(expectedAudit2);
    assertNotNull(expectedMedia);
    assertNotNull(expectedMedia2);

    // find the new media and audit files
    String mediaName = null, auditName = null, currentFileName;
    File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
    File[] listOfFiles = currentDirectory.listFiles();
    assertNotNull(listOfFiles);

    for (File file : listOfFiles) {
      currentFileName = file.getName();
      String[] currentSplit = currentFileName.split("_");
      if ("OPLMediaReport".equals(currentSplit[0])) mediaName = currentFileName;
      else if ("OPLAuditFile".equals(currentSplit[0])) auditName = currentFileName;
    }

    // get contents of media and audit files
    String actualMedia = OPLTestHelpers.getAllFileContents(mediaName);
    String actualAudit = OPLTestHelpers.getAllFileContents(auditName);

    assertNotNull(actualMedia);
    assertNotNull(actualAudit);

    // output match
    expectedOutput = expectedOutput.replaceAll("\n", "").replaceAll("\r", "");
    expectedOutput2 = expectedOutput2.replaceAll("\n", "").replaceAll("\r", "");
    String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");
    assertTrue(expectedOutput.equals(actualOutput) || expectedOutput2.equals(actualOutput));

    // audit match
    expectedAudit = expectedAudit.replaceAll("\n", "").replaceAll("\r", "");
    expectedAudit2 = expectedAudit2.replaceAll("\n", "").replaceAll("\r", "");
    actualAudit = actualAudit.replaceAll("\n", "").replaceAll("\r", "");
    assertTrue(expectedAudit.equals(actualAudit) || expectedAudit2.equals(actualAudit));

    // media match
    expectedMedia = expectedMedia.replaceAll("\n", "").replaceAll("\r", "");
    expectedMedia2 = expectedMedia2.replaceAll("\n", "").replaceAll("\r", "");
    actualMedia = actualMedia.replaceAll("\n", "").replaceAll("\r", "");
    assertTrue(expectedMedia.equals(actualMedia) || expectedMedia2.equals(actualMedia));
  }

}

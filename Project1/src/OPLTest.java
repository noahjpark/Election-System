import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OPL Unit Tests
 *
 * These tests are fully automatic and do not require any manual steps.
 *
 * @author Michael Markiewicz
 */
public class OPLTest {
  private final ByteArrayOutputStream systemOut = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  /**
   * Redirects any standard system output to a print steam.
   */
  @BeforeEach
  public void setUp() {
    System.setOut(new PrintStream(systemOut));
    // delete the files before so there are only one set of media/audit files after
    // running algorithm
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
   * Tests the HandleTie function for illegal arguments (nonpositive numbers)
   */
  @Test
  public void testHandleTieIllegalArguments() {
    // illegal arguments that should cause an IllegalArgumentException
    int[] illegalArguments = {0, -1, -10, -100};
    for (int illegal: illegalArguments) {
      try {
        Election.handleTie(illegal);
        assertTrue(false); // did not throw an exception
      } catch (IllegalArgumentException e) {
        assertTrue(true); // should have thrown an exception
      }
    }
  }

  /**
   * Tests normal OPL constructor.
   */
  @Test
  public void testOPLConstructor() {
    int totalNumBallots = 5520;
    int numSeatsAvailable = 3;

    int numParties = 10;
    int[] numCandidatesPerParty = new int[numParties];
    for (int i = 0; i < numParties; i++) {
      numCandidatesPerParty[i] = 10;
    }
    ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(10, numCandidatesPerParty);

    // should not throw an exception
    OPL correctOPL = new OPL(totalNumBallots, numSeatsAvailable, parties);
    assertEquals(totalNumBallots/numSeatsAvailable, correctOPL.getQuota());
    assertEquals(numSeatsAvailable, correctOPL.getNumSeatsAvailable());
    assertEquals(totalNumBallots, correctOPL.getTotalNumBallots());
  }

  /**
   * Tests exception thrown from constructor when "null" is passed as the parties parameter
   */
  @Test
  public void testOPLConstructorThrowsExceptionNullParties() {
    try {
      OPL exceptionOPL = new OPL(10, 10, null);
      assertEquals(null, exceptionOPL);
    } catch (IllegalArgumentException e) {
      assertEquals("totalNumBallots must be positive, numSeatsAvailable must be positive,"
                      + " and parties must not be null and contain at least one party",
              e.getMessage());
    }
  }

  /**
   * Tests exception thrown from constructor when the parties parameter is initialized but empty
   */
  @Test
  public void testOPLConstructorThrowsExceptionEmptyParties() {
    try {
      OPL exceptionOPL = new OPL(10, 10, new ArrayList<Party>());
      assertEquals(null, exceptionOPL);
    } catch (IllegalArgumentException e) {
      assertEquals("totalNumBallots must be positive, numSeatsAvailable must be positive,"
                      + " and parties must not be null and contain at least one party",
              e.getMessage());
    }
  }

  /**
   * Tests exception thrown from constructor when the parties parameter is initialized but empty
   */
  @Test
  public void testOPLConstructorThrowsExceptionNonPositiveNumBallots() {
    ArrayList<Party> parties = new ArrayList<Party>();
    // doesn't need to be more than one party, and party doesn't need to have candidates
    // for this to work (for now)
    parties.add(new Party("p0"));

    int[] nonPositiveBallots = {-100, -50, -1, 0};
    for (int numBallots: nonPositiveBallots) {
      try {
        OPL exceptionOPL = new OPL(numBallots, 10, parties);
        assertEquals(null, exceptionOPL);
      } catch (IllegalArgumentException e) {
        assertEquals("totalNumBallots must be positive, numSeatsAvailable must be positive,"
                      + " and parties must not be null and contain at least one party",
                      e.getMessage());
      }
    }

    // no exception should be thrown for 1 and above
    OPL correctOPL = new OPL(1, 10, parties);
  }

  /**
   * Tests exception thrown from constructor when the parties parameter is initialized but empty
   */
  @Test
  public void testOPLConstructorThrowsExceptionNonPositiveSeats() {
    ArrayList<Party> parties = new ArrayList<Party>();
    // doesn't need to be more than one party, and party doesn't need to have candidates
    // for this to work (for now)
    parties.add(new Party("p0"));

    int[] nonPositiveSeats = {-100, -50, -1, 0};
    for (int numSeats: nonPositiveSeats) {
      try {
        OPL exceptionOPL = new OPL(10, numSeats, parties);
        assertEquals(null, exceptionOPL);
      } catch (IllegalArgumentException e) {
        assertEquals("totalNumBallots must be positive, numSeatsAvailable must be positive,"
                      + " and parties must not be null and contain at least one party",
                      e.getMessage());
      }
    }

    // no exception should be thrown for 1 and above
    OPL correctOPL = new OPL(10, 1, parties);
  }

  /**
   * Checks if running the OPL Voting Algorithm creates a media and audit report in correct format.
   * This will delete any preexisting media/audit reports before running the algorithm to ensure
   * new ones are created.
   */
  @Test
  public void testOPLRunVotingAlgorithmCheckFilesCreated() {
    // create a number of parties with different numbers of candidates
    int numParties = 5;
    int[] numCandidatesPerParty = {3, 2, 6, 3, 1};
    ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                       numCandidatesPerParty);

    // number of ballots and seats up for election
    int numBallots = 10000;
    int numSeats = 2;

    OPL opl = new OPL(numBallots, numSeats, parties);

    // run the algorithm
    opl.runVotingAlgorithm();

    // check if the media report and audit file were created successfully with correct formatting
    String[] filenames = OPLTestHelpers.findAuditAndMedia();

    // ensure they were actually found
    assertNotNull(filenames[0], "Media file not found");
    assertNotNull(filenames[1], "Audit file not found");

    // check if they have the correct naming structure
    for (int i = 0; i < 2; i++) {
      String[] currentSplit = filenames[i].split("_");
      assertEquals(7, currentSplit.length); // number of items in the name should be 5
      // file format should be txt
      assertEquals("txt", filenames[i].split("\\.")[1]);
    }
  }

  /**
   * Tests that the remaining votes and number of seats of each party matches how much
   * they should have after the first allocation.
   */
  @Test
  public void testOPLRunVotingAlgorithmFirstAllocation() {
    // create a number of parties with different numbers of candidates
    int numParties = 4;
    int[] numCandidatesPerParty = {3, 2, 6, 2};
    ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                       numCandidatesPerParty);

    // set the number of votes for each party
    // fourth party won't get any seats so their remaining votes should equal their totalVotes
    int[] partyVotes = {2000, 3000, 4000, 100};
    // don't need to initialize since we won't be using the candidate votes anyway in this test
    int[][] candidateVotes = new int[numParties][];
    for (int i = 0; i < partyVotes.length; i++) {
      candidateVotes[i] = new int[numCandidatesPerParty[i]];
    }
    OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);
    int totalVotes = 0;
    for (int votes: partyVotes) {
      totalVotes += votes;
    }

    // other election information
    int totalSeats = 3;
    int expectedQuota = totalVotes / totalSeats;

    // run election
    OPL opl = new OPL(totalVotes, totalSeats, parties);
    opl.runVotingAlgorithm();

    // Get the first allocation stats
    HashMap<Party, Integer> firstAllocation = opl.getFirstAllocation();

    for (int i = 0; i < numParties; i++) {
      assertEquals(partyVotes[i]/expectedQuota, firstAllocation.get(parties.get(i)));
      assertEquals(partyVotes[i]%expectedQuota, parties.get(i).getRemainingVotes());
    }

    // fourth party should have gotten any initial seats so their remaining votes should be the
    // same as original
    assertEquals(parties.get(3).getTotalVotes(), parties.get(3).getRemainingVotes());
  }

  /**
   * Ensures that the number of seats each party has after the election is as expected.
   * This test does not induce ties.
   */
  @Test
  public void testOPLRunVotingAlgorithmSecondAllocationNumSeats() {
    // create a number of parties with different numbers of candidates
    int numParties = 4;
    int[] numCandidatesPerParty = {3, 2, 6, 2};
    ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                       numCandidatesPerParty);

    // set the number of votes for each party
    // fourth party won't get any seats so their remaining votes should equal their totalVotes
    int[] partyVotes = {2000, 3000, 4000, 500};
    // don't need to initialize since we won't be using the candidate votes anyway in this test
    int[][] candidateVotes = new int[numParties][];
    for (int i = 0; i < partyVotes.length; i++) {
      candidateVotes[i] = new int[numCandidatesPerParty[i]];
    }
    OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);
    int totalVotes = 0;
    for (int votes: partyVotes) {
      totalVotes += votes;
    }

    // other election information
    int totalSeats = 5;
    int expectedQuota = totalVotes / totalSeats;

    // run election
    OPL opl = new OPL(totalVotes, totalSeats, parties);
    opl.runVotingAlgorithm();

    // first party should have 1 seat
    // second and third party should get 2
    // fourth party should have 0
    assertEquals(1, parties.get(0).getNumberOfSeats());
    assertEquals(2, parties.get(1).getNumberOfSeats());
    assertEquals(2, parties.get(2).getNumberOfSeats());
    assertEquals(0, parties.get(3).getNumberOfSeats());
  }

  /**
   * Ensures that the most popular candidate(s) of each party was elected depending on the number
   * of seats won. Does not induce any ties.
   */
  @Test
  public void testOPLRunVotingAlgorithmCheckWinners() {
    // create a number of parties with different numbers of candidates
    int numParties = 4;
    int[] numCandidatesPerParty = {3, 2, 3, 2};
    ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                       numCandidatesPerParty);

    // set the number of votes for each party
    // fourth party won't get any seats so their remaining votes should equal their totalVotes
    int[] partyVotes = {2000, 3000, 4000, 500};
    int[][] candidateVotes = {{1200, 500, 300},{2000,1000},{500,1000,2500},{300,200}};
    OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);
    int totalVotes = 0;
    for (int votes: partyVotes) {
      totalVotes += votes;
    }

    // set the expected winners
    ArrayList<Candidate> expectedWinners = new ArrayList<>();
    expectedWinners.add(parties.get(0).getCandidates().get(0));
    expectedWinners.add(parties.get(1).getCandidates().get(0));
    expectedWinners.add(parties.get(1).getCandidates().get(1));
    expectedWinners.add(parties.get(2).getCandidates().get(1));
    expectedWinners.add(parties.get(2).getCandidates().get(2));


    // other election information
    int totalSeats = 5;
    int expectedQuota = totalVotes / totalSeats;

    // run election
    OPL opl = new OPL(totalVotes, totalSeats, parties);
    opl.runVotingAlgorithm();

    // get winners
    ArrayList<Candidate> winners = opl.getWinningCandidates();

    // number of winners should be the same
    assertEquals(expectedWinners.size(), winners.size());

    // for each winner, check that they were an expected winner
    int count = 0;
    for (Candidate winner: winners) {
      count = 0;
      for (Candidate expectedWinner: expectedWinners) {
        // should be the same instance, but check candidateID just to make sure
        if (winner.getCandidateID() == expectedWinner.getCandidateID()) {
          count += 1;
        }
      }
      // candidate should only show up once in expected winners list
      assertEquals(1, count);
    }
  }

  /**
   * Ensures that the output to the console, audit file, and media report are expected outputs.
   * This works by running the algorithm, and comparing the outputs to example output files
   * (ExampleOPLOutput.txt, ExampleOPLAudit.txt, ExampleOPLMedia.txt)
   *
   * The tests are designed in such a way where ties should not happen so output should
   * be the same each time.
   */
  @Test
  public void testOPLOutput() {
    // create a number of parties with different numbers of candidates
    int numParties = 4;
    int[] numCandidatesPerParty = {3, 2, 3, 2};
    ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                       numCandidatesPerParty);

    // set the number of votes for each party
    // make it so there are no ties (i.e., the output should be the same every time)
    int[] partyVotes = {2000, 3000, 4000, 500};
    int[][] candidateVotes = {{1200, 500, 300},{2000,1000},{500,1000,2500},{300,200}};
    OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);
    int totalVotes = 0;
    for (int votes: partyVotes) {
      totalVotes += votes;
    }

    // other election information
    int totalSeats = 5;

    // run election
    OPL opl = new OPL(totalVotes, totalSeats, parties);
    opl.runVotingAlgorithm();

    // get the example output files
    String expectedOutput = OPLTestHelpers.getAllFileContents(
            "testing/ExampleFiles/ExampleOPLOutput.txt");
    assertNotNull(expectedOutput);
    String expectedAudit = OPLTestHelpers.getAllFileContents(
            "testing/ExampleFiles/ExampleOPLAudit.txt");
    assertNotNull(expectedAudit);
    String expectedMedia = OPLTestHelpers.getAllFileContents(
            "testing/ExampleFiles/ExampleOPLMedia.txt");
    assertNotNull(expectedMedia);

    // find the new media and audit files
    String[] filenames = OPLTestHelpers.findAuditAndMedia();

    // get contents of media and audit files
    String actualAudit = OPLTestHelpers.getAllFileContents(filenames[1]);
    assertNotNull(actualAudit);
    String actualMedia = OPLTestHelpers.getAllFileContents(filenames[0]);
    assertNotNull(actualMedia);

    // compare example output to screen output
    // remove newlines because they were causing some discrepancies on windows.
    // newlines don't affect our end desired result anyway.
    assertEquals(expectedOutput, systemOut.toString().replaceAll("\n", "").replaceAll("\r", ""));

    // compare output to audit file
    assertEquals(expectedAudit, actualAudit);

    // compare output to media file
    assertEquals(expectedMedia, actualMedia);
  }

  /**
   * This function will run an election where there are less candidates than seats up for election.
   * The expected output of running the algorithm would be that everyone gets a seat but there
   * are still seats left in the election.
   */
  @Test
  public void testOPLLessCandidatesThanSeats() {
    // create a number of parties with different numbers of candidates
    int numParties = 4;
    int[] numCandidatesPerParty = {3, 2, 3, 2};
    ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                       numCandidatesPerParty);

    // set the number of votes for each party
    // make it so there are no ties (i.e., the output should be the same every time)
    int[] partyVotes = {2000, 3000, 4000, 500};
    int[][] candidateVotes = {{1200, 500, 300},{2000,1000},{500,1000,2500},{300,200}};
    OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);
    int totalVotes = 0;
    for (int votes: partyVotes) {
      totalVotes += votes;
    }

    // other election information -- more seats than there are candidates
    int totalSeats = 11;

    // run election
    OPL opl = new OPL(totalVotes, totalSeats, parties);
    opl.runVotingAlgorithm();

    // get the example output files
    String expectedOutput = OPLTestHelpers.getAllFileContents(
            "testing/ExampleFiles/ExampleOPLOutput_MoreSeatsThanCandidates.txt");
    assertNotNull(expectedOutput);
    String expectedAudit = OPLTestHelpers.getAllFileContents(
            "testing/ExampleFiles/ExampleOPLAudit_MoreSeatsThanCandidates.txt");
    assertNotNull(expectedAudit);
    String expectedMedia = OPLTestHelpers.getAllFileContents(
            "testing/ExampleFiles/ExampleOPLMedia_MoreSeatsThanCandidates.txt");
    assertNotNull(expectedMedia);

    // find the new media and audit files
    String[] filenames = OPLTestHelpers.findAuditAndMedia();

    // get contents of media and audit files
    String actualAudit = OPLTestHelpers.getAllFileContents(filenames[1]);
    assertNotNull(actualAudit);
    String actualMedia = OPLTestHelpers.getAllFileContents(filenames[0]);
    assertNotNull(actualMedia);

    // compare example output to screen output
    // remove newlines because they were causing some discrepancies on windows.
    // newlines don't affect our end desired result anyway.
    assertEquals(expectedOutput, systemOut.toString().replaceAll("\n", "").replaceAll("\r", ""));

    // compare output to audit file
    assertEquals(expectedAudit, actualAudit);

    // compare output to media file
    assertEquals(expectedMedia, actualMedia);
  }

  /**
   * This function will run an election where there are less candidates than seats up for election.
   * The expected output of running the algorithm would be that everyone gets a seat but there
   * are still seats left in the election.
   */
  @Test
  public void testOPLOneParty() {
    // create a number of parties with different numbers of candidates
    int numParties = 1;
    int[] numCandidatesPerParty = {4};
    ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
            numCandidatesPerParty);

    // set the number of votes for each party
    int[] partyVotes = {2011};
    int[][] candidateVotes = {{1000,1000,10,1}};
    OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);
    int totalVotes = partyVotes[0];
    int totalSeats = 4;

    // run election
    OPL opl = new OPL(totalVotes, totalSeats, parties);
    opl.runVotingAlgorithm();
    ArrayList<Candidate> testResult = opl.getWinningCandidates();
    ArrayList<String> candidateNames = parties.get(0).getCandidateNames();
    for(int i = 0; i<numCandidatesPerParty[0]; i++){
      String testCandidateName = testResult.get(i).getName();
      String actualCandidateName = candidateNames.get(i);
      assertEquals(testCandidateName,actualCandidateName);
    }
  }


  /**
   * Tests to see if the output from running an OPL algorithm with a candidate tie
   * matches the expected output from a tie.
   */
  @Test
  public void testOPLTiedCandidatesExpectedOutput() {
    // basic info that wont need to reset each iteration
    int numParties = 4;
    int[] numCandidatesPerParty = {4, 2, 3, 2};

    // the first 3 candidates in party 1 will have a tie for popularity votes
    // party 1 will earn 1 seat
    int[] partyVotes = {2000, 3000, 4000, 500};
    int[][] candidateVotes = {{600, 600, 600, 200},{2000,1000},{500,1000,2500},{300,200}};
    int totalVotes = 0;
    for (int votes: partyVotes) {
      totalVotes += votes;
    }

    // other election information
    int totalSeats = 5;
    int numTies = 3;

    // get the 3 possible example output, audit, and media files
    String[] expectedOutputs = new String[numTies];
    String[] expectedAudits = new String[numTies];
    String[] expectedMedias = new String[numTies];
    for (int i = 0; i < numTies; i++) {
      expectedOutputs[i] = OPLTestHelpers.getAllFileContents(
              "testing/ExampleFiles/ExampleOPLOutput_TiedCandidates" + i + ".txt");
      assertNotNull(expectedOutputs[i]);
      expectedAudits[i] = OPLTestHelpers.getAllFileContents(
              "testing/ExampleFiles/ExampleOPLAudit_TiedCandidates" + i + ".txt");
      assertNotNull(expectedAudits[i]);
      expectedMedias[i] = OPLTestHelpers.getAllFileContents(
              "testing/ExampleFiles/ExampleOPLMedia_TiedCandidates" + i + ".txt");
      assertNotNull(expectedMedias[i]);
    }

    // repeat this test a lot of time so randomness has a chance to fail
    for (int numTrial = 0; numTrial < 100; numTrial++) {
      // create a number of parties with different numbers of candidates
      ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                         numCandidatesPerParty);

      // set the number of votes for each party
      OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);

      // run election
      OPL opl = new OPL(totalVotes, totalSeats, parties);
      opl.runVotingAlgorithm();

      // get the actual output
      String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");

      // find if the actual output matches one of the three possible expected outputs
      int match = -1; // -1 to signify match hasn't been found
      for (int i = 0; i < numTies; i++) {
        if (actualOutput.equals(expectedOutputs[i])) {
          match = i;
        }
      }
      String errorMessage = "The actual output did not match any of the expected outputs. "
                          + "The actual output was:\n";
      errorMessage.concat(actualOutput);
      assertNotEquals(-1, match, errorMessage);

      // find the new media and audit files
      String[] filenames = OPLTestHelpers.findAuditAndMedia();

      // get contents of media and audit files
      String actualAudit = OPLTestHelpers.getAllFileContents(filenames[1]);
      assertNotNull(actualAudit);
      String actualMedia = OPLTestHelpers.getAllFileContents(filenames[0]);
      assertNotNull(actualMedia);

      // compare output to audit file
      assertEquals(expectedAudits[match], actualAudit);

      // compare output to media file
      assertEquals(expectedMedias[match], actualMedia);

      // clear audit and media files
      // also clear system output buffer
      OPLTestHelpers.deleteElectionOutputFiles();
      systemOut.reset();
    }
  }

  /**
   * Tests to see if the output from running an OPL algorithm with a party tie
   * matches the expected output from a tie. This function is very similar to
   * testOPLTiedCandidatesExpectedOutput
   */
  @Test
  public void testOPLTiedPartiesExpectedOutput() {
    // basic info that wont need to reset each iteration
    int numParties = 4;
    int[] numCandidatesPerParty = {1, 1, 1, 2};

    // party 1, 2, and 3 will have a tie in remaining votes
    int[] partyVotes = {2000, 2000, 2000, 100};
    // don't need to initialize since we won't be using the candidate votes anyway in this test
    int[][] candidateVotes = new int[numParties][];
    for (int i = 0; i < partyVotes.length; i++) {
      candidateVotes[i] = new int[numCandidatesPerParty[i]];
    }

    int totalVotes = 0;
    for (int votes: partyVotes) {
      totalVotes += votes;
    }

    // other election information
    int totalSeats = 1;
    int numTies = 3;

    // get the 3 possible example output, audit, and media files
    String[] expectedOutputs = new String[numTies];
    String[] expectedAudits = new String[numTies];
    String[] expectedMedias = new String[numTies];
    for (int i = 0; i < numTies; i++) {
      expectedOutputs[i] = OPLTestHelpers.getAllFileContents(
              "testing/ExampleFiles/ExampleOPLOutput_TiedParties" + i + ".txt");
      assertNotNull(expectedOutputs[i]);
      expectedAudits[i] = OPLTestHelpers.getAllFileContents(
              "testing/ExampleFiles/ExampleOPLAudit_TiedParties" + i + ".txt");
      assertNotNull(expectedAudits[i]);
      expectedMedias[i] = OPLTestHelpers.getAllFileContents(
              "testing/ExampleFiles/ExampleOPLMedia_TiedParties" + i + ".txt");
      assertNotNull(expectedMedias[i]);
    }

    // repeat this test a lot of time so randomness has a chance to fail
    for (int numTrial = 0; numTrial < 100; numTrial++) {
      // create a number of parties with different numbers of candidates
      ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                         numCandidatesPerParty);

      // set the number of votes for each party
      OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);

      // run election
      OPL opl = new OPL(totalVotes, totalSeats, parties);
      opl.runVotingAlgorithm();


      // get the actual output
      String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");

      // find if the actual output matches one of the three possible expected outputs
      int match = -1; // -1 to signify match hasn't been found
      for (int i = 0; i < numTies; i++) {
        if (actualOutput.equals(expectedOutputs[i])) {
          match = i;
        }
      }
      String errorMessage = "The actual output did not match any of the expected outputs. "
                          + "The actual output was:\n";
      errorMessage.concat(actualOutput);
      assertNotEquals(-1, match, errorMessage);

      // find the new media and audit files
      String[] filenames = OPLTestHelpers.findAuditAndMedia();

      // get contents of media and audit files
      String actualAudit = OPLTestHelpers.getAllFileContents(filenames[1]);
      assertNotNull(actualAudit);
      String actualMedia = OPLTestHelpers.getAllFileContents(filenames[0]);
      assertNotNull(actualMedia);

      // compare output to audit file
      assertEquals(expectedAudits[match], actualAudit);

      // compare output to media file
      assertEquals(expectedMedias[match], actualMedia);

      // clear audit and media files
      // also clear system output buffer
      OPLTestHelpers.deleteElectionOutputFiles();
      systemOut.reset();
    }
  }

  /**
   * Tests to see if the output from running an OPL algorithm with a party tie
   * and candidate tie matches the expected output from a tie.
   * This function is very similar to testOPLTiedCandidatesExpectedOutput and
   * testOPLTiedPartiesExpectedOutput, but this one focuses on the case
   * where candidates tie in addition to a party tie.
   */
  @Test
  public void testOPLTiedPartiesAndTiedCandidatesExpectedOutput() {
    // basic info that wont need to reset each iteration
    int numParties = 3;
    int[] numCandidatesPerParty = {2, 2, 1};

    // party 1, 2 will have a tie in remaining votes
    int[] partyVotes = {2000, 2000, 100};
    // candidate 1 and 2 of parties 1 and 2 will tie if their party is chosen for the seat
    int[][] candidateVotes = {{1000, 1000}, {1000, 1000}, {100}};

    int totalVotes = 0;
    for (int votes: partyVotes) {
      totalVotes += votes;
    }

    // other election information
    int totalSeats = 1;
    int numTies = 4;

    // get the 4 possible example output, audit, and media files
    String[] expectedOutputs = new String[numTies];
    String[] expectedAudits = new String[numTies];
    String[] expectedMedias = new String[numTies];
    for (int i = 0; i < numTies; i++) {
      expectedOutputs[i] = OPLTestHelpers.getAllFileContents(
              "testing/ExampleFiles/ExampleOPLOutput_TiedCandidatesAndParties" + i + ".txt");
      assertNotNull(expectedOutputs[i]);
      expectedAudits[i] = OPLTestHelpers.getAllFileContents(
              "testing/ExampleFiles/ExampleOPLAudit_TiedCandidatesAndParties" + i + ".txt");
      assertNotNull(expectedAudits[i]);
      expectedMedias[i] = OPLTestHelpers.getAllFileContents(
              "testing/ExampleFiles/ExampleOPLMedia_TiedCandidatesAndParties" + i + ".txt");
      assertNotNull(expectedMedias[i]);
    }

    // repeat this test a lot of time so randomness has a chance to fail
    for (int numTrial = 0; numTrial < 100; numTrial++) {
      // create a number of parties with different numbers of candidates
      ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                         numCandidatesPerParty);

      // set the number of votes for each party
      OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);

      // run election
      OPL opl = new OPL(totalVotes, totalSeats, parties);
      opl.runVotingAlgorithm();


      // get the actual output
      String actualOutput = systemOut.toString().replaceAll("\n", "").replaceAll("\r", "");

      // find if the actual output matches one of the three possible expected outputs
      int match = -1; // -1 to signify match hasn't been found
      for (int i = 0; i < numTies; i++) {
        if (actualOutput.equals(expectedOutputs[i])) {
          match = i;
        }
      }
      String errorMessage = "The actual output did not match any of the expected outputs. "
                          + "The actual output was:\n";
      errorMessage.concat(actualOutput);
      assertNotEquals(-1, match, errorMessage);

      // find the new media and audit files
      String[] filenames = OPLTestHelpers.findAuditAndMedia();

      // get contents of media and audit files
      String actualAudit = OPLTestHelpers.getAllFileContents(filenames[1]);
      assertNotNull(actualAudit);
      String actualMedia = OPLTestHelpers.getAllFileContents(filenames[0]);
      assertNotNull(actualMedia);

      // compare output to audit file
      assertEquals(expectedAudits[match], actualAudit);

      // compare output to media file
      assertEquals(expectedMedias[match], actualMedia);

      // clear audit and media files
      // also clear system output buffer
      OPLTestHelpers.deleteElectionOutputFiles();
      systemOut.reset();
    }
  }
}
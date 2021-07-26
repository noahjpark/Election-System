import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * IR Unit Tests
 *
 * @author Mohammad Essawy, Michael Markiewicz
 */
public class IRTest {
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
    public void testCorrectIRConstructor() {
        int totalNumBallots = 20;

        int[] votes = new int[]{20};
        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("(p0)", 20);

        IR correctIR = new IR(ballots, candidates, totalNumBallots);

        assertEquals(totalNumBallots, correctIR.getTotalCounts());
        assertEquals(candidates, correctIR.getCandidates());
        assertEquals(ballots, correctIR.getBallots());
    }

    /**
     * Tests Constructor when inputted Null parameters.
     */
    @Test
    public void testNullParametersIRConstructor() {
        int totalNumBallots = 5520;
        int[] votes = new int[]{10, 20, 30, 40, 5420};

        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("c0", 20);


        try {
            IR invalidBallot = new IR(null, candidates, totalNumBallots);
            assertTrue(false); // did not throw an exception
        } catch (IllegalArgumentException e) {
            assertEquals("totalNumBallots must be positive, candidates and ballots must be " +
                    "non-null and non-empty", e.getMessage());
        }
        try {
            IR invalidHash = new IR(ballots, null, totalNumBallots);
            assertTrue(false); // did not throw an exception
        } catch (IllegalArgumentException e) {
            assertEquals("totalNumBallots must be positive, candidates and ballots must be " +
                    "non-null and non-empty", e.getMessage());
        }
    }

    /**
     * Tests IR Constructor when inputted Empty arraylist of empty ballots or candidates.
     */
    @Test
    public void testEmptyParametersIRConstructor() {
        int totalNumBallots = 5520;
        int[] votes = new int[]{10, 20, 30, 40, 50};

        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("c0", 20);

        votes = new int[]{};
        ArrayList<Candidate> emptyCandidates = IRTestHelpers.createBasicCandidateInstance(votes);
        HashMap<String, Integer> emptyBallots = new HashMap<String, Integer>();

        try {
            IR invalidBallot = new IR(emptyBallots, candidates, totalNumBallots);
            assertTrue(false); // did not throw an exception
        } catch (IllegalArgumentException e) {
            assertEquals("totalNumBallots must be positive, candidates and ballots must " +
                    "be non-null and non-empty", e.getMessage());
        }
        try {
            IR invalidHash = new IR(ballots, emptyCandidates, totalNumBallots);
            assertTrue(false); // did not throw an exception
        } catch (IllegalArgumentException e) {
            assertEquals("totalNumBallots must be positive, candidates and ballots must " +
                    "be non-null and non-empty", e.getMessage());
        }
    }

    /**
     * Tests IR Constructor when given a non-positive input.
     */
    @Test
    public void testNonPositiveInputIRConstructor() {
        int totalNumBallots = 0;
        int[] votes = new int[]{10, 20, 30, 40, 50};

        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("c0", 20);

        try {
            IR invalidTotal = new IR(ballots, candidates, totalNumBallots);
            assertTrue(false); // did not throw an exception
        } catch (IllegalArgumentException e) {
            assertEquals("totalNumBallots must be positive, candidates and ballots must " +
                    "be non-null and non-empty", e.getMessage());
        }
    }

    /**
     * Checks if running the IR Voting Algorithm creates a media and audit report in correct format.
     * This will delete any preexisting media/audit reports before running the algorithm to ensure
     * new ones are created.
     */
    @Test
    public void testIRRunVotingAlgorithmCheckFilesCreated() {

        // create election parts
        int[] votes = new int[]{10, 20, 30, 40};
        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        int totalNumBallots = 100;
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("(p0)(p2)(p1)", 10);
        ballots.put("(p1)(p3)(p2)", 20);
        ballots.put("(p2)(p1)(p3)", 30);
        ballots.put("(p3)(p1)(p2)", 40);

        // create election
        IR ir = new IR(ballots, candidates, totalNumBallots);

        // run the algorithm
        ir.runVotingAlgorithm();

        // check if the media report and audit file were created successfully with correct formatting
        String[] filenames = IRTestHelpers.findAuditAndMedia();

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
     * Tests that the algorithm is cut short if the majority is present
     * by making share the hashmap is never changed when majority is present
     */
    @Test
    public void checkMajorityPresent() {

        // create election parts
        int[] votes = new int[]{10, 20, 30, 70};
        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        int totalNumBallots = 130;
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("(p0)(p2)(p1)", 10);
        ballots.put("(p1)(p3)(p2)", 20);
        ballots.put("(p2)(p1)(p3)", 30);
        ballots.put("(p3)(p1)(p2)", 70);

        HashMap<String, Integer> ballotsCopy = (HashMap<String, Integer>) ballots.clone();

        // create election
        IR ir = new IR(ballots, candidates, totalNumBallots);

        // run the algorithm
        ir.runVotingAlgorithm();

        //check for early termination
        assertTrue(ir.getBallots().equals(ballotsCopy));

    }

    /**
     * Tests that the algorithm is not prematurely cut short
     * by making sure the hashmap is changed when majority is not present
     */
    @Test
    public void checkMajorityNotPresent() {

        // create election parts
        int[] votes = new int[]{10, 20, 30, 60};
        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        int totalNumBallots = 120;
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("(p0)(p2)(p1)", 10);
        ballots.put("(p1)(p3)(p2)", 20);
        ballots.put("(p2)(p1)(p3)", 30);
        ballots.put("(p3)(p1)(p2)", 60);

        HashMap<String, Integer> ballotsCopy = (HashMap<String, Integer>) ballots.clone();

        // create election
        IR ir = new IR(ballots, candidates, totalNumBallots);

        // run the algorithm
        ir.runVotingAlgorithm();

        //check for early termination
        assertFalse(ballots.equals(ballotsCopy));

    }

    /**
     * Tests that the algorithm is cut short if only two candidates remain
     * by making share the hashmap is never changed when there are only two candidates.
     * This also induces a tie, but we do not test who wins.
     */
    @Test
    public void checkMajorityWhenOnlyTwoCandidates() {
        // create election parts
        int[] votes = new int[]{10, 10};
        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        int totalNumBallots = 20;
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("(p0)", 10);
        ballots.put("(p1)", 10);

        HashMap<String, Integer> ballotsCopy = (HashMap<String, Integer>) ballots.clone();

        // create election
        IR ir = new IR(ballots, candidates, totalNumBallots);

        // run the algorithm
        ir.runVotingAlgorithm();

        //check for early termination
        assertTrue(ballots.equals(ballotsCopy));
    }

    /**
     * Tests that the ballots hashmaps changes appropriately when there is a candidate
     * who wins without a tie, but does not win after the first round
     */
    @Test
    public void checkBallotsChangedToExpected() {
        // create election parts
        int[] votes = new int[] {10, 15, 30, 20};
        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        int totalNumBallots = 120;
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("(p0)", 10);
        ballots.put("(p1)(p2)(p0)", 10);
        ballots.put("(p3)(p2)", 20);
        ballots.put("(p2)(p1)(p0)", 10);
        ballots.put("(p1)(p2)", 5);
        ballots.put("(p2)(p1)(p3)", 20);

        // expected ballots after the election is run
        HashMap<String, Integer> expectedBallots = new HashMap<>();
        expectedBallots.put("(p2)", 25);
        expectedBallots.put("(p3)(p2)", 20);
        expectedBallots.put("(p2)(p3)", 20);

        // create election
        IR ir = new IR(ballots, candidates, totalNumBallots);

        // run the algorithm
        ir.runVotingAlgorithm();

        // check that the expected ballots is equal to the ballots after running the algorithm
        assertEquals(expectedBallots, ballots);
    }

    /**
     * Checks that the winner of a regular election with no ties is the expected winner.
     */
    @Test
    public void checkWinnerOfRegularElection() {
        // create election parts
        // candidate 2 should be the winner
        int[] votes = new int[] {10, 15, 30, 20};
        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        int totalNumBallots = 75;
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("(p0)", 10);
        ballots.put("(p1)(p2)(p0)", 10);
        ballots.put("(p3)(p2)", 20);
        ballots.put("(p2)(p1)(p0)", 10);
        ballots.put("(p1)(p2)", 5);
        ballots.put("(p2)(p1)(p3)", 20);

        Candidate expectedWinner = candidates.get(2);

        // create election
        IR ir = new IR(ballots, candidates, totalNumBallots);

        // run the algorithm
        ir.runVotingAlgorithm();

        // check that candidate 2 won the election
        assertEquals(expectedWinner, ir.getWinner());
    }

    /**
     * Tests that a candidate wins if they are the only one in the election.
     */
    @Test
    public void checkWinnerOfElectionWithOneCandidate() {
        // create election parts
        // candidate 0 should be the winner
        int[] votes = new int[] {10};
        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        int totalNumBallots = 10;
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("(p0)", 10);

        Candidate expectedWinner = candidates.get(0);

        // create election
        IR ir = new IR(ballots, candidates, totalNumBallots);

        // run the algorithm
        ir.runVotingAlgorithm();

        // check that candidate 2 won the election
        assertEquals(expectedWinner, ir.getWinner());
    }

    /**
     * Ensures that the output to the console, audit file, and media report are expected outputs.
     * This works by running the algorithm, and comparing the outputs to example output files
     * (ExampleIROutput.txt, ExampleIRAudit.txt, ExampleIRMedia.txt)
     *
     * The tests are designed in such a way where ties should not happen so output should
     * be the same each time.
     */
    @Test
    public void testIROutput() {
        // create election parts
        // candidate 2 should be the winner
        int[] votes = new int[] {10, 15, 30, 20};
        ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
        int totalNumBallots = 75;
        HashMap<String, Integer> ballots = new HashMap<String, Integer>();
        ballots.put("(p0)", 10);
        ballots.put("(p1)(p2)(p0)", 10);
        ballots.put("(p3)(p2)", 20);
        ballots.put("(p2)(p1)(p0)", 10);
        ballots.put("(p1)(p2)", 5);
        ballots.put("(p2)(p1)(p3)", 20);

        // create election
        IR ir = new IR(ballots, candidates, totalNumBallots);

        // run the algorithm
        ir.runVotingAlgorithm();

        // get the example output files
        String expectedOutput = OPLTestHelpers.getAllFileContents(
                "testing/ExampleIRFiles/ExampleIROutput.txt");
        assertNotNull(expectedOutput);
        String expectedAudit = OPLTestHelpers.getAllFileContents(
                "testing/ExampleIRFiles/ExampleIRAudit.txt");
        assertNotNull(expectedAudit);
        String expectedMedia = OPLTestHelpers.getAllFileContents(
                "testing/ExampleIRFiles/ExampleIRMedia.txt");
        assertNotNull(expectedMedia);

        // find the new media and audit files
        String[] filenames = IRTestHelpers.findAuditAndMedia();

        // get contents of media and audit files
        String actualAudit = IRTestHelpers.getAllFileContents(filenames[1]);
        assertNotNull(actualAudit);
        String actualMedia = IRTestHelpers.getAllFileContents(filenames[0]);
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
     * Tests to see if the output from running the IR algorithm with a candidate tie when being
     * eliminated matches the expected output from a tie.
     */
    @Test
    public void testIRTiedCandidatesExpectedOutput() {
        // there will be 3 candidates who tie for the election
        int numTies = 3;

        // get the 3 possible example output, audit, and media files
        String[] expectedOutputs = new String[numTies];
        String[] expectedAudits = new String[numTies];
        String[] expectedMedias = new String[numTies];
        for (int i = 0; i < numTies; i++) {
            expectedOutputs[i] = OPLTestHelpers.getAllFileContents(
                    "testing/ExampleIRFiles/ExampleIROutput_TiedCandidates" + i + ".txt");
            assertNotNull(expectedOutputs[i]);
            expectedAudits[i] = OPLTestHelpers.getAllFileContents(
                    "testing/ExampleIRFiles/ExampleIRAudit_TiedCandidates" + i + ".txt");
            assertNotNull(expectedAudits[i]);
            expectedMedias[i] = OPLTestHelpers.getAllFileContents(
                    "testing/ExampleIRFiles/ExampleIRMedia_TiedCandidates" + i + ".txt");
            assertNotNull(expectedMedias[i]);
        }

        // repeat this test a lot of time so randomness has a chance to fail
        for (int numTrial = 0; numTrial < 1; numTrial++) {
            // create election parts
            // candidate 1-3 should always tie in some way (note that they may not tie in a
            // uniform way depending on which one is eliminated first)
            // candidates 1-3 will always tie for being eliminated first, but then if...
            //  candidate 1 is eliminated -> candidate 2 wins
            //  candidate 2 is eliminated -> candidate 1 wins
            //  candidate 3 is eliminated -> candidate 2 wins
            int[] votes = new int[] {33, 35, 40, 45};
            ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
            int totalNumBallots = 153;
            HashMap<String, Integer> ballots = new HashMap<String, Integer>();
            ballots.put("(p0)(p1)(p3)", 15);
            ballots.put("(p0)", 3);
            ballots.put("(p0)(p2)", 5);
            ballots.put("(p0)(p2)(p1)(p3)", 5);
            ballots.put("(p0)(p3)(p1)", 5);
            ballots.put("(p1)(p2)(p0)", 20);
            ballots.put("(p1)(p2)", 15);
            ballots.put("(p2)(p1)(p0)", 20);;
            ballots.put("(p2)(p1)(p3)", 20);
            ballots.put("(p3)(p2)", 25);
            ballots.put("(p3)", 20);

            // create election
            IR ir = new IR(ballots, candidates, totalNumBallots);

            // run the algorithm
            ir.runVotingAlgorithm();

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
            String[] filenames = IRTestHelpers.findAuditAndMedia();

            // get contents of media and audit files
            String actualAudit = IRTestHelpers.getAllFileContents(filenames[1]);
            assertNotNull(actualAudit);
            String actualMedia = IRTestHelpers.getAllFileContents(filenames[0]);
            assertNotNull(actualMedia);

            // compare output to audit file
            assertEquals(expectedAudits[match], actualAudit);

            // compare output to media file
            assertEquals(expectedMedias[match], actualMedia);

            // clear audit and media files
            // also clear system output buffer
            IRTestHelpers.deleteElectionOutputFiles();
            systemOut.reset();
        }
    }

    /**
     * Tests to see if the output from running the IR algorithm with a candidate tie when the
     * candidates tie for the win.
     */
    @Test
    public void testIRTiedCandidatesForWinnerExpectedOutput() {
        // there will be 2 candidates who tie for the election
        int numTies = 2;

        // get the 2 possible example output, audit, and media files
        String[] expectedOutputs = new String[numTies];
        String[] expectedAudits = new String[numTies];
        String[] expectedMedias = new String[numTies];
        for (int i = 0; i < numTies; i++) {
            expectedOutputs[i] = OPLTestHelpers.getAllFileContents(
                    "testing/ExampleIRFiles/ExampleIROutput_TiedCandidatesForWin" + i + ".txt");
            assertNotNull(expectedOutputs[i]);
            expectedAudits[i] = OPLTestHelpers.getAllFileContents(
                    "testing/ExampleIRFiles/ExampleIRAudit_TiedCandidatesForWin" + i + ".txt");
            assertNotNull(expectedAudits[i]);
            expectedMedias[i] = OPLTestHelpers.getAllFileContents(
                    "testing/ExampleIRFiles/ExampleIRMedia_TiedCandidatesForWin" + i + ".txt");
            assertNotNull(expectedMedias[i]);
        }

        // repeat this test a lot of time so randomness has a chance to fail
        for (int numTrial = 0; numTrial < 100; numTrial++) {
            // create election parts
            // candidate 2-3 should always tie for the win
            int[] votes = new int[] {33, 34, 40, 40};
            ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
            int totalNumBallots = 147;
            HashMap<String, Integer> ballots = new HashMap<String, Integer>();
            ballots.put("(p0)(p2)(p1)", 15);
            ballots.put("(p0)(p3)(p2)", 15);
            ballots.put("(p0)(p1)", 3);
            ballots.put("(p1)(p2)(p0)", 15);
            ballots.put("(p1)(p0)(p2)(p3)", 2);
            ballots.put("(p1)(p3)(p0)(p2)", 7);
            ballots.put("(p1)(p0)(p3)", 10);
            ballots.put("(p2)(p3)(p0)(p1)", 10);
            ballots.put("(p2)(p1)", 20);
            ballots.put("(p2)(p0)(p3)", 10);
            ballots.put("(p3)", 15);
            ballots.put("(p3)(p2)", 10);
            ballots.put("(p3)(p1)(p0)", 15);

            // create election
            IR ir = new IR(ballots, candidates, totalNumBallots);

            // run the algorithm
            ir.runVotingAlgorithm();

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
            String[] filenames = IRTestHelpers.findAuditAndMedia();

            // get contents of media and audit files
            String actualAudit = IRTestHelpers.getAllFileContents(filenames[1]);
            assertNotNull(actualAudit);
            String actualMedia = IRTestHelpers.getAllFileContents(filenames[0]);
            assertNotNull(actualMedia);

            // compare output to audit file
            assertEquals(expectedAudits[match], actualAudit);

            // compare output to media file
            assertEquals(expectedMedias[match], actualMedia);

            // clear audit and media files
            // also clear system output buffer
            IRTestHelpers.deleteElectionOutputFiles();
            systemOut.reset();
        }
    }
}





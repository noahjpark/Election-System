import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * This class is in charge of reading a processing
 * the ballot CSV files. It should process the
 * ballot CSV and return the correct election type.
 * This class will exit the program if there is a problem
 * Identifying the ballot file or the ballot file is
 * incorrectly formatted.
 *
 * @author John Foley
 */
public class CreateElectionTest {
    private final ByteArrayOutputStream systemOut = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    final private String testingPath = "./testing/testFiles/";
    final private String invalidElectionTypeFilePath = testingPath + "invalidElectionType.csv";
    final private String givenIRFilePath = testingPath + "givenIR.csv";
    final private String givenOPLFilePath = testingPath + "givenOPL.csv";
    final private String bigIRFilePath = testingPath + "bigIR.csv";
    final private String bigOPLFilePath = testingPath + "bigOPL.csv";
    final private String oneCandidateIRFilePath = testingPath + "oneCandidateIR.csv";
    final private String oneCandidateOPLFilePath = testingPath + "oneCandidateOPL.csv";

    /**
     * Helper function to compare the equality of two ArrayLists of candidates.
     * Note this checks only the full equality before the runElection() function
     * is called
     *
     * @param testCandidates The expected candidates
     * @param realCandidates The actual list of candidates generated
     */
    public void canidateArraylistIsEqual(ArrayList<Candidate> testCandidates, ArrayList<Candidate> realCandidates) {
        assertEquals(testCandidates.size(), realCandidates.size());
        for (int i = 0; i < testCandidates.size(); i++) {
            assertEquals(testCandidates.get(i).getName(), realCandidates.get(i).getName());
            assertEquals(testCandidates.get(i).getParty(), realCandidates.get(i).getParty());
            assertEquals(testCandidates.get(i).getCandidateID(), realCandidates.get(i).getCandidateID());
            assertEquals(testCandidates.get(i).getCurNumVotes(), realCandidates.get(i).getCurNumVotes());
        }
    }


    /**
     * Helper function to compare the equality of two Array lists of parties
     * Note this checks only the full equality before the runElection() function
     * is called
     *
     * @param testParty The expected Party
     * @param realParty The actual list of Parties generated
     */
    public void partyArraylistIsEqual(ArrayList<Party> testParty, ArrayList<Party> realParty) {
        assertEquals(testParty.size(), realParty.size());
        for (int i = 0; i < testParty.size(); i++) {
            canidateArraylistIsEqual(testParty.get(i).getCandidates(), realParty.get(i).getCandidates());
            assertEquals(testParty.get(i).getTotalVotes(), realParty.get(i).getTotalVotes());
        }

    }

    /**
     * Sets the standard output to a different stream so no output to the terminal happens during
     * unit tests.
     */
    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(systemOut));
    }

    /**oo
     * Resets the standard output to stdout
     */
    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }


    /**
     * Tests that null is returned if the file specified does not exist
     * Also tests the error message is correct
     */
    @Test
    public void testFileNotFound() {
        Election nullElection = CreateElection.createElection("/not/a/real/file/path");
        assertEquals(null, nullElection);
        String actualOutput = systemOut.toString().replaceAll("[\n\r]", "");
        String testOutput = "Error: File Not Found";
        assertEquals(testOutput, actualOutput);
    }

    /**
     * Tests that null is returned if the file specifies an invalid
     * Election type
     * Also tests the error message is correct
     */
    @Test
    public void testInvalidElectionType() {
        Election nullElection = CreateElection.createElection(invalidElectionTypeFilePath);
        assertEquals(null, nullElection);
        String actualOutput = systemOut.toString().replaceAll("[\n\r]", "");
        String testOutput = "Error: Invalid Election Type";
        assertEquals(testOutput, actualOutput);
    }

    /**
     * Tests if createElection returns is an instance
     * of IR if the CSV file is for an IR election
     */
    @Test
    public void testCreateIRElectionObject() {
        Election testIR = CreateElection.createElection(givenIRFilePath);
        assertTrue(testIR instanceof IR);
    }

    /**
     * Tests if createElection returns is an instance
     * of OPL if the CSV file is for an OPL election
     */
    @Test
    public void testCreateOPLElectionObject() {
        Election testOPL = CreateElection.createElection(givenOPLFilePath);
        assertTrue(testOPL instanceof OPL);
    }


    /**
     * Compares the expected IR object from the only IR file
     * given to us in the instructions to the actual IR object
     * generated
     */
    @Test()
    public void testParametersIRGivenFile() {
        Election testIR = CreateElection.createElection(givenIRFilePath);
        assertTrue(testIR instanceof IR);
        Class<?> spyIR = testIR.getClass();
        Field fields[] = spyIR.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                switch (field.getName()) {
                    case "ballots":
                        HashMap<String, Integer> testBallotCounter = new HashMap<>();
                        testBallotCounter.put("(D)(I)", 1);
                        testBallotCounter.put("(D)(L)(R)(I)", 1);
                        testBallotCounter.put("(D)(R)(I)", 1);
                        testBallotCounter.put("(I)(R)(D)(L)", 1);
                        testBallotCounter.put("(I)(L)", 1);
                        testBallotCounter.put("(L)", 1);
                        assertEquals(testBallotCounter, field.get(testIR));
                        continue;
                    case "candidates":
                        ArrayList<Candidate> testCandidates = new ArrayList<>();
                        Candidate rosenCandidate = new Candidate("Rosen", "D", 0);
                        rosenCandidate.setCurNumVotes(3);
                        testCandidates.add(rosenCandidate);
                        testCandidates.add(new Candidate("Kleinberg", "R", 1));
                        Candidate chouCandidate = new Candidate("Chou", "I", 2);
                        chouCandidate.setCurNumVotes(2);
                        testCandidates.add(chouCandidate);
                        Candidate royceCandidate = new Candidate("Royce", "L", 3);
                        royceCandidate.setCurNumVotes(1);
                        testCandidates.add(royceCandidate);
                        ArrayList<Candidate> realCandidates = (ArrayList<Candidate>) field.get(testIR);
                        canidateArraylistIsEqual(testCandidates, realCandidates);
                        continue;
                    case "totalCounts":
                        assertEquals(field.get(testIR), 6);
                }
            } catch (IllegalAccessException e) {

            }
        }
    }

    /**
     * Compares the expected OPL object from the only OPL file
     * given to us in the instructions to the actual OPL object
     * generated
     */
    @Test
    public void testParametersOPLGivenFile() {
        Election testOPL = CreateElection.createElection(givenOPLFilePath);
        assertTrue(testOPL instanceof OPL);
        Class<?> spyOPL = testOPL.getClass();
        Field fields[] = spyOPL.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                switch (field.getName()) {
                    case "numSeatsAvailable":
                        assertEquals(field.get(testOPL), 3);
                        continue;
                    case "numSeatsLeft":
                        assertEquals(field.get(testOPL), 3);
                        continue;
                    case "parties":
                        ArrayList<Party> actualParty = (ArrayList<Party>) field.get(testOPL);
                        ArrayList<Party> testParty = new ArrayList<>();

                        Candidate pikeCandidate = new Candidate("Pike", "D", 0);
                        pikeCandidate.setCurNumVotes(3);

                        Candidate fosterCandidate = new Candidate("Foster", "D", 1);
                        fosterCandidate.setCurNumVotes(2);

                        Party dParty = new Party("D");
                        dParty.addCandidate(pikeCandidate);
                        dParty.addCandidate(fosterCandidate);
                        dParty.setTotalVotes(5);
                        testParty.add(dParty);

                        Candidate deutschCandidate = new Candidate("Deutsch", "R", 2);
                        deutschCandidate.setCurNumVotes(0);

                        Candidate borgCandidate = new Candidate("Borg", "R", 3);
                        borgCandidate.setCurNumVotes(2);

                        Candidate jonesCandidate = new Candidate("Jones", "R", 4);
                        jonesCandidate.setCurNumVotes(1);

                        Party rParty = new Party("R");
                        rParty.addCandidate(deutschCandidate);
                        rParty.addCandidate(borgCandidate);
                        rParty.addCandidate(jonesCandidate);
                        rParty.setTotalVotes(3);
                        testParty.add(rParty);

                        Candidate smithCandidate = new Candidate("Smith", "I", 5);
                        smithCandidate.setCurNumVotes(1);

                        Party iParty = new Party("I");
                        iParty.addCandidate(smithCandidate);
                        iParty.setTotalVotes(1);
                        testParty.add(iParty);

                        partyArraylistIsEqual(testParty, actualParty);
                        continue;
                    case "quota":
                        assertEquals(field.get(testOPL), 3);
                        continue;
                }
            } catch (IllegalAccessException e) {

            }
        }
    }

    /**
     * Tests if a create election can process a 100,000
     * ballot IR file in less than 30 seconds. It also compares
     * the expected IR object ot the actual IR object
     */
    @Test()
    public void testReadBigIR() {
        Election testIR = assertTimeout(ofMillis(30000), () -> {
            return CreateElection.createElection(bigIRFilePath);
        });
        assertTrue(testIR instanceof IR);
        Class<?> spyIR = testIR.getClass();
        Field fields[] = spyIR.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                switch (field.getName()) {
                    case "ballots":
                        HashMap<String, Integer> testBallotCounter = new HashMap<>();
                        testBallotCounter.put("(D)(R)(I)(L)", 50000);
                        testBallotCounter.put("(L)(I)(R)(D)", 50000);
                        assertEquals(testBallotCounter, field.get(testIR));
                        continue;
                    case "candidates":
                        ArrayList<Candidate> testCandidates = new ArrayList<>();
                        //Have to make candidate objects for those we want to set votes of
                        Candidate rosenCandidate = new Candidate("Rosen", "D", 0);
                        rosenCandidate.setCurNumVotes(50000);
                        testCandidates.add(rosenCandidate);
                        testCandidates.add(new Candidate("Kleinberg", "R", 1));
                        testCandidates.add(new Candidate("Chou", "I", 2));
                        Candidate royceCandidate = new Candidate("Royce", "L", 3);
                        royceCandidate.setCurNumVotes(50000);
                        testCandidates.add(royceCandidate);
                        ArrayList<Candidate> realCandidates = (ArrayList<Candidate>) field.get(testIR);
                        canidateArraylistIsEqual(testCandidates, realCandidates);
                        continue;
                    case "totalCounts":
                        assertEquals(field.get(testIR), 100000);
                }
            } catch (IllegalAccessException e) {

            }
        }
    }

    /**
     * Tests if a create election can process a 100,000
     * ballot OPL file in less than 30 seconds. It also compares
     * the expected OPL object ot the actual OPL object
     */
    @Test()
    public void testReadBigOPL() {
        Election testOPL = assertTimeout(ofMillis(30000), () -> {
            return CreateElection.createElection(bigOPLFilePath);
        });
        assertTrue(testOPL instanceof OPL);

        Class<?> spyOPL = testOPL.getClass();
        Field fields[] = spyOPL.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                switch (field.getName()) {
                    case "numSeatsAvailable":
                        assertEquals(field.get(testOPL), 3);
                        continue;
                    case "numSeatsLeft":
                        assertEquals(field.get(testOPL), 3);
                        continue;
                    case "parties":
                        ArrayList<Party> actualParty = (ArrayList<Party>) field.get(testOPL);
                        ArrayList<Party> testParty = new ArrayList<>();

                        Candidate pikeCandidate = new Candidate("Pike", "D", 0);
                        pikeCandidate.setCurNumVotes(50000);

                        Candidate fosterCandidate = new Candidate("Foster", "D", 1);

                        Party dParty = new Party("D");
                        dParty.addCandidate(pikeCandidate);
                        dParty.addCandidate(fosterCandidate);
                        dParty.setTotalVotes(50000);
                        testParty.add(dParty);

                        Candidate deutschCandidate = new Candidate("Deutsch", "R", 2);
                        Candidate borgCandidate = new Candidate("Borg", "R", 3);
                        Candidate jonesCandidate = new Candidate("Jones", "R", 4);

                        Party rParty = new Party("R");
                        rParty.addCandidate(deutschCandidate);
                        rParty.addCandidate(borgCandidate);
                        rParty.addCandidate(jonesCandidate);
                        rParty.setTotalVotes(0);
                        testParty.add(rParty);

                        Candidate smithCandidate = new Candidate("Smith", "I", 5);
                        smithCandidate.setCurNumVotes(50000);

                        Party iParty = new Party("I");
                        iParty.addCandidate(smithCandidate);
                        iParty.setTotalVotes(50000);
                        testParty.add(iParty);

                        partyArraylistIsEqual(testParty, actualParty);
                        continue;
                    case "quota":
                        assertEquals(field.get(testOPL), 33333);
                        continue;
                }
            } catch (IllegalAccessException e) {

            }
        }
    }

    /**
     * Tests the edge case of having a single candidate.
     * Verifies the file is read correctly and that the expected
     * IR object is the same as the actual IR object
     */
    @Test
    public void testParametersIROneCandidate() {
        Election testIR = CreateElection.createElection(oneCandidateIRFilePath);
        assertTrue(testIR instanceof IR);
        Class<?> spyIR = testIR.getClass();
        Field fields[] = spyIR.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                switch (field.getName()) {
                    case "ballots":
                        HashMap<String, Integer> testBallotCounter = new HashMap<>();
                        testBallotCounter.put("(D)", 1);
                        assertEquals(testBallotCounter, field.get(testIR));
                        continue;
                    case "candidates":
                        ArrayList<Candidate> testCandidates = new ArrayList<>();
                        Candidate rosenCandidate = new Candidate("Rosen", "D", 0);
                        rosenCandidate.setCurNumVotes(1);
                        testCandidates.add(rosenCandidate);
                        ArrayList<Candidate> realCandidates = (ArrayList<Candidate>) field.get(testIR);
                        canidateArraylistIsEqual(testCandidates, realCandidates);
                        continue;
                    case "totalCounts":
                        assertEquals(field.get(testIR), 1);
                }
            } catch (IllegalAccessException e) {

            }
        }
    }

    /**
     * Tests the edge case of having a single candidate.
     * Verifies the file is read correctly and that the expected
     * OPL object is the same as the actual OPL object
     */
    @Test
    public void testParametersOPLOneCandidate() {
        Election testOPL = CreateElection.createElection(oneCandidateOPLFilePath);
        assertTrue(testOPL instanceof OPL);
        Class<?> spyOPL = testOPL.getClass();
        Field fields[] = spyOPL.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                switch (field.getName()) {
                    case "numSeatsAvailable":
                        assertEquals(field.get(testOPL), 1);
                        continue;
                    case "numSeatsLeft":
                        assertEquals(field.get(testOPL), 1);
                        continue;
                    case "parties":
                        ArrayList<Party> actualParty = (ArrayList<Party>) field.get(testOPL);
                        ArrayList<Party> testParty = new ArrayList<>();
                        Candidate pikeCandidate = new Candidate("Pike", "D", 0);
                        pikeCandidate.setCurNumVotes(1);
                        Party dParty = new Party("D");
                        dParty.addCandidate(pikeCandidate);
                        dParty.setTotalVotes(1);
                        testParty.add(dParty);
                        partyArraylistIsEqual(testParty, actualParty);
                        continue;
                    case "quota":
                        assertEquals(field.get(testOPL), 1);
                        continue;
                }
            } catch (IllegalAccessException e) {
            }
        }
    }
}

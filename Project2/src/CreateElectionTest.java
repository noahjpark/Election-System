import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;


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
    final private String IRSplitFile1FilePath = testingPath + "IRSplitFile1.csv";
    final private String IRSpiltFile2FilePath = testingPath + "IRSplitFile2.csv";
    final private String multipleOPLFilesPath = testingPath + "MultipleOPLFiles";
    final private String multipleIRFilesPath = testingPath + "MultipleIRFiles";
    final private String noSeatsOPLPath = testingPath + "noSeatsOPL.csv";
    final private String differentElectionTypesPath = testingPath + "DifferentElectionTypesFiles";
    final private String noBallotsOPL = testingPath + "OPLNoBallots.csv";
    final private String invalidBallotsFilePath = testingPath + "invalidBallotsIR.csv";
    final private String noInvalidBallotsFilePath = testingPath + "noInvalidBallotsIR.csv";

    /**
     * Helper function to compare the equality of two ArrayLists of candidates.
     * Note this checks only the full equality before the runElection() function
     * is called
     *
     * @param testCandidates The expected candidates
     * @param realCandidates The actual list of candidates generated
     */
    public static void candidateArraylistIsEqual(ArrayList<Candidate> testCandidates,
                                           ArrayList<Candidate> realCandidates) {
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
    public static void partyArraylistIsEqual(ArrayList<Party> testParty,
                                            ArrayList<Party> realParty) {
        assertEquals(testParty.size(), realParty.size());
        for (int i = 0; i < testParty.size(); i++) {
            candidateArraylistIsEqual(testParty.get(i).getCandidates(), realParty.get(i).getCandidates());
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
        // delete the files before so there are only one set of Invalidated files after running algorithm
        CreateElectionTestHelpers.deleteElectionOutputFiles();
    }

    /**
     * Resets the standard output to stdout
     */
    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        // delete the files so there are not a bunch of them floating around
        CreateElectionTestHelpers.deleteElectionOutputFiles();
    }


    /**
     * Tests that null is returned if the file specified does not exist
     * Also tests the error message is correct
     */
    @Test
    public void testFileNotFound() {
        Election nullElection = CreateElection.createElection(new String[] {"/not/a/real/file" +
                "/path"});
        assertEquals(null, nullElection);
        String actualOutput = systemOut.toString().replaceAll("[\n\r]", "");
        String testOutput = "Error: File (/not/a/real/file/path) Not Found";
        assertEquals(testOutput, actualOutput);
    }

    /**
     * Tests that null is returned if the file specifies an invalid
     * Election type
     * Also tests the error message is correct
     */
    @Test
    public void testInvalidElectionType() {
        Election nullElection =
                CreateElection.createElection(new String[] {invalidElectionTypeFilePath});
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
        Election testIR = CreateElection.createElection(new String[] {givenIRFilePath});
        assertTrue(testIR instanceof IR);
    }

    /**
     * Tests if createElection returns is an instance
     * of OPL if the CSV file is for an OPL election
     */
    @Test
    public void testCreateOPLElectionObject() {
        Election testOPL = CreateElection.createElection(new String[] {givenOPLFilePath});
        assertTrue(testOPL instanceof OPL);
    }


    /**
     * Compares the expected IR object from the only IR file
     * given to us in the instructions to the actual IR object
     * generated
     */
    @Test()
    public void testParametersIRGivenFile() {
        Election testIR = CreateElection.createElection(new String[] {givenIRFilePath});
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
                        testCandidates.add(royceCandidate);
                        ArrayList<Candidate> realCandidates = (ArrayList<Candidate>) field.get(testIR);
                        candidateArraylistIsEqual(testCandidates, realCandidates);
                        continue;
                    case "totalCounts":
                        assertEquals(field.get(testIR), 5);
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
        Election testOPL = CreateElection.createElection(new String[] {givenOPLFilePath});
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
            return CreateElection.createElection(new String[] {bigIRFilePath});
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
                        candidateArraylistIsEqual(testCandidates, realCandidates);
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
            return CreateElection.createElection(new String[] {bigOPLFilePath});
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
        Election testIR = CreateElection.createElection(new String[] {oneCandidateIRFilePath});
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
                        candidateArraylistIsEqual(testCandidates, realCandidates);
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
        Election testOPL = CreateElection.createElection(new String[] {oneCandidateOPLFilePath});
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


    /**
     * Test whether multiple files work on IR with no ballots removed
     */
    @Test
    public void testParametersIRSplitFiles() {
        Election testIR = CreateElection.createElection(new String[] {IRSplitFile1FilePath,IRSpiltFile2FilePath});

        assertTrue(testIR instanceof IR);
        Class<?> spyIR = testIR.getClass();
        Field fields[] = spyIR.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                switch (field.getName()) {
                    case "ballots":
                        HashMap<String, Integer> testBallotCounter = new HashMap<>();
                        testBallotCounter.put("(D)(L)(R)(I)", 1);
                        testBallotCounter.put("(D)(R)(I)(L)", 1);
                        testBallotCounter.put("(I)(R)(D)", 1);
                        testBallotCounter.put("(L)(D)(I)(R)", 1);
                        assertEquals(testBallotCounter, field.get(testIR));
                        continue;
                    case "candidates":
                        ArrayList<Candidate> testCandidates = new ArrayList<>();
                        Candidate rosenCandidate = new Candidate("Rosen", "D", 0);
                        rosenCandidate.setCurNumVotes(2);
                        testCandidates.add(rosenCandidate);
                        testCandidates.add(new Candidate("Kleinberg", "R", 1));
                        Candidate chouCandidate = new Candidate("Chou", "I", 2);
                        chouCandidate.setCurNumVotes(1);
                        testCandidates.add(chouCandidate);
                        Candidate royceCandidate = new Candidate("Royce", "L", 3);
                        royceCandidate.setCurNumVotes(1);
                        testCandidates.add(royceCandidate);
                        ArrayList<Candidate> realCandidates = (ArrayList<Candidate>) field.get(testIR);
                        candidateArraylistIsEqual(testCandidates, realCandidates);
                        continue;
                    case "totalCounts":
                        assertEquals(field.get(testIR), 4);
                }
            } catch (IllegalAccessException e) {

            }
        }
        String[] filenames = CreateElectionTestHelpers.findInvalidated();
        String actualInvalidated = OPLTestHelpers.getAllFileContents(filenames[0]);
        assertEquals("",actualInvalidated);
    }

    /**
     * Test whether multiple files work on IR with multiple ballots removed
     */
    @Test
    public void testParametersIRSplitFilesWithInvalid() {
        Election testIR = CreateElection.createElection(getAllFilesInPath(multipleIRFilesPath));
        assertTrue(testIR instanceof IR);
        Class<?> spyIR = testIR.getClass();
        Field fields[] = spyIR.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                switch (field.getName()) {
                    case "ballots":
                        HashMap<String, Integer> testBallotCounter = new HashMap<>();
                        testBallotCounter.put("(D)(R)(I)(L)", 3);
                        testBallotCounter.put("(L)(D)(I)", 2);
                        assertEquals(testBallotCounter, field.get(testIR));
                        continue;
                    case "candidates":
                        ArrayList<Candidate> testCandidates = new ArrayList<>();
                        Candidate rosenCandidate = new Candidate("Rosen", "D", 0);
                        rosenCandidate.setCurNumVotes(3);
                        testCandidates.add(rosenCandidate);
                        testCandidates.add(new Candidate("Kleinberg", "R", 1));
                        testCandidates.add(new Candidate("Chou", "I", 2));
                        Candidate royceCandidate = new Candidate("Royce", "L", 3);
                        royceCandidate.setCurNumVotes(2);
                        testCandidates.add(royceCandidate);
                        ArrayList<Candidate> realCandidates = (ArrayList<Candidate>) field.get(testIR);
                        candidateArraylistIsEqual(testCandidates, realCandidates);
                        continue;
                    case "totalCounts":
                        assertEquals(field.get(testIR), 5);
                }
            } catch (IllegalAccessException e) {

            }
        }
        String[] filenames = CreateElectionTestHelpers.findInvalidated();
        String actualInvalidated = OPLTestHelpers.getAllFileContents(filenames[0]);
        assertEquals("1,,,,,1,",actualInvalidated);
    }

    /**
     * Tests if ballots with less than half of the candidates ranked are ignored.
     */
    @Test()
    public void testInvalidatingBallot() {
        Election testIR = assertTimeout(ofMillis(30000), () -> {
            return CreateElection.createElection(new String[] {invalidBallotsFilePath});
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
                        testBallotCounter.put("(D)(R)(I)(L)(C)(S)(B)", 1);
                        testBallotCounter.put("(D)(R)(I)(L)(C)(S)", 1);
                        testBallotCounter.put("(D)(R)(I)(L)(C)", 1);
                        testBallotCounter.put("(D)(R)(I)(L)", 1);
                        assertEquals(testBallotCounter, field.get(testIR));
                        continue;
                    case "candidates":
                        ArrayList<Candidate> testCandidates = new ArrayList<>();
                        int candidateID = 0;
                        //Have to make candidate objects for those we want to set votes of
                        Candidate rosenCandidate = new Candidate("Rosen", "D", candidateID++);
                        rosenCandidate.setCurNumVotes(4);
                        testCandidates.add(rosenCandidate);
                        testCandidates.add(new Candidate("Kleinberg", "R", candidateID++));
                        testCandidates.add(new Candidate("Chou", "I", candidateID++));
                        testCandidates.add(new Candidate("Royce", "L", candidateID++));
                        testCandidates.add(new Candidate("Cena", "C", candidateID++));
                        testCandidates.add(new Candidate("Stark", "S", candidateID++));
                        testCandidates.add(new Candidate("Barnes", "B", candidateID++));
                        ArrayList<Candidate> realCandidates = (ArrayList<Candidate>) field.get(testIR);
                        candidateArraylistIsEqual(testCandidates, realCandidates);
                        continue;
                    case "totalCounts":
                        assertEquals(field.get(testIR), 4);
                }
            } catch (IllegalAccessException e) {

            }
        }
    }

    /**
     * Tests that an OPL election is created successfully when multiple files (10) are given
     * as input.
     */
    @Test
    public void testParametersOPLMultipleFiles() {
        String[] filenames = getAllFilesInPath(multipleOPLFilesPath);
        Election testOPL = CreateElection.createElection(filenames);
        assertTrue(testOPL instanceof OPL);
        verifyOPLAttributes(testOPL);
    }

    /**
     * Tests that an OPL election is created successfully when multiple files are given
     * as input and some of them may have no ballots in them.
     */
    @Test
    public void testParametersOPLMultipleFilesNoBallots() {
        String[] filenames = getAllFilesInPath(multipleOPLFilesPath);
        String[] newFiles = new String[filenames.length + 1];
        System.arraycopy(filenames, 0, newFiles, 0, filenames.length);
        newFiles[filenames.length] = noBallotsOPL;
        Election testOPL = CreateElection.createElection(newFiles);
        assertTrue(testOPL instanceof OPL);
        verifyOPLAttributes(testOPL);
    }

    /**
     * Tests that an OPL election is not created when the number of seats in the election is 0
     */
    @Test
    public void testInvalidNumSeatsOPL() {
        Election nullElection =
                CreateElection.createElection(new String[] {noSeatsOPLPath});
        assertEquals(null, nullElection);
        String actualOutput = systemOut.toString().replaceAll("[\n\r]", "");
        String testOutput = "Error: An OPL Election cannot be created with 0 seats available.";
        assertEquals(testOutput, actualOutput);
    }

    /**
     * Tests to make sure no election is created if no election files are given (empty string array)
     */
    @Test
    public void testNoElectionFilesGiven() {
        // empty string array
        Election nullElection =
                CreateElection.createElection(new String[] {});
        assertEquals(null, nullElection);
        String actualOutput = systemOut.toString().replaceAll("[\n\r]", "");
        String testOutput = "Error: There needs to be at least one election files for an election.";
        assertEquals(testOutput, actualOutput);
    }

    /**
     * Tests to make sure no election is created if no election files are given (null string array)
     */
    @Test
    public void testNoElectionFilesGivenNull() {
        // null string array
        Election nullElection =
                CreateElection.createElection(null);
        assertEquals(null, nullElection);
        String actualOutput = systemOut.toString().replaceAll("[\n\r]", "");
        String testOutput = "Error: There needs to be at least one election files for an election.";
        assertEquals(testOutput, actualOutput);
    }

    /**
     * Tests to make sure an election isn't created if one of the files listed as input does not
     * exist when there are multiple files
     */
    @Test
    public void someFilesDontExistOPL() {
        String[] filenames = getAllFilesInPath(multipleOPLFilesPath);
        // replace a file with one that doesn't exist
        filenames[0] = "INVALID.csv";

        Election nullElection =
                CreateElection.createElection(filenames);
        assertEquals(null, nullElection);
        String actualOutput = systemOut.toString().replaceAll("[\n\r]", "");
        String testOutput = "Error: File (INVALID.csv) Not Found";
        assertEquals(testOutput, actualOutput);
    }

    /**
     * Tests to make sure a mix of election types inputs fail when creating an election
     */
    @Test
    public void multipleElectionTypesTest() {
        String[] filenames = getAllFilesInPath(differentElectionTypesPath);
        Election nullElection =
                CreateElection.createElection(filenames);
        assertEquals(null, nullElection);
        String actualOutput = systemOut.toString().replaceAll("[\n\r]", "");
        String testOutput = "Error: The election type of one of the input files is not the same " +
                "as the expected";
        assertEquals(testOutput, actualOutput);
    }

    /**
     * Gets all the filenames in the directory provided by path and returns the filenames as an
     * array of strings
     *
     * @param path the path to the desired directory
     * @return An array of all the files in the specified directory
     */
    private static String[] getAllFilesInPath(String path) {
        File desiredDirectory = new File(path); // Desired directory
        File[] listOfFiles = desiredDirectory.listFiles();
        String[] namesOfFiles = new String[listOfFiles.length];

        for (int i = 0; i < listOfFiles.length; i++) {
            namesOfFiles[i] = path + "/" + listOfFiles[i].getName();
        }

        return namesOfFiles;
    }

    /**
     * Helper function to make sure an OPL election has the correct attributes for a specific
     * election (the election files being the ones found in testing/testFiles/MultipleOPLFiles)
     * @param testOPL the OPL election that was created
     */
    private static void verifyOPLAttributes(Election testOPL) {
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
                        pikeCandidate.setCurNumVotes(16803);
                        Candidate fosterCandidate = new Candidate("Foster", "D", 1);
                        fosterCandidate.setCurNumVotes(16612);

                        Party dParty = new Party("D");
                        dParty.addCandidate(pikeCandidate);
                        dParty.addCandidate(fosterCandidate);
                        dParty.setTotalVotes(33415);
                        testParty.add(dParty);

                        Candidate deutschCandidate = new Candidate("Deutsch", "R", 2);
                        deutschCandidate.setCurNumVotes(16638);
                        Candidate borgCandidate = new Candidate("Borg", "R", 3);
                        borgCandidate.setCurNumVotes(16732);
                        Candidate jonesCandidate = new Candidate("Jones", "R", 4);
                        jonesCandidate.setCurNumVotes(16593);

                        Party rParty = new Party("R");
                        rParty.addCandidate(deutschCandidate);
                        rParty.addCandidate(borgCandidate);
                        rParty.addCandidate(jonesCandidate);
                        rParty.setTotalVotes(49963);
                        testParty.add(rParty);

                        Candidate smithCandidate = new Candidate("Smith", "I", 5);
                        smithCandidate.setCurNumVotes(16622);

                        Party iParty = new Party("I");
                        iParty.addCandidate(smithCandidate);
                        iParty.setTotalVotes(16622);
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

    @Test
    public void testNoInvalidatingBallot() {
        Election testIR = assertTimeout(ofMillis(30000), () -> {
            return CreateElection.createElection(new String[] {noInvalidBallotsFilePath});
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
                        testBallotCounter.put("(D)(R)(I)(L)(C)(S)(B)", 1);
                        testBallotCounter.put("(D)(R)(I)(L)(C)(S)", 1);
                        testBallotCounter.put("(D)(R)(I)(L)(C)", 1);
                        testBallotCounter.put("(D)(R)(I)(L)", 1);
                        assertEquals(testBallotCounter, field.get(testIR));
                        continue;
                    case "candidates":
                        ArrayList<Candidate> testCandidates = new ArrayList<>();
                        int candidateID = 0;
                        //Have to make candidate objects for those we want to set votes of
                        Candidate rosenCandidate = new Candidate("Rosen", "D", candidateID++);
                        rosenCandidate.setCurNumVotes(4);
                        testCandidates.add(rosenCandidate);
                        testCandidates.add(new Candidate("Kleinberg", "R", candidateID++));
                        testCandidates.add(new Candidate("Chou", "I", candidateID++));
                        testCandidates.add(new Candidate("Royce", "L", candidateID++));
                        testCandidates.add(new Candidate("Cena", "C", candidateID++));
                        testCandidates.add(new Candidate("Stark", "S", candidateID++));
                        testCandidates.add(new Candidate("Barnes", "B", candidateID++));
                        ArrayList<Candidate> realCandidates = (ArrayList<Candidate>) field.get(testIR);
                        candidateArraylistIsEqual(testCandidates, realCandidates);
                        continue;
                    case "totalCounts":
                        assertEquals(field.get(testIR), 4);
                }
        } catch (IllegalAccessException e) {

        }
    }
}

    /**
     * Tests if the Invalidated ballots file is correct when there are invalid ballots present
     */
    @Test
    public void invalidBallotsFileOutput() {
        // get the example output files
        String expectedOutput = OPLTestHelpers.getAllFileContents(
                "testing/ExampleInvalidatedFileOutput/ExampleInvalidatedFileOutput.txt");
        assertNotNull(expectedOutput);
        CreateElection.createElection(new String[] {invalidBallotsFilePath});
        String[] filenames = CreateElectionTestHelpers.findInvalidated();
        String actualInvalidated = OPLTestHelpers.getAllFileContents(filenames[0]);
        assertNotNull(actualInvalidated);

        // compare output to output file
        assertEquals(expectedOutput,actualInvalidated);

    }

    /**
     * Tests if the Invalidated ballots file is correct when there are no invalid ballots present
     */
    @Test
    public void noInvalidBallotsFileOutput() {
        // get the example output files
        String expectedOutput = OPLTestHelpers.getAllFileContents(
                "testing/ExampleInvalidatedFileOutput/ExampleNoInvalidatedFileOutput.txt");
        assertNotNull(expectedOutput);
        CreateElection.createElection(new String[] {noInvalidBallotsFilePath});
        String[] filenames = CreateElectionTestHelpers.findInvalidated();
        String actualInvalidated = CreateElectionTestHelpers.getAllFileContents(filenames[0]);
        assertNotNull(actualInvalidated);

        // compare output to output file
        assertEquals(expectedOutput,actualInvalidated);
    }
}

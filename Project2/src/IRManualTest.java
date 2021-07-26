import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IR Manual Unit Tests
 *
 * These tests are those which has some form of manual component for the tester.
 *
 * @author Mohammad Essawy
 */
public class IRManualTest {
    /**
     * Tests the handleTie function that is used by IR when inputted valid arguments
     * Creates situation where tiw will exist in every round of Ir in order to make
     * sure the function works when multiple people tie for last place and when
     * two people tie for first place.
     */
    @Test
    public void testHandleTie() {
        // don't output results to console every time algorithm is run
        ByteArrayOutputStream systemOut = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(systemOut));


        int p0 = 0;
        int p1 = 0;
        int p2 = 0;
        int p3 = 0;

        for (int i = 0; i < 1000; i++) {
            int totalNumBallots = 40;
            int[] votes = new int[]{0, 0, 0, 0};

            ArrayList<Candidate> candidates = IRTestHelpers.createBasicCandidateInstance(votes);
            HashMap<String, Integer> ballots = new HashMap<String, Integer>();
            ballots.put("(p0)", 10);
            ballots.put("(p1)", 10);
            ballots.put("(p2)", 10);
            ballots.put("(p3)", 10);

            IR ir = new IR(ballots, candidates, totalNumBallots);
            ir.runVotingAlgorithm();
            if (ir.getWinner().getParty().equals("p0")) {
                p0++;
            } else if (ir.getWinner().getParty().equals("p1")) {
                p1++;
            } else if (ir.getWinner().getParty().equals("p2")) {
                p2++;
            } else {
                p3++;
            }

            IRTestHelpers.deleteElectionOutputFiles();
        }

        StringBuilder out = new StringBuilder();
        out.append("Testing the HandleTie function:\n");
        out.append("-------------------------------\n");
        out.append("The number of parties in the tie is 4. The frequency of each party winning a"
                + " tie should be roughly equal.\n");
        out.append("Party ".concat("p0").concat(" has won the tie ")
                .concat(String.valueOf(p0)).concat(" times.\n"));
        out.append("Party ".concat("p1").concat(" has won the tie ")
                .concat(String.valueOf(p1)).concat(" times.\n"));
        out.append("Party ".concat("p2").concat(" has won the tie ")
                .concat(String.valueOf(p2)).concat(" times.\n"));
        out.append("Party ".concat("p3").concat(" has won the tie ")
                .concat(String.valueOf(p3)).concat(" times.\n"));

        // now we want to print results to console
        System.setOut(originalOut);
        System.out.println(out.toString());
    }
}

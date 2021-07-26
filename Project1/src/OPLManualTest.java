import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OPL Manual Unit Tests
 *
 * These tests are those which has some form of manual component for the tester.
 *
 * @author Michael Markiewicz
 */
public class OPLManualTest {
  /**
   * Tests the handleTie function that is used by OPL for valid arguments.
   */
  @Test
  public void testHandleTie() {
    // test ties for a number of situations
    int[] numPartiesInTie = {1, 5, 10, 100};
    int repeat = 100000;
    int result;
    int freq[];
    StringBuilder out = new StringBuilder();
    for (int numParties: numPartiesInTie) {
      freq = new int[numParties];
      for (int i = 0; i < repeat; i++) {
        result = Election.handleTie(numParties);
        freq[result]++;
        // check if the result is within the bounds
        assertTrue(0 <= result && result < numParties);
      }
      for (int i = 0; i < numParties; i++) {
        // each party should have been chosen at least once
        assertTrue(freq[i] >= 1);
      }
      if (numParties == 5) {
        // only output frequencies if the number of people in tie is 5
        // this is because we don't want too much output
        out.append("Testing the HandleTie function:\n");
        out.append("-------------------------------\n");
        out.append("The number of parties in the tie is 5. The frequency of each party winning a"
                   + " tie should be roughly equal.\n");
        for (int party = 0; party < numParties; party++) {
          out.append("Party ".concat(String.valueOf(party)).concat(" has won the tie ")
                     .concat(String.valueOf(freq[party])).concat(" times.\n"));
        }
      }
    }
    System.out.println(out.toString());
  }

  /**
   * Checks that the results of a tie in remaining votes causes different outcomes
   * (i.e., the tie has been resolved different outcomes could occur each time)
   * This will print off the number of times that each party won in the tie. It will be
   * up to the tester whether that number is within tolerance to be equal.
   */
  @Test
  public void testOPLRunVotingAlgorithmCheckPartyTie() {
    // don't output results to console every time algorithm is run
    ByteArrayOutputStream systemOut = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(systemOut));

    // basic info that wont need to reset each iteration
    int numParties = 4;
    int[] numCandidatesPerParty = {3, 2, 6, 2};

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

    // run the OPL algorithm 1500 times and prints the total frequency of seats
    // for parties 1, 2, 3, and 4
    int numRuns = 1500;
    int[] freq = new int[4];
    for (int i = 0; i < numRuns; i++) {
      // create a number of parties with different numbers of candidates
      ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                         numCandidatesPerParty);

      // set the number of votes for each party
      OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);

      // run election
      OPL opl = new OPL(totalVotes, totalSeats, parties);
      opl.runVotingAlgorithm();

      for (int j = 0; j < numParties; j++) {
        freq[j] += parties.get(j).getNumberOfSeats();
      }
    }

    StringBuilder out = new StringBuilder();
    out.append("Testing the Ties Between Parties:\n");
    out.append("---------------------------------\n");
    out.append("3/4 parties should have tied in election. The number of times the 3 parties "
               + "won each seat should be close to equal.\n");
    out.append("Party 1 won the seat " + freq[0] + " times.\n");
    out.append("Party 2 won the seat " + freq[1] + " times.\n");
    out.append("Party 3 won the seat " + freq[2] + " times.\n\n");

    // now we want to print results to console
    System.setOut(originalOut);
    System.out.print(out.toString());

    assertEquals(0, freq[3]); // the fourth party shouldn't have won any seat anytime.

    // don't want lots of unnecessary test files in working directory
    OPLTestHelpers.deleteElectionOutputFiles();
  }

  /**
   * Checks that the results of a tie between candidates causes different outcomes
   * (i.e., the tie has been resolved different outcomes could occur each time)
   * This will print off the number of times that each candidate won in the tie.
   * It will be up to the tester whether that number is within tolerance to be equal.
   */
  @Test
  public void testOPLRunVotingAlgorithmCheckCandidateTie() {
    // don't output results to console every time algorithm is run
    ByteArrayOutputStream systemOut = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(systemOut));

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

    // run the OPL algorithm 1500 times and prints the total frequency of seats
    // for parties 1, 2, 3, and 4
    int numRuns = 1500;
    int[] freq = new int[4];
    for (int i = 0; i < numRuns; i++) {
      // create a number of parties with different numbers of candidates
      ArrayList<Party> parties = OPLTestHelpers.createBasicPartyInstance(numParties,
                                                                         numCandidatesPerParty);

      // set the number of votes for each party
      OPLTestHelpers.setPartyAndCandidateVotes(parties, partyVotes, candidateVotes);

      // run election
      OPL opl = new OPL(totalVotes, totalSeats, parties);
      opl.runVotingAlgorithm();

      // get winners
      ArrayList<Candidate> winners = opl.getWinningCandidates();

      // get candidates in the first party
      ArrayList<Candidate> candidates = parties.get(0).getCandidates();

      for (Candidate winner: winners) {
        for (int j = 0; j < candidates.size(); j++) {
          if (winner.getCandidateID() == candidates.get(j).getCandidateID()) {
            freq[j] += 1;
          }
        }
      }
    }

    StringBuilder out = new StringBuilder();
    out.append("Testing the Ties Between Candidates:\n");
    out.append("------------------------------------\n");
    out.append("3/4 candidates should have tied in election for a seat. The number of times the "
               + "3 candidates won a seat should be close to equal.\n");
    out.append("Candidate 1 won the seat " + freq[0] + " times.\n");
    out.append("Candidate 2 won the seat " + freq[1] + " times.\n");
    out.append("Candidate 3 won the seat " + freq[2] + " times.\n\n");

    // now we want to print results to console
    System.setOut(originalOut);
    System.out.println(out.toString());

    assertEquals(0, freq[3]); // the fourth candidate shouldn't have won any seat anytime.

    // don't want lots of unnecessary test files in working directory
    OPLTestHelpers.deleteElectionOutputFiles();
  }
}

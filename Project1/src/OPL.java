import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

/**
 * The OPL class is in charge of running an Open Party List election and outputting any results
 * to the audit file, media report, and display.
 *
 * @author Michael Markiewicz, Noah Park
 */
public class OPL extends Election {
  /**
   * The number of seats available in the election
   */
  private int numSeatsAvailable;

  /**
   * The quota for the first round of allocation
  */
  private int quota;

  /**
   * All the parties that are taking part in the election
   */
  private ArrayList<Party> parties;

  /**
   * The number of seats that need to be distributed still
   */
  private int numSeatsLeft;

  /**
   * The list of winning candidates. This won't be initialized until the algorithm has been run.
   */
  private ArrayList<Candidate> winningCandidates;

  /**
   * A string builder to keep track of ties and any other notes about the election.
   */
  private StringBuilder additionalNotes;

  /**
   * Keeps track of how many seats each party got in the first allocation
   */
  private HashMap<Party, Integer> firstAllocation;

  /**
   * The constructor for OPL initializes the OPL class so it is ready to run the OPL algorithm.
   * It is assumed that the parties have been initialized with the candidates for the election,
   * and that the totalNumBallots and numSeatsAvailable are positive.
   *
   * @param totalNumBallots the total number of ballots cast for this election.
   * @param numSeatsAvailable the number of seats that are up for election.
   * @param parties a list of parties that have candidates in the election.
   * @throws IllegalArgumentException if the totalNumBallots or numSeatsAvailable is nonpositive,
   *         or the parties is null or empty.
   */
  public OPL(int totalNumBallots, int numSeatsAvailable, ArrayList<Party> parties)
          throws IllegalArgumentException {
    if (totalNumBallots <= 0 || numSeatsAvailable <= 0 || parties == null || parties.size() == 0) {
      throw new IllegalArgumentException("totalNumBallots must be positive, numSeatsAvailable must"
              + " be positive, and parties must not be null and contain at least one party");
    }

    int numCandidates = 0;
    for (Party party: parties) {
      numCandidates += party.getNumCandidates();
    }

    // initialize class variables
    this.totalNumBallots = totalNumBallots;
    this.numSeatsAvailable = numSeatsAvailable;
    this.parties = parties;
    quota = totalNumBallots / numSeatsAvailable; // rounds down to nearest integer
    numSeatsLeft = numSeatsAvailable;
    winningCandidates = new ArrayList<>();
    additionalNotes = new StringBuilder();
    additionalNotes.append("Additional Notes:\n");
    additionalNotes.append("-----------------\n");
    auditString = new StringBuilder();
    auditString.append("Election Type: Open Party List\n");
    auditString.append("Number of candidates: ".concat(String.valueOf(numCandidates)).concat("\n"));
    auditString.append("Candidates: ");
    for (Party party: parties) {
      for (Candidate candidate: party.getCandidates()) {
        auditString.append("[".concat(candidate.getName()).concat(",").concat(party.getName())
                   .concat("],"));
      }
    }
    auditString.setLength(auditString.length() - 1); // remove last comma
    auditString.append("\nNumber of Seats: ".concat(String.valueOf(numSeatsAvailable))
               .concat("\n"));
    auditString.append("Total number of Votes: ".concat(String.valueOf(totalNumBallots))
               .concat("\n"));
    auditString.append("Calculated Quota: ".concat(String.valueOf(quota)).concat("\n"));
    firstAllocation = new HashMap<>();
  }

  /**
   * Main function to run voting algorithm for OPL. This will conduct the first allocation,
   * second allocation, determine the winners, output the audit file, output the media report,
   * and output the election results to the screen.
   */
  public void runVotingAlgorithm() {
    // conduct the OPL voting algorithm
    conductFirstAllocation();
    conductSecondAllocation();
    determineWinners();

    // finalizes the additionalNotes stringBuilder
    finishUpAdditionalNotes();

    // output results to screen
    outputResults();

    // output the results to the audit file and media report
    try {
      generateAuditFile();
    } catch (FileNotFoundException f) {
      System.out.println(f.getMessage());
    }
    try {
      generateMediaFile();
    } catch (FileNotFoundException f) {
      System.out.println(f.getMessage());
    }
  }

  /**
   * Conducts the first allocation of votes for the OPL election.
   */
  private void conductFirstAllocation() {
    StringBuilder info = new StringBuilder();

    for (Party party : parties) {
      int totalPartyVotes = party.getTotalVotes();
      // <party name>: <total votes for party>
      auditString.append(party.getName().concat(",").concat(String.valueOf(totalPartyVotes)
                 .concat("\n")));

      for (Candidate candidate : party.getCandidates()) {
        // [<candidate name>, <total votes for candidate>]
        auditString.append("[".concat(candidate.getName()).concat(",")
                   .concat(String.valueOf(candidate.getCurNumVotes())).concat("],"));
      }
      auditString.setLength(auditString.length() - 1); // remove last comma
      auditString.append("\n");

      // make sure the parties do not get more seats than they have candidates for
      int obtainedSeats = Math.min(totalPartyVotes / quota, party.getNumCandidates());
      party.setNumberOfSeats(obtainedSeats);

      numSeatsLeft -= obtainedSeats;
      party.setRemainingVotes(totalPartyVotes - quota * obtainedSeats);

      // <Party name> has <obtained seats> quotas
      info.append(party.getName().concat(" has ").concat(String.valueOf(obtainedSeats))
          .concat(" quota(s)\n"));

      // add this party's first allocation to a hash map for later use
      firstAllocation.put(party, obtainedSeats);
    }

    info.append(String.valueOf(numSeatsLeft).concat(" seat(s) remaining\n"));
    auditString.append(info);
  }

  /**
   * Conducts the second allocation of votes for the OPL election.
   */
  private void conductSecondAllocation() {
    // don't need to do anything if there are no more seats to allocate
    if (numSeatsLeft <= 0) {
      return;
    }

    // maps remaining votes to a list of Parties
    // the values are a list of Parties to handle ties between remaining votes
    TreeMap<Integer, ArrayList<Party>> ordering = new TreeMap<>();

    for (Party party : parties) {
      Integer remainingVotes = party.getRemainingVotes();

      // <party name>: <remaining votes>
      auditString.append(party.getName().concat(",").concat(String.valueOf(remainingVotes))
                 .concat("\n"));

      if (ordering.containsKey(remainingVotes)) {
        // if the key already exists in the map, then add the party to the end of the list
        ordering.get(remainingVotes).add(party);
      } else {
        ArrayList<Party> newList = new ArrayList<>();
        newList.add(party);
        ordering.put(remainingVotes, newList);
      }
    }

    // array of keys will help go through each party in a circular manner (if needed)
    Integer[] votes = ordering.descendingMap().keySet().toArray(new Integer[0]);
    int i = 0; // keeps track of which party we will be assigning votes to
    // Will keep track of all the parties that can earn no more seats.
    // Resets at the beginning of every loop to recount the parties.
    // If this becomes equal to the number of parties total, then there are more seats left
    // than parties have candidates for. We should stop even though there are seats left to
    // distribute since we would be stuck in an infinite loop. This may be a little inefficient,
    // but it should not happen often (if at all).
    int partiesWithNoMoreCandidates = 0;
    int totalParties = parties.size();
    while (numSeatsLeft > 0 && partiesWithNoMoreCandidates < totalParties) {
      // go until there are no more seats to distribute or no more candidates to give seats to
      // get the parties we will look at giving seats to
      ArrayList<Party> currentParties = ordering.get(votes[i]);

      int size = currentParties.size();
      // go through the parties with the same number of votes and remove any that don't have
      // any candidates left
      ArrayList<Integer> partiesToRemove = new ArrayList<>();
      for (int j = 0; j < size; j++) {
        if (currentParties.get(j).getNumberOfSeats() >= currentParties.get(j).getNumCandidates()) {
          // remove this party from consideration
          partiesToRemove.add(j);
          partiesWithNoMoreCandidates += 1;
        }
      }
      for (int j = 0; j < partiesToRemove.size(); j++) {
        currentParties.remove(j);
      }

      size = currentParties.size(); // update the number of parties in the list if necessary

      if (size > 1) {
        // if there is more than one party with the same number of votes, we may need to
        // determine a tie
        if (size <= numSeatsLeft) {
          // everyone gets a seat. No need to determine tie
          for (int j = 0; j < size; j++) {
            addSeat(currentParties.get(j));
          }
        } else {
          // need to determine ties
          // create tiedParties array so we can keep track of all the parties left in the tie
          ArrayList<Party> tiedParties = (ArrayList<Party>) currentParties.clone();

          while (numSeatsLeft > 0) {
            // stop when there are no more seats left to distribute
            int winningPartyIndex = Election.handleTie(tiedParties.size());
            addSeat(tiedParties.get(winningPartyIndex));
            // party successfully got a new seat. Need to report this tie to additional notes
            reportPartyTie(winningPartyIndex, tiedParties);
            // remove the party that won the tie from the list
            tiedParties.remove(winningPartyIndex);
          }
        }
      } else if (size == 1){
        // no tie, so we can add the seat if the party has enough candidates
        addSeat(currentParties.get(0));
      }

      i = (i+1) % votes.length; // increment i and loop around to beginning if needed
    }

    // add any last info to audit file
    if (numSeatsLeft > 0) {
      // need to account for case that there are less candidates in election that seats available
      auditString.append("Not all seats have been distributed. This is likely due to there being " +
                         "less candidates than seats available.\n");
    } else {
      auditString.append("All seats have been filled.\n");
    }
    for(Party party: parties) {
      auditString.append(party.getName().concat(" has earned ")
                 .concat(String.valueOf(party.getNumberOfSeats())).concat(" seat(s).\n"));
    }
  }

  /**
   * Helper function for conductSecondAllocation. Ensures that the party has enough candidates
   * for another seat.
   *
   * @param party The party that we are trying to add another seat for.
   * @return true if the party earned another seat, false if the party did not have enough
   *        candidates for another seat.
   */
  private boolean addSeat(Party party) {
    int numSeats = party.getNumberOfSeats();
    if (party.getNumCandidates() <= numSeats) {
      // no more possible candidates for the seat. Party does not get assigned another seat.
      return false;
    }
    party.setNumberOfSeats(numSeats + 1);
    numSeatsLeft -= 1;
    auditString.append(party.getName().concat(" receives another seat.\n"));
    return true;
  }

  /**
   * Helper function for conductSecondAllocation. This function reports a tie to the
   * additionalNotes stringBuilder.
   *
   * @param winningPartyIndex the index into tiedParties that corresponds to the winning party.
   * @param tiedParties the list of parties that were a part of the tie.
   */
  private void reportPartyTie(int winningPartyIndex, ArrayList<Party> tiedParties) {
    // list each party in the tie, and declare which party won the tie.
    for (int j = 0; j < tiedParties.size(); j++) {
      additionalNotes.append(tiedParties.get(j).getName());
      if (j != tiedParties.size()-1) {
        additionalNotes.append(", ");
      }
    }
    additionalNotes.append(" tied when assigning remaining seats. ");
    additionalNotes.append(tiedParties.get(winningPartyIndex).getName()
                   .concat(" won in a fair coin toss.\n"));
  }

  /**
   * Function to determine candidates that won in each party. Adds each winner to the
   * winningCandidates attribute for later use. This assumes that the number of seats each party
   * won is less than or equal to the number of candidates they have.
   */
  private void determineWinners() {
    // go through each party and determine the winning candidates
    int numSeatsWon;
    ArrayList<Candidate> candidates;
    TreeMap<Integer, ArrayList<Candidate>> orderedCandidates;
    for(Party party: parties) {
      orderedCandidates = new TreeMap<>(); // reset for each party
      numSeatsWon = party.getNumberOfSeats();
      candidates = party.getCandidates();

      for (Candidate candidate: candidates) {
        // put all candidates into TreeMap which will naturally order them by key
        // having the value be the a list of candidates helps determine ties if needed
        Integer numVotes = candidate.getCurNumVotes();
        if (orderedCandidates.containsKey(numVotes)) {
          // if the key already exists in the map, then add the party to the end of the list
          orderedCandidates.get(numVotes).add(candidate);
        } else {
          ArrayList<Candidate> newList = new ArrayList<>();
          newList.add(candidate);
          orderedCandidates.put(numVotes, newList);
        }
      }

      Iterator<Integer> votesOrdered = orderedCandidates.descendingKeySet().iterator();
      ArrayList<Candidate> currentCandidates;
      for (int numSeatsDistributed = 0; numSeatsDistributed < numSeatsWon; numSeatsDistributed++) {
        // go until there are no seats left
        currentCandidates = orderedCandidates.get(votesOrdered.next());
        if (currentCandidates.size() > 1) {
          // may need to determine tie
          if (numSeatsWon - numSeatsDistributed >= currentCandidates.size()) {
            // no need to determine ties. Each candidate has won a seat
            for (Candidate candidate: currentCandidates) {
              winningCandidates.add(candidate);
              numSeatsDistributed += 1;
            }
          } else {
            // need to determine tie
            // create tiedCandidates array so we can keep track of all the
            // candidates left in the tie
            ArrayList<Candidate> tiedCandidates = (ArrayList<Candidate>) currentCandidates.clone();

            while (tiedCandidates.size() != 0 && numSeatsWon - numSeatsDistributed > 0) {
              // go until there are no more candidates in the list (this shouldn't happen)
              // otherwise, stop when there are no more seats left to distribute
              // to winning candidates
              int winningCandidateIndex = Election.handleTie(tiedCandidates.size());
              // candidate successfully got a new seat. Need to report this tie to additional notes
              winningCandidates.add(tiedCandidates.get(winningCandidateIndex));
              reportCandidateTie(winningCandidateIndex, tiedCandidates);
              // remove the party that won the tie from the list
              tiedCandidates.remove(winningCandidateIndex);
              numSeatsDistributed += 1;
            }
          }
          // have to decrease by 1 to account that the 'for'-loop is going to increase it again
          numSeatsDistributed -= 1;
        } else {
          // no tie, candidate has won a seat
          winningCandidates.add(currentCandidates.get(0));
        }
      }
    }

    reportWinningCandidates();
  }

  /**
   * Helper function for determineWinners. This function reports a tie to the
   * additionalNotes stringBuilder.
   *
   * @param winningCandidateIndex the index into tiedCandidates that corresponds to
   *                              the winning candidate.
   * @param tiedCandidates the list of candidates that were a part of the tie.
   */
  private void reportCandidateTie(int winningCandidateIndex, ArrayList<Candidate> tiedCandidates) {
    // list each candidate in the tie, and declare which candidate won the tie.
    for (int j = 0; j < tiedCandidates.size(); j++) {
      additionalNotes.append(tiedCandidates.get(j).getName());
      if (j != tiedCandidates.size()-1) {
        additionalNotes.append(", ");
      }
    }
    additionalNotes.append(" tied in popularity when assigning seats for ");
    additionalNotes.append(tiedCandidates.get(0).getParty().concat(". "));
    additionalNotes.append(tiedCandidates.get(winningCandidateIndex).getName()
                   .concat(" won in a fair coin toss.\n"));
  }

  /**
   * Helper function for determineWinners. Adds to the audit string which candidates won
   * in the election.
   */
  private void reportWinningCandidates() {
    for (Candidate candidate: winningCandidates) {
      auditString.append("[".concat(candidate.getName()).concat(",")
                 .concat(candidate.getParty()).concat("],"));
    }
    auditString.setLength(auditString.length() - 1); // remove last comma
    auditString.append(" have been elected.\n\n");
  }

  /**
   * Gets the quota for the election.
   *
   * @return the election's quota.
   */
  public int getQuota() {
    return quota;
  }

  /**
   * Gets the number of seats that are in the election.
   *
   * @return the number of seats up for election.
   */
  public int getNumSeatsAvailable() {
    return numSeatsAvailable;
  }

  /**
   * Gets the number of ballots cast in the election.
   *
   * @return the number of votes cast.
   */
  public int getTotalNumBallots() {
    return totalNumBallots;
  }

  /**
   * Gets the candidates who won a seat in the election.
   *
   * @return the array of Candidates that won a seat in the election.
   */
  public ArrayList<Candidate> getWinningCandidates() {
    return winningCandidates;
  }

  /**
   * Gets the mapping of the parties to their first allocation of seats.
   *
   * @return a mapping from Parties to the number of seats they won in the first allocation.
   */
  public HashMap<Party, Integer> getFirstAllocation() {
    return firstAllocation;
  }

  /**
   * Gathers the results of the elections together into a string. This includes Candidate names,
   * total number of votes cast, number of seats up for election, and the winning candidates.
   *
   * @return the election results in a string format.
   */
  private String getElectionResults() {
    StringBuilder electionResults = new StringBuilder();
    electionResults.append("Election Results:\n");
    electionResults.append("-----------------\n");
    electionResults.append("Candidates:\n");
    for (Party party: parties) {
      electionResults.append("\t".concat(party.getName()).concat(" Candidates: "));
      for (Candidate candidate: party.getCandidates()) {
        electionResults.append(candidate.getName().concat(", "));
      }
      electionResults.setLength(electionResults.length() - 2); // remove last comma and space
      electionResults.append("\n");
    }
    electionResults.append("Total Number of votes cast: "
                   .concat(String.valueOf(totalNumBallots)).concat("\n"));
    electionResults.append("Number of seats up for election: "
                   .concat(String.valueOf(numSeatsAvailable)).concat("\n"));
    electionResults.append("Winners: ");
    for (Candidate candidate: winningCandidates) {
      electionResults.append(candidate.getName().concat(" (")
                     .concat(candidate.getParty()).concat("), "));
    }
    electionResults.setLength(electionResults.length() - 2); // remove last comma and space
    electionResults.append("\n\n");
    return electionResults.toString();
  }

  /**
   * Gathers the results of the allocation statistics together into a string.
   * This includes party name, votes for party, first allocation of seats, remaining
   * votes after first allocation, second allocation of seats, and final seat total.
   *
   * @return the allocation statistics in a string format.
   */
  private String getSeatAllocationStatistics() {
    StringBuilder allocationStatistics = new StringBuilder();
    allocationStatistics.append("Seat Allocation Statistics:\n");
    allocationStatistics.append("---------------------------\n");
    allocationStatistics.append("***Quota for First Allocation: ".concat(String.valueOf(quota))
                        .concat(" Votes***\n"));
    allocationStatistics.append("[Party],[Votes],[First Allocation of Seats],[Remaining Votes],"
                                + "[Second Allocation of Seats],[Final Seat Total]\n");
    for (Party party: parties) {
      int totalSeats = party.getNumberOfSeats();
      int firstAllocationSeats = firstAllocation.get(party);
      int secondAllocation = totalSeats - firstAllocationSeats;
      allocationStatistics.append(party.getName().concat(",")
                          .concat(String.valueOf(party.getTotalVotes())));
      allocationStatistics.append(",".concat(String.valueOf(firstAllocationSeats)).concat(","));
      allocationStatistics.append(String.valueOf(party.getRemainingVotes()).concat(","));
      allocationStatistics.append(String.valueOf(secondAllocation).concat(",")
                          .concat(String.valueOf(totalSeats)));
      allocationStatistics.append("\n");
    }
    allocationStatistics.append("\n");

    return allocationStatistics.toString();
  }

  /**
   * Gathers the voting results of each candidates together as a string.
   * This includes candidate name, party, and total number of votes cast for the candidate.
   *
   * @return the votes for each candidate in a string format.
   */
  private String getVotesForEachCandidate() {
    StringBuilder candidateVotes = new StringBuilder();
    candidateVotes.append("Votes for Each Candidate:\n");
    candidateVotes.append("-------------------------\n");
    for (Party party: parties) {
      candidateVotes.append(party.getName().concat(" Candidates: "));
      for (Candidate candidate: party.getCandidates()) {
        candidateVotes.append(candidate.getName().concat(" (")
                      .concat(String.valueOf(candidate.getCurNumVotes())).concat("), "));
      }
      candidateVotes.setLength(candidateVotes.length() - 2); // remove last comma and space
      candidateVotes.append("\n");
    }
    candidateVotes.append("\n");
    return candidateVotes.toString();
  }

  /**
   * Creates the audit file and outputs the auditString to the new file.
   */
  protected void generateAuditFile() throws FileNotFoundException {
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
    LocalDateTime currentTime = LocalDateTime.now();
    String filename = "OPLAuditFile_".concat(dateFormat.format(currentTime)).concat(".txt");
    auditString.append(additionalNotes.toString());

    PrintWriter out = new PrintWriter(new File(filename));
    out.print(auditString.toString());
    out.close();
  }

  /**
   * Creates the media file and outputs the election results, seat allocation statistics,
   * votes for each candidate, and additionalNotes to the new file.
   */
  protected void generateMediaFile() throws FileNotFoundException {
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
    LocalDateTime currentTime = LocalDateTime.now();
    String filename = "OPLMediaReport_".concat(dateFormat.format(currentTime)).concat(".txt");

    PrintWriter out = new PrintWriter(new File(filename));
    out.print(getElectionResults());
    out.print(getSeatAllocationStatistics());
    out.print(getVotesForEachCandidate());
    out.print(additionalNotes.toString());
    out.close();
  }

  /**
   * Outputs the election results, allocation statistics, votes for each candidate,
   * and additionalNotes to the display screen.
   */
  protected void outputResults() {
    System.out.print(getElectionResults());
    System.out.print(getSeatAllocationStatistics());
    System.out.print(getVotesForEachCandidate());
    System.out.print(additionalNotes.toString());
  }

  /**
   * Finalizes the additional notes string builder with any last minute information.
   */
  private void finishUpAdditionalNotes() {
    // check for ties, update additional notes if there aren't any ties
    if (additionalNotes.toString().equals("Additional Notes:\n-----------------\n")) {
      // there were no ties
      additionalNotes.append("No ties occurred in this election.\n");
    }
    // if not all the seats have been distributed, add this information
    if (numSeatsLeft > 0) {
      additionalNotes.append(String.valueOf(numSeatsLeft).concat(" seat(s) has not been "
                     + "distributed. This is likely due to there being less candidates than seats" +
                     " available.\n"));
    }
  }
}
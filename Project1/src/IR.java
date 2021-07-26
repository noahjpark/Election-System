import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * The IR class is in charge of running an Instant Runoff election and outputting any results
 * to the audit file, media report, and display.
 *
 * @author Mohammad Essawy, Michael Markiewicz, Justin Lam
 */
public class IR extends Election {

  /**
   * A map between the different permutations of candidate preferences, and the current number of
   * votes towards that preference.
   */
  private HashMap<String, Integer> ballots;

  /**
   * A list of candidates currently in the election.
   */
  private ArrayList<Candidate> candidates;

  /**
   * A string builder to help keep track of election results throughout the election.
   */
  private StringBuilder electionResults;

  /**
   * A string builder to help keep track of election statistics (i.e., where votes are
   * redistributed to in different rounds)
   */
  private StringBuilder electionStatistics;

  /**
   * A string builder to help keep track of ties and any other final remarks.
   */
  private StringBuilder finalNotes;

  /**
   * The total votes left in the election
   */
  private int totalCounts;

  /**
   * The current round that we are counting for
   */
  private int roundCount;

  /**
   * The candidate that won the election. Won't be initialized until the algorithm has been run.
   */
  private Candidate winner;

  /**
   * The current candidate that is being eliminated. This will be useful for outputting who is
   * being eliminated each round to the audit file.
   */
  private Candidate candidateToEliminate;

  /**
   * Keeps track of the number of votes each candidate had before a voting redistribution. This
   * is useful for showing how the votes for each candidate changed between rounds.
   */
  private HashMap<Candidate, Integer> votesBeforeRedistribution;

  /**
   * The constructor for IR initializes the IR class so it is ready to run the IR algorithm. It is assumed that
   * the candidates have been initialized with the candidates for the election, and that the totalNumBallots is
   * positive.
   *
   * @param candidates a list of candidates in the election.
   * @param ballots a hashmap containing all the votes cast
   * @param totalNumBallots the total number of ballots cast for this election.
   * @throws IllegalArgumentException if the totalNumBallots or numSeatsAvailable is nonpositive, or the parties is null or empty.
   */
  public IR(HashMap<String, Integer> ballots, ArrayList<Candidate> candidates, int totalNumBallots) {
    // check invalid inputs
    if (totalNumBallots <= 0 || candidates == null || ballots == null || candidates.isEmpty() || ballots.isEmpty()) {
      throw new IllegalArgumentException("totalNumBallots must be positive, candidates and ballots must be " +
              "non-null and non-empty");
    }

    // initialize from parameters to constructor
    this.totalNumBallots = totalNumBallots;
    this.ballots = ballots;
    this.candidates = candidates;

    // Create auditString
    auditString = new StringBuilder();
    auditString.append("Election Type: Instant Runoff\n");
    auditString.append("Number of candidates: ".concat(String.valueOf(candidates.size())).concat("\n"));
    auditString.append("Candidates: ");
    for (Candidate candidate: candidates) {
      auditString.append(candidate.getName().concat(" (").concat(candidate.getParty())
                 .concat("), "));
    }
    auditString.setLength(auditString.length() - 2);
    auditString.append("\n");
    auditString.append("Total Ballots: " + String.valueOf(totalNumBallots) + "\n");

    // Create election Results
    electionResults = new StringBuilder();
    electionResults.append("Election Results:\n");
    electionResults.append("-----------------\n");
    electionResults.append("Election Type: Instant Runoff\n");
    electionResults.append("Candidates: ");
    for (Candidate candidate: candidates) {
      electionResults.append(candidate.getName().concat(" (").concat(candidate.getParty()).concat("), "));
    }
    electionResults.setLength(electionResults.length() - 2); // remove last comma
    electionResults.append("\nTotal Number of Votes Cast: " + totalNumBallots + "\n");

    // create election Statistics string
    electionStatistics = new StringBuilder();
    electionStatistics = new StringBuilder();
    electionStatistics.append("Election Statistics:\n");
    electionStatistics.append("--------------------\n");

    //create final Notes string
    finalNotes = new StringBuilder();
    finalNotes.append("Final Notes:\n");
    finalNotes.append("------------\n");

    // last thing to initialize
    this.totalCounts = totalNumBallots;
    this.roundCount = 1;

    // initialize to empty
    votesBeforeRedistribution = new HashMap<>();

    //set original vote count for each candidate
    setCandidateOriginalVotes();
  }

  /**
   * Main function to run voting algorithm for IR. It will check for majority each round
   * if majority is not found it will eliminate a candidate and redistribute votes. It will repeat those
   * three steps until a winner is found. Then, it will output results and generate audit and media files.
   */
  public void runVotingAlgorithm() {
    updateAuditAndElectionStatistics();
    roundCount++;
    // repeat until winner is found
    while (!checkMajority()) {
      // redistribute the votes when there is not a majority
      candidateToEliminate = eliminateCandidate();
      updateVotes(candidateToEliminate);
      updateAuditAndElectionStatistics();
      roundCount++;
    }

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
   * Updates the audit and election statistics strings with the current count distribution
   */
  protected void updateAuditAndElectionStatistics() {
    String roundN = addNCountToStatistics(roundCount);
    electionStatistics.append(roundN);
    if (roundCount > 1) {
      // need to mention whose votes are being redistributed
      electionStatistics.append(" (Transfer of ".concat(candidateToEliminate.getName()).concat(
              "'s votes):\n"));
    } else {
      electionStatistics.append(":\n");
    }
    for (Candidate candidate : candidates) {
      electionStatistics.append("\t");
      electionStatistics.append(candidate.getName() + " (" + candidate.getParty() + "): " + candidate.getCurNumVotes());
      if (roundCount > 1) {
        // need to say how votes were redistributed
        int redistribution = candidate.getCurNumVotes() - votesBeforeRedistribution.get(candidate);
        electionStatistics.append(" (+".concat(String.valueOf(redistribution)).concat(")"));
      }
      electionStatistics.append("\n");
    }

    if (roundCount > 1) {
      auditString.append("Distribution after ".concat(roundN).concat(":\n"));
    } else {
      auditString.append("Original Distribution (i.e., 1st count):\n");
    }
    Iterator ballotsIteratorAudit = ballots.entrySet().iterator();
    while (ballotsIteratorAudit.hasNext()) {
      Map.Entry pair = (Map.Entry) ballotsIteratorAudit.next();
      auditString.append(pair.getKey().toString() + ": ");
      auditString.append(pair.getValue().toString() + "\n");
    }
  }

  /**
   * This function takes checks if any candidate has a majority.
   * If no candidate has a majority it checks for the case where
   * two candidates ties for first place. If a winner is found it
   * updates file Strings accordingly. Otherwise it returns false
   *
   * @return true if a candidate has been declared a winner and false if
   * no candidate was delcared a winner.
   */
  private boolean checkMajority() {
    // iterate through to find candidate with most votes
    Candidate mostVotes = candidates.get(0);
    for (Candidate candidate: candidates) {
      if(mostVotes.getCurNumVotes() < candidate.getCurNumVotes())
      mostVotes = candidate;
    }

    //check for majority
    float proportion = (float) mostVotes.getCurNumVotes() / (float) totalCounts;
    if( proportion > .5 ) {
      winner = mostVotes;
      String addition = mostVotes.getName() + " (" + mostVotes.getParty() + ") " + "has won the election with "
              +  String.valueOf(proportion * 100) + "% of the remaining votes.\nThis is " +
              String.valueOf((float) mostVotes.getCurNumVotes() / (float) totalNumBallots * 100) + " % of the total votes cast in " +
              "this election.\n";
      auditString.append("\n");
      auditString.append(electionStatistics);
      auditString.append(addition);
      electionResults.append(addition);

      if(finalNotes.length() < 40) {
        finalNotes.append("No ties occurred in this election.");
      }
      return true;
    }
    else if(candidates.size() == 2) {
      // if two candidates remain handle tie appropriately
      int winnerIndx = handleTie(2);
      candidates.get(winnerIndx);
      winner = candidates.get(winnerIndx);
      finalNotes.append(candidates.get(0).getName() + " and " + candidates.get(1).getName() +
              " tied in number of votes while determining the winner. " + candidates.get(winnerIndx).getName() +
              " won the election in a fair coin toss.");

      String addition = candidates.get(winnerIndx).getName() + " (" + candidates.get(winnerIndx).getParty()
              + ") " + "has won the election with " +  String.valueOf(proportion * 100) +
              "% of the remaining votes.\n This is " + String.valueOf((float) mostVotes.getCurNumVotes() / (float) totalNumBallots * 100) +
              " % of the total votes cast in this election.\n";
      auditString.append("\n");
      auditString.append(electionStatistics);
      auditString.append(addition);
      electionResults.append(addition);
      return true;
    }

    return false;
  }

  /**
   * This function finds the candidate to be eliminated given that no majority was found.
   * It eliminates the last place candidate so far. If multiple candidates tie for last place.
   * It randomly selects a candidate to be eliminated
   *
   * @return the candidate that will be eliminated
   */
  private Candidate eliminateCandidate() {
    // iterate through to find candidate with the least votes
    Candidate leastVotes = candidates.get(0);
    for (Candidate candidate: candidates) {
      if(leastVotes.getCurNumVotes() > candidate.getCurNumVotes())
        leastVotes = candidate;
    }

    // create a list of candidates tied for last
    ArrayList<Candidate> losingCandidates = new ArrayList<Candidate>();
    losingCandidates.add(leastVotes);
    for (Candidate candidate: candidates) {
      if(!leastVotes.equals(candidate) && leastVotes.getCurNumVotes() == candidate.getCurNumVotes()) {
        losingCandidates.add(candidate);
      }
    }

    // if only one candidate in last return
    if(losingCandidates.size() == 1) {
      return losingCandidates.get(0);
    }
    // otherwise break tie using handle tie function
    else {
      int loserIndex = handleTie(losingCandidates.size());
      Candidate loser = losingCandidates.get(loserIndex);
      // list each party in the tie, and declare which party won the tie.
      for (int i = 0; i < losingCandidates.size(); i++) {
        finalNotes.append(losingCandidates.get(i).getName());
        if (i != losingCandidates.size()-1) {
          finalNotes.append(", ");
        }
      }
      finalNotes.append(" tied in number of votes while determining the loser during round " + String.valueOf(roundCount-1) + ". " + loser.getName() +
                " was eliminated in a fair coin toss.\n");
      return losingCandidates.get(loserIndex);
    }
  }

  /**
   * This function iterates through the hashmap of ballots and selects the ballots
   * that contain the eliminated candidate. Then, it redistributes votes in that
   * ballot accordingly.
   * For example if p3 is eliminated votes in ballot (p0)(p3)(p1)(p2) go to ballot
   * (p0)(p1)(p2). Also, total counts and counts for each candidate are adjusted accordingly.
   *
   * @param c the candidate that will be removed
   */
  private void updateVotes(Candidate c) {
    // make arrays to store elements in hash map that will be
    // removed or added
    ArrayList<String> willBeRemoved = new ArrayList<>();
    ArrayList<Object[]> willBeAdded = new ArrayList<>();

    // update the previous votes for each candidate
    votesBeforeRedistribution = new HashMap<>();
    for (Candidate candidate: candidates) {
      if (candidate != c) {
        // only do this for candidates not being eliminated
        votesBeforeRedistribution.put(candidate, candidate.getCurNumVotes());
      }
    }

    // iterate through each element in hashmap
    Iterator ballotsIterator = ballots.entrySet().iterator();
    while (ballotsIterator.hasNext()) {
      Map.Entry pair = (Map.Entry) ballotsIterator.next();
      String ballotKey = pair.getKey().toString();
      // if element contains the party that was removed redistribute it
      if (ballotKey.contains('(' + c.getParty() + ')')) {
        // replacement is the ballot that the values will be moved to
        String replacement = ballotKey.replace('(' + c.getParty() + ')', "");
        // get value of the eliminated ballot
        int eliminatedBallotValue = (int) pair.getValue();
        // if you can't redistribute reduce count
        if (replacement.length() == 0) {
          totalCounts = totalCounts - eliminatedBallotValue;
        }
        // if the replacement is in the ballot add the new value
        else if (ballots.containsKey(replacement)) {
          int oldValue = ballots.get(replacement);
          ballots.replace(replacement, (oldValue + eliminatedBallotValue));

          // if votes have moved from loser to somebody else
          // increment there votes
          Candidate lostBallot = getCandidateFromBallot(ballotKey);
          Candidate wonBallot= getCandidateFromBallot(replacement);
          if (lostBallot != wonBallot) {
            wonBallot.incrementCurNumVotes((int)pair.getValue());
          }

        // otherwise create a new ballot with the key = replacement
        }
        else {
          Object toAdd[] = {replacement, eliminatedBallotValue};
          willBeAdded.add(toAdd);

          // if votes have moved from loser to somebody else
          // increment there votes
          Candidate lostBallot = getCandidateFromBallot(ballotKey);
          Candidate wonBallot= getCandidateFromBallot(replacement);
          if (lostBallot != wonBallot) {
            wonBallot.incrementCurNumVotes((int)pair.getValue());
          }

        }
        willBeRemoved.add(ballotKey);
      }
    }
    for (Object[] addable : willBeAdded) {
      int currentVal = ballots.getOrDefault((String) addable[0], 0);
      ballots.put((String) addable[0], currentVal + (int) addable[1]);
    }
    for (String removable : willBeRemoved) {
      ballots.remove(removable);
    }

    // Lastly remove the candidate
    candidates.remove(c);
  }

  /**
   * Takes the current roundCount formats the string for the auditString and electionStatistics
   * (i.e., 2 becomes 2nd, 3 becomes 3rd, etc.)
   *
   * @param n the current round
   * @return Formatted string for nth round count
   */
  protected String addNCountToStatistics(int n) {
    if (n == 1) {
      return "1st Count";
    } else if (n == 2) {
      return "2nd Count";
    } else if (n == 3) {
      return "3rd Count";
    } else {
      return n + "th Count";
    }
  }

  /**
   * Creates the audit file and outputs the auditString to the new file.
   */
  protected void generateAuditFile() throws FileNotFoundException {
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
    LocalDateTime currentTime = LocalDateTime.now();
    String filename = "IRAuditFile_".concat(dateFormat.format(currentTime)).concat(".txt");

    PrintWriter out = new PrintWriter(new File(filename));
    out.print(auditString.toString());
    out.print("\n");
    out.print(finalNotes);
    out.close();
  }

  /**
   * Creates the media file and outputs the election results, seat election statistics,
   * and final Notes to the new file.
   */
  protected void generateMediaFile() throws FileNotFoundException {
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
    LocalDateTime currentTime = LocalDateTime.now();
    String filename = "IRMediaReport_".concat(dateFormat.format(currentTime)).concat(".txt");

    PrintWriter out = new PrintWriter(new File(filename));
    out.println(electionResults);
    out.println(electionStatistics);
    out.println(finalNotes);
    out.close();
  }

  /**
   * Outputs election Statistics and Final notes to the screen.
   */
  protected void outputResults() {
    System.out.println(electionResults);
    System.out.println(electionStatistics);
    System.out.println(finalNotes);
  }

  /**
   * Helper Function that finds the number 1 ranked candidate given a ballot as
   * a string.
   * @param ballot the ballot in the form of a string
   * @return the number 1 ranked candidate in the ballot
   */
  private Candidate getCandidateFromBallot(String ballot) {

    StringBuilder partyString = new StringBuilder();
    for (int i = 1; i < ballot.length(); i++) {
      char c = ballot.charAt(i);
      if (c == ')') {
        break;
      }
      partyString.append(c);

    }
    for (Candidate candidate : candidates) {
      if (candidate.getParty().equals(partyString.toString())) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * This function is called by the constructor to initialize the current number of votes for
   * each candidate based on the hashmap of ballots given as an input to the constructor
   */
  private void setCandidateOriginalVotes() {
    // reset all candidate votes to zero since we are recounting if not counted already
    for (Candidate candidate: candidates) {
      candidate.setCurNumVotes(0);
    }

    // iterate through each element in hashmap
    Iterator ballotsIterator = ballots.entrySet().iterator();
    while (ballotsIterator.hasNext()) {
      Map.Entry pair = (Map.Entry) ballotsIterator.next();
      String ballotKey = pair.getKey().toString();
      // find first choice candidate in hashmap
      Candidate firstChoice = getCandidateFromBallot(ballotKey);
      // increment first choice candidate
      firstChoice.incrementCurNumVotes((int) pair.getValue());
    }

  }

  /**
   * Gets the array list containing the candidates currently in the election
   * @return the candidate array list
   */
  public ArrayList<Candidate> getCandidates() {
    return candidates;
  }

  /**
   * Gets the hashmap containing all of the ballots
   * @return  the hashmap containing all of the ballots
   */
  public HashMap<String, Integer> getBallots() {
    return ballots;
  }

  /**
   * Gets the current total Number of Votes in the election
   * @return the total number of votes
   */
  public int getTotalCounts() {
    return totalCounts;
  }

  /**
   * Returns the winner of the election. Will be null before the algorithm is run.
   * @return The winner of the IR election.
   */
  public Candidate getWinner() {
    return winner;
  }

}


import java.io.FileNotFoundException;
import java.util.Random;

/**
 * An abstract class that every Election algorithm will inherit.
 *
 * @author Justin Lam
 */
public abstract class Election {

  /**
   * The total number of ballots cast in the election.
   */
  protected int totalNumBallots;

  /**
   * An string builder that will be used to generate the audit file as the election is run.
   */
  protected StringBuilder auditString;

  /**
   * Handles a tie between at least two candidates by generating a random number choosing one of them.
   *
   * @param numCandidates the number of candidates that are tied in votes.
   * @return the chosen candidate to settle the tie.
   */
  protected static int handleTie(int numCandidates) {
    Random r = new Random();
    return r.nextInt(numCandidates);
  }

  /**
   * Runs the specified voting algorithm. This should output the results to the display, as well
   * as generate media and audit files.
   */
  public abstract void runVotingAlgorithm();

  /**
   * Generates the audit file for the election.
   * @throws FileNotFoundException If the file was not created successfully
   */
  protected abstract void generateAuditFile() throws FileNotFoundException;

  /**
   * Generates the media file for the election.
   * @throws FileNotFoundException If the file was not created successfully
   */
  protected abstract void generateMediaFile() throws FileNotFoundException;

  /**
   * Outputs the election results to the standard output.
   */
  protected abstract void outputResults();

}

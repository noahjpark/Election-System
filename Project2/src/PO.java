import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Dummy PO class that just stores the information for PO but doesn't actually run any solving algorithm.
 */
public class PO extends Election {

  int totalNumBallots;
  ArrayList<Party> parties;

  /**
   * Default constructor.
   *
   * @param totalNumBallots total ballots for the PO election.
   * @param parties parties participating in the PO election.
   */
  public PO(int totalNumBallots, ArrayList<Party> parties) {
    this.totalNumBallots = totalNumBallots;
    this.parties = parties;
  }

  /**
   * Gets the total ballots involved.
   *
   * @return total number of ballots.
   */
  public int getTotalNumBallots() {
    return totalNumBallots;
  }

  /**
   * Gets the parties involved.
   *
   * @return parties involved as a list.
   */
  public ArrayList<Party> getParties() {
    return parties;
  }

  /**
   * Dummy method.
   */
  public void runVotingAlgorithm() {

  }

  /**
   * Dummy method.
   *
   * @throws FileNotFoundException dummy exception.
   */
  protected void generateAuditFile() throws FileNotFoundException {

  }

  /**
   * Dummy method.
   *
   * @throws FileNotFoundException dummy exception.
   */
  protected void generateMediaFile() throws FileNotFoundException {

  }

  /**
   * Dummy method.
   */
  protected void outputResults() {

  }

}

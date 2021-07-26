/**
 * Candidate Class representing individual candidates participating in the election.
 *
 * @author Noah Park
 */
public class Candidate {

  /**
   * The name of the candidate
   */
  private String name;

  /**
   * The current number of votes the candidate has
   */
  private int curNumVotes;

  /**
   * A unique ID assigned to the candidate
   */
  private int candidateID;

  /**
   * The name of the candidates party
   */
  private String party;

  /**
   * Candidate constructor initializes Candidate object.
   *
   * @param name candidate name.
   * @param party party name.
   * @param candidateID candidate id.
   * @throws IllegalArgumentException if name is null, 0 in length, party is null, or 0 in length.
   */
  public Candidate(String name, String party, int candidateID) throws IllegalArgumentException {
    if (name == null || party == null || name.length() == 0 || party.length() == 0)
      throw new IllegalArgumentException("Name cannot be null or 0 in length. Party cannot be null or 0 in length.");

    this.name = name;
    this.party = party;
    this.candidateID = candidateID;
    curNumVotes = 0;
  }

  /**
   * Gets the name of the candidate.
   *
   * @return candidate name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the id of the candidate.
   *
   * @return candidate id.
   */
  public int getCandidateID() {
    return candidateID;
  }

  /**
   * Gets the party the candidate is associated with.
   *
   * @return candidate party.
   */
  public String getParty() {
    return party;
  }

  /**
   * Gets the current number of votes the candidate has.
   *
   * @return candidate vote count.
   */
  public int getCurNumVotes() {
    return curNumVotes;
  }

  /**
   * Sets the current number of votes the candidate will have.
   *
   * @param votes total votes candidate will have.
   * @throws IllegalArgumentException if votes is negative.
   */
  public void setCurNumVotes(int votes) throws IllegalArgumentException {
    if (votes < 0) throw new IllegalArgumentException("Votes cannot be negative.");

    curNumVotes = votes;
  }

  /**
   * Increments curNumVotes for the particular candidate by the votes argument.
   *
   * @param votes amount to increment curNumVotes by.
   * @throws IllegalArgumentException if votes is negative.
   */
  public void incrementCurNumVotes(int votes) throws IllegalArgumentException {
    if (votes < 0) throw new IllegalArgumentException("Votes cannot be negative.");

    curNumVotes += votes;
  }
}

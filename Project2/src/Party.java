import java.util.ArrayList;

/**
 * Party class represents the parties participating in the election.
 *
 * @author Noah Park
 */
public class Party {
  /**
   * The name of the party
   */
  private String name;

  /**
   * A list of candidates that are a part of this party
   */
  private ArrayList<Candidate> candidates;

  /**
   * The total votes that the party has earned in an election
   */
  private int totalVotes;

  /**
   * The number of votes remaining for this party if running the OPL algorithm.
   */
  private int remainingVotes;

  /**
   * The number of seats the party has earned in an OPL election.
   */
  private int numberOfSeats;

  /**
   * Party constructor initializes party object.
   *
   * @param name party name.
   * @throws IllegalArgumentException if party name is null or empty.
   */
  public Party(String name) throws IllegalArgumentException {
    if (name == null || name.length() == 0) throw new IllegalArgumentException("Party name must not be null and have at least one character.");

    this.name = name;
    candidates = new ArrayList<>();
    totalVotes = 0;
    remainingVotes = 0;
    numberOfSeats = 0;
  }

  /**
   * Gets the name of the party.
   *
   * @return party name.
   */
  public String getName() {
    return name;
  }

  /**
   * Adds a candidate to the party's candidate list.
   *
   * @param candidate associated candidate.
   * @throws IllegalArgumentException if candidate is null.
   */
  public void addCandidate(Candidate candidate) throws IllegalArgumentException {
    if (candidate == null) throw new IllegalArgumentException("Candidate cannot be null.");

    candidates.add(candidate);
  }

  /**
   * Gets the candidates list.
   *
   * @return list of candidates.
   */
  public ArrayList<Candidate> getCandidates() {
    return candidates;
  }

  /**
   * Gets the total votes the party has.
   *
   * @return total votes for the party.
   */
  public int getTotalVotes() {
    return totalVotes;
  }

  /**
   * Gets remaining votes the party has.
   *
   * @return remaining votes for the party.
   */
  public int getRemainingVotes() {
    return remainingVotes;
  }

  /**
   * Gets the names of all of the party's candidates as a list.
   *
   * @return list of candidate names.
   */
  public ArrayList<String> getCandidateNames() {
    ArrayList<String> names = new ArrayList<>();

    for (Candidate candidate : candidates) names.add(candidate.getName());

    return names;
  }

  /**
   * Sets the total votes for the party.
   *
   * @param votes the total votes for the party.
   * @throws IllegalArgumentException if votes is negative.
   */
  public void setTotalVotes(int votes) throws IllegalArgumentException {
    if (votes < 0) throw new IllegalArgumentException("Votes cannot be negative.");

    totalVotes = votes;
  }

  /**
   * Sets the remaining votes for the party.
   *
   * @param votes the remaining votes for the party.
   * @throws IllegalArgumentException if votes is negative.
   */
  public void setRemainingVotes(int votes) throws IllegalArgumentException {
    if (votes < 0) throw new IllegalArgumentException("Votes cannot be negative.");

    remainingVotes = votes;
  }

  /**
   * Gets the number of seats the party has.
   *
   * @return number of seats for the party.
   */
  public int getNumberOfSeats() {
    return numberOfSeats;
  }

  /**
   * Sets the number of seats the party has.
   *
   * @param seats number of seats for the party.
   * @throws IllegalArgumentException if seats is negative.
   */
  public void setNumberOfSeats(int seats) throws IllegalArgumentException {
    if (seats < 0) throw new IllegalArgumentException("Seats cannot be negative.");

    numberOfSeats = seats;
  }

  /**
   * Gets the number of candidates associated with the party.
   *
   * @return number of candidates associated with the party.
   */
  public int getNumCandidates() {
    return candidates.size();
  }

}

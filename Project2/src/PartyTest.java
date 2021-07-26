import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Party Class Unit Tests
 *
 * These tests are fully automatic and do not require any manual steps.
 *
 * @author Noah Park
 */
public class PartyTest {

  /**
   * Tests normal Party constructor.
   */
  @Test
  public void testPartyConstructor() {
    Party party = new Party("test");
    assertNotEquals(null, party);
    assertEquals("test", party.getName());
    assertEquals(0, party.getCandidates().size());
    assertEquals(0, party.getTotalVotes());
    assertEquals(0, party.getRemainingVotes());
    assertEquals(0, party.getNumberOfSeats());
  }

  /**
   * Tests exception thrown from constructor on null name argument.
   */
  @Test
  public void testPartyConstructorThrowsExceptionNullName() {
    try {
      Party party = new Party(null);
      assertNull(party);
    } catch (IllegalArgumentException e) {
      assertEquals("Party name must not be null and have at least one character.", e.getMessage());
    }
  }

  /**
   * Tests exception thrown from constructor on empty name argument.
   */
  @Test
  public void testPartyConstructorThrowsExceptionEmptyName() {
    try {
      Party party = new Party("");
      assertNull(party);
    } catch (IllegalArgumentException e) {
      assertEquals("Party name must not be null and have at least one character.", e.getMessage());
    }
  }

  /**
   * Tests get name method.
   */
  @Test
  public void testGetName() {
    Party party = new Party("test");
    assertEquals("test", party.getName());
  }

  /**
   * Tests add candidate method to ensure candidates are added to the candidates list.
   */
  @Test
  public void testAddCandidate() {
    Party party = new Party("test");
    Candidate candidateOne = new Candidate("candidate1", "test", 1);
    Candidate candidateTwo = new Candidate("candidate2", "test", 2);
    Candidate candidateThree = new Candidate("candidate3", "test", 3);

    party.addCandidate(candidateOne);
    assertEquals(1, party.getCandidates().size());
    assertEquals(candidateOne, party.getCandidates().get(0));

    party.addCandidate(candidateTwo);
    assertEquals(2, party.getCandidates().size());
    assertEquals(candidateTwo, party.getCandidates().get(1));

    party.addCandidate(candidateThree);
    assertEquals(3, party.getCandidates().size());
    assertEquals(candidateThree, party.getCandidates().get(2));
  }

  /**
   * Tests exception thrown from add candidate method on null candidate argument.
   */
  @Test
  public void testAddCandidateThrowsException() {
    try {
      Party party = new Party("test");
      party.addCandidate(null);
      fail(); // shouldn't get to this point. If it does, something went wrong
    } catch (IllegalArgumentException e) {
      assertEquals("Candidate cannot be null.", e.getMessage());
    }
  }

  /**
   * Tests get candidates method.
   */
  @Test
  public void testGetCandidates() {
    Party party = new Party("test");
    Candidate candidateOne = new Candidate("candidate1", "test", 1);
    Candidate candidateTwo = new Candidate("candidate2", "test", 2);
    Candidate candidateThree = new Candidate("candidate3", "test", 3);

    party.addCandidate(candidateOne);
    party.addCandidate(candidateTwo);
    party.addCandidate(candidateThree);

    ArrayList<Candidate> testList = new ArrayList<>();
    testList.add(candidateOne);
    testList.add(candidateTwo);
    testList.add(candidateThree);

    assertEquals(testList, party.getCandidates());
  }

  /**
   * Tests set total votes method and get total votes method.
   */
  @Test
  public void testSetAndGetTotalVotes() {
    Party party = new Party("test");
    party.setTotalVotes(1000);
    assertEquals(1000, party.getTotalVotes());
  }

  /**
   * Tests exception thrown from set total votes method on negative votes argument.
   */
  @Test
  public void testSetTotalVotesException() {
    try {
      Party party = new Party("test");
      party.setTotalVotes(-1);
      fail(); // shouldn't get to this point. If it does, something went wrong
    } catch (IllegalArgumentException e) {
      assertEquals("Votes cannot be negative.", e.getMessage());
    }
  }

  /**
   * Tests set remaining votes method and get remaining votes method.
   */
  @Test
  public void testSetAndGetRemainingVotes() {
    Party party = new Party("test");
    party.setRemainingVotes(1000);
    assertEquals(1000, party.getRemainingVotes());
  }

  /**
   * Tests exception thrown from set remaining votes method on negative votes argument.
   */
  @Test
  public void testSetRemainingVotesException() {
    try {
      Party party = new Party("test");
      party.setRemainingVotes(-1);
      fail(); // shouldn't get to this point. If it does, something went wrong
    } catch (IllegalArgumentException e) {
      assertEquals("Votes cannot be negative.", e.getMessage());
    }
  }

  /**
   * Tests set number of seats method and get number of seats method.
   */
  @Test
  public void testSetAndGetNumberOfSeats() {
    Party party = new Party("test");
    party.setNumberOfSeats(10);
    assertEquals(10, party.getNumberOfSeats());
  }

  /**
   * Tests exception thrown from set number of seats method on negative votes argument.
   */
  @Test
  public void testSetNumberOfSeatsException() {
    try {
      Party party = new Party("test");
      party.setNumberOfSeats(-1);
      fail(); // shouldn't get to this point. If it does, something went wrong
    } catch (IllegalArgumentException e) {
      assertEquals("Seats cannot be negative.", e.getMessage());
    }
  }

  /**
   * Tests get number of candidates method to ensure the correct number of candidates are added to the candidates list.
   */
  @Test
  public void testGetNumCandidates() {
    Party party = new Party("test");
    Candidate candidateOne = new Candidate("candidate1", "test", 1);
    Candidate candidateTwo = new Candidate("candidate2", "test", 2);
    Candidate candidateThree = new Candidate("candidate3", "test", 3);

    party.addCandidate(candidateOne);
    party.addCandidate(candidateTwo);
    party.addCandidate(candidateThree);

    assertEquals(3, party.getNumCandidates());
  }

  /**
   * Tests get candidate names method to ensure all candidate names are output correctly.
   */
  @Test
  public void testGetCandidateNames() {
    Party party = new Party("test");
    Candidate candidateOne = new Candidate("candidate1", "test", 1);
    Candidate candidateTwo = new Candidate("candidate2", "test", 2);
    Candidate candidateThree = new Candidate("candidate3", "test", 3);

    party.addCandidate(candidateOne);
    party.addCandidate(candidateTwo);
    party.addCandidate(candidateThree);

    ArrayList<String> testList = new ArrayList<>();
    testList.add("candidate1");
    testList.add("candidate2");
    testList.add("candidate3");

    assertEquals(testList, party.getCandidateNames());
  }

}

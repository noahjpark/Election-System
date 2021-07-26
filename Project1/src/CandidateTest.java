import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Candidate Class Unit Tests
 *
 * These tests are fully automatic and do not require any manual steps.
 *
 * @author Noah Park
 */
public class CandidateTest {

  /**
   * Tests normal Candidate constructor.
   */
  @Test
  public void testCandidateConstructor() {
    Candidate candidate = new Candidate("John Doe", "test", 1);
    assertNotEquals(null, candidate);
    assertEquals("John Doe", candidate.getName());
    assertEquals("test", candidate.getParty());
    assertEquals(1, candidate.getCandidateID());
    assertEquals(0, candidate.getCurNumVotes());
  }

  /**
   * Tests exception thrown from constructor on null name argument.
   */
  @Test
  public void testCandidateConstructorThrowsExceptionNullName() {
    try {
      Candidate candidate = new Candidate(null, "test", 1);
      assertNotEquals(null, candidate);
    } catch (IllegalArgumentException e ) {
      assertEquals("Name cannot be null or 0 in length. Party cannot be null or 0 in length.", e.getMessage());
    }
  }

  /**
   * Tests exception thrown from constructor on empty name argument.
   */
  @Test
  public void testCandidateConstructorThrowsExceptionEmptyName() {
    try {
      Candidate candidate = new Candidate("", "test", 1);
      assertNotEquals(null, candidate);
    } catch (IllegalArgumentException e ) {
      assertEquals("Name cannot be null or 0 in length. Party cannot be null or 0 in length.", e.getMessage());
    }
  }

  /**
   * Tests exception thrown from constructor on null party argument.
   */
  @Test
  public void testCandidateConstructorThrowsExceptionNullParty() {
    try {
      Candidate candidate = new Candidate("John Doe", null, 1);
      assertNotEquals(null, candidate);
    } catch (IllegalArgumentException e ) {
      assertEquals("Name cannot be null or 0 in length. Party cannot be null or 0 in length.", e.getMessage());
    }
  }

  /**
   * Tests exception thrown from constructor on empty party argument.
   */
  @Test
  public void testCandidateConstructorThrowsExceptionEmptyParty() {
    try {
      Candidate candidate = new Candidate("John Doe", "", 1);
      assertNotEquals(null, candidate);
    } catch (IllegalArgumentException e ) {
      assertEquals("Name cannot be null or 0 in length. Party cannot be null or 0 in length.", e.getMessage());
    }
  }

  /**
   * Tests get name method.
   */
  @Test
  public void testGetName() {
    Candidate candidate = new Candidate("John Doe", "test", 1);
    assertEquals("John Doe", candidate.getName());
  }

  /**
   * Tests get ID method.
   */
  @Test
  public void testGetCandidateID() {
    Candidate candidate = new Candidate("John Doe", "test", 1);
    assertEquals(1, candidate.getCandidateID());
  }

  /**
   * Tests get party method.
   */
  @Test
  public void testGetParty() {
    Candidate candidate = new Candidate("John Doe", "test", 1);
    assertEquals("test", candidate.getParty());
  }

  /**
   * Tests set number of votes and get number of votes methods.
   */
  @Test
  public void testSetAndGetCurNumVotes() {
    Candidate candidate = new Candidate("John Doe", "test", 1);
    candidate.setCurNumVotes(1000);
    assertEquals(1000, candidate.getCurNumVotes());
  }

  /**
   * Tests exception thrown from set number of votes method on negative votes argument.
   */
  @Test
  public void testSetCurNumVotesException() {
    try {
      Candidate candidate = new Candidate("John Doe", "test", 1);
      candidate.setCurNumVotes(-1);
    } catch (IllegalArgumentException e) {
      assertEquals("Votes cannot be negative.", e.getMessage());
    }
  }

  /**
   * Tests increment num votes method.
   */
  @Test
  public void testIncrementCurNumVotes() {
    Candidate candidate = new Candidate("John Doe", "test", 1);
    candidate.setCurNumVotes(1000);
    candidate.incrementCurNumVotes(1000);
    assertEquals(2000, candidate.getCurNumVotes());
  }

  /**
   * Tests exception thrown from increment number of votes method on negative votes argument.
   */
  @Test
  public void testIncrementCurNumVotesException() {
    try {
      Candidate candidate = new Candidate("John Doe", "test", 1);
      candidate.incrementCurNumVotes(-1);
    } catch (IllegalArgumentException e) {
      assertEquals("Votes cannot be negative.", e.getMessage());
    }
  }
}

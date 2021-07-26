import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the PO election type.
 *
 * @author Noah Park.
 */
public class POTest {

  final private String testingPath = "./testing/testFiles/";

  /**
   * Tests multiple PO csv files at once.
   */
  @Test
  public void TestMultiplePOFiles() {
    Election po = CreateElection.createElection(new String[]{ testingPath + "examplePO.csv" , testingPath + "examplePO2.csv"});
    assertTrue(po instanceof PO);
    assertEquals(18, ((PO) po).getTotalNumBallots());

    ArrayList<Party> parties = ((PO) po).getParties();

    for (Party p : parties) {
      for (Candidate c : p.getCandidates()) {
        switch (p.getName()) {
          case "D":
            assertEquals(10, p.getTotalVotes());
            switch (c.getName()) {
              case "Pike":
                assertEquals(6, c.getCurNumVotes());
                break;
              case "Foster":
                assertEquals(4, c.getCurNumVotes());
                break;
              default:
                fail();
            }
            break;
          case "R":
            assertEquals(6, p.getTotalVotes());
            switch (c.getName()) {
              case "Deutsch":
                assertEquals(0, c.getCurNumVotes());
                break;
              case "Borg":
                assertEquals(4, c.getCurNumVotes());
                break;
              case "Jones":
                assertEquals(2, c.getCurNumVotes());
                break;
              default:
                fail();
            }
            break;
          case "I":
            assertEquals(2, p.getTotalVotes());
            assertEquals(2, c.getCurNumVotes());
            break;
          default:
            fail();
        }
      }
    }
  }

  /**
   * Tests an election file with no ballots, candidates, or parties.
   */
  @Test
  public void TestEmptyPO() {
    Election po = CreateElection.createElection(new String[]{ testingPath + "EmptyPO.csv" });
    assertNull(po);
  }

  /**
   * Tests the creation of a PO election instance with correct values for the example PO csv file.
   */
  @Test
  public void TestExamplePO() {
    Election po = CreateElection.createElection(new String[]{ testingPath + "examplePO.csv" });
    assertTrue(po instanceof PO);
    assertEquals(9, ((PO) po).getTotalNumBallots());

    ArrayList<Party> parties = ((PO) po).getParties();

    for (Party p : parties) {
      for (Candidate c : p.getCandidates()) {
        switch (p.getName()) {
          case "D":
            assertEquals(5, p.getTotalVotes());
            switch (c.getName()) {
              case "Pike":
                assertEquals(3, c.getCurNumVotes());
                break;
              case "Foster":
                assertEquals(2, c.getCurNumVotes());
                break;
              default:
                fail();
            }
            break;
          case "R":
            assertEquals(3, p.getTotalVotes());
            switch (c.getName()) {
              case "Deutsch":
                assertEquals(0, c.getCurNumVotes());
                break;
              case "Borg":
                assertEquals(2, c.getCurNumVotes());
                break;
              case "Jones":
                assertEquals(1, c.getCurNumVotes());
                break;
              default:
                fail();
            }
            break;
          case "I":
            assertEquals(1, p.getTotalVotes());
            assertEquals(1, c.getCurNumVotes());
            break;
          default:
            fail();
        }
      }
    }
  }

  /**
   * Tests the creation of a PO election instance with correct values for the large PO csv file.
   */
  @Test
  public void TestPO() {
    Election po = CreateElection.createElection(new String[]{ testingPath + "/DifferentElectionTypesFiles/PO.csv" });
    assertTrue(po instanceof PO);
    assertEquals(100000, ((PO) po).getTotalNumBallots());

    ArrayList<Party> parties = ((PO) po).getParties();

    for (Party p : parties) {
      for (Candidate c : p.getCandidates()) {
        switch (p.getName()) {
          case "D":
            assertEquals(50000, p.getTotalVotes());
            switch (c.getName()) {
              case "Pike":
                assertEquals(50000, c.getCurNumVotes());
                break;
              case "Foster":
                assertEquals(0, c.getCurNumVotes());
                break;
              default:
                fail();
            }
            break;
          case "R":
            assertEquals(0, p.getTotalVotes());
            switch (c.getName()) {
              case "Deutsch":
              case "Borg":
              case "Jones":
                assertEquals(0, c.getCurNumVotes());
                break;
              default:
                fail();
            }
            break;
          case "I":
            assertEquals(50000, p.getTotalVotes());
            assertEquals(50000, c.getCurNumVotes());
            break;
          default:
            fail();
        }
      }
    }
  }

  /**
   * Tests the creation of a PO election instance with non PO csv files.
   */
  @Test
  public void TestNotPO() {
    Election notPO = CreateElection.createElection(new String[]{ testingPath + "givenIR.csv" });
    assertFalse(notPO instanceof PO);

    notPO = CreateElection.createElection(new String[]{ testingPath + "givenOPL.csv" });
    assertFalse(notPO instanceof PO);

    notPO = CreateElection.createElection(new String[]{ testingPath + "bigIR.csv" });
    assertFalse(notPO instanceof PO);

    notPO = CreateElection.createElection(new String[]{ testingPath + "bigOPL.csv" });
    assertFalse(notPO instanceof PO);

    notPO = CreateElection.createElection(new String[]{ testingPath + "bigRandomIR.csv" });
    assertFalse(notPO instanceof PO);

    notPO = CreateElection.createElection(new String[]{ testingPath + "invalidElectionType.csv" });
    assertFalse(notPO instanceof PO);
  }

}

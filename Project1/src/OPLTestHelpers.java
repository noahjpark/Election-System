import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Helper functions used by both OPLTest and OPLManualTest
 *
 * @author Michael Markiewicz
 */
public class OPLTestHelpers {
  /**
   * A helper function for the unit tests that will delete any instance of a Media Report or an OPL
   * Audit File from the current working directory
   */
  public static void deleteElectionOutputFiles() {
    File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
    File[] listOfFiles = currentDirectory.listFiles();
    String currentFileName;
    String[] currentSplit;
    for (File file: listOfFiles) {
      currentFileName = file.getName();
      currentSplit = currentFileName.split("_");
      if ("OPLMediaReport".equals(currentSplit[0]) || "OPLAuditFile".equals(currentSplit[0])) {
        // delete the files
        file.delete();
      }
    }
  }

  /**
   * Helper function for many different unit tests. Creates a basic party list with candidates
   * populated. Party names will be p0, p1, ..., p&lt;numParties - 1&gt;
   * Candidate names for each party will be
   * c0_&lt;party name&gt;, c1_&lt;party name&gt;, ...,
   * c&lt;numCandidatesPerParty[index] - 1&gt;_&lt;party name>&gt;
   * @param numParties The total parties that will be created.
   * @param numCandidatesPerParty An array of the number of candidates for each party that
   *                              will be created. Should be the same size as numParties
   * @return An ArrayList of Parties that are populated with candidates.
   */
  public static ArrayList<Party> createBasicPartyInstance(int numParties,
                                                          int[] numCandidatesPerParty) {
    // create <numParties> parties with name p0, p1, ..., p<numParties - 1>
    ArrayList<Party> parties = new ArrayList<>();
    for(int i = 0; i < numParties; i++) {
      parties.add(new Party("p".concat(String.valueOf(i))));
    }

    // create <numCandidates> candidates for each party
    int uniqueCandidateID = 0;
    for (int partyNum = 0; partyNum < numParties; partyNum++) {
      for (int i = 0; i < numCandidatesPerParty[partyNum]; i++) {
        Party curParty = parties.get(partyNum);
        curParty.addCandidate(new Candidate("c".concat(String.valueOf(i)).concat("_")
                .concat(curParty.getName()), curParty.getName(), uniqueCandidateID++));
      }
    }

    return parties;
  }

  /**
   * Sets the party and candidate votes to certain values as indicated by parameters.
   *
   * @param parties The parties that we are initializing the votes for.
   * @param partyVotes The array of ints that correspond to the number of votes each party earns.
   *                   For example, partyVotes[i] corresponds to the number of votes that party
   *                   parties.get(i) receives.
   * @param candidateVotes The 2-D array of ints that corresponds to the number of votes each
   *                       candidate gets for each party. For example, candidateVotes[i][j]
   *                       corresponds to the number of votes that candidate
   *                       parties.get(i).getCandidates().get(j) receives.
   */
  public static void setPartyAndCandidateVotes(ArrayList<Party> parties, int[] partyVotes,
                                               int[][] candidateVotes) {
    ArrayList<Candidate> currentCandidates;
    for (int i = 0; i < parties.size(); i++) {
      parties.get(i).setTotalVotes(partyVotes[i]);
      currentCandidates = parties.get(i).getCandidates();
      for (int j = 0; j < currentCandidates.size(); j++) {
        currentCandidates.get(j).setCurNumVotes(candidateVotes[i][j]);
      }
    }
  }

  /**
   * Gets all the contents from a file and returns the contents as a string.
   * Returns null if there was an issue with inputting the file.
   *
   * @param filename Filename of the file you want the contents of
   * @return the contents of the file
   */
  public static String getAllFileContents(String filename) {
    Scanner in = null;
    try {
      in = new Scanner(new File(filename)).useDelimiter("\\Z");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    String result = in.next();
    in.close();
    // remove newlines because they were causing some discrepancies on windows.
    // newlines don't affect our end desired result anyway.
    return result.replaceAll("\n", "").replaceAll("\r", "");
  }

  /**
   * Finds the references to an audit and media file output and returns filenames
   * associated with them. This function assumes there are only one audit and media file in the
   * current directory.
   * Filename will be null if an audit or media file was not found.
   *
   * @return the filename associated with the media and audit file. Arg 0 is media,
   *         and Arg 1 is audit.
   */
  public static String[] findAuditAndMedia() {
    // find the new media and audit files
    String mediaName = null;
    String auditName = null;
    File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
    File[] listOfFiles = currentDirectory.listFiles();
    String currentFileName;
    String[] currentSplit;
    for (File file: listOfFiles) {
      currentFileName = file.getName();
      currentSplit = currentFileName.split("_");
      if ("OPLMediaReport".equals(currentSplit[0])) {
        mediaName = currentFileName;
      } else if ("OPLAuditFile".equals(currentSplit[0])) {
        auditName = currentFileName;
      }
    }

    return new String[] {mediaName, auditName};
  }
}

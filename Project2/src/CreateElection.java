import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit tests for the Create Election Class
 *
 * @author John Foley, Justin Lam, Mohammad Essawy, Noah Park, Michael Markiewicz
 */
public class CreateElection {

  /**
   * This function takes the file name a ballot CSV file
   * and returns the election object based on that file.
   * If the file cannot be found or has incorrect formatting a
   * statement prints and the program exits.
   *
   * @param electionFileNames The name of the CSV file with the election data
   * @return An Election object. In this case an OPL or IR Election Object
   */
  public static Election createElection(String[] electionFileNames) {
    if (electionFileNames == null || electionFileNames.length < 1) {
      System.out.println("Error: There needs to be at least one election files for an election.");
      return null;
    }

    // single buffered reader to read in election type
    BufferedReader electionBufferedReader = openElectionFile(electionFileNames[0]);
    if (electionBufferedReader == null) {
      return null;
    }

    // get the election type
    String strFirstLine;
    try {
      strFirstLine = electionBufferedReader.readLine();
    } catch (IOException ex) {
      System.out.println("Error: Invalid File Format for " + electionFileNames[0]);
      return null;
    }

    // close buffered reader (won't be using anymore)
    try {
      electionBufferedReader.close();
    } catch (IOException e) {
      // something unexpected happened when closing the election file
      e.printStackTrace();
    }

    // call the respective function to create the election
    switch (strFirstLine) {
      case "IR":
        return createIR(electionFileNames);
      case "OPL":
        return createOPL(electionFileNames);
      case "PO":
        return createPO(electionFileNames);
      default:
        System.out.println("Error: Invalid Election Type");
        return null;
    }
  }

  /**
   * This function creates the Invalidated ballots audit file
   * in the correct syntax. Then, it creates a PrintWriter Object
   * and returns it to allow the users of the function to
   * write to the file
   *
   * @return A PrintWriter object that is ready to be apended to
   */
  private static PrintWriter makeFile() throws FileNotFoundException {
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
    LocalDateTime currentTime = LocalDateTime.now();
    String filename = "Invalidated_".concat(dateFormat.format(currentTime)).concat(".txt");

    PrintWriter out = new PrintWriter(new File(filename));
    return out;
  }

  /**
   * This function reads the rest of the file passed to it by
   * the createElection function. It processes the file and
   * returns the IR object based on the file
   *
   * @param electionFileNames This is the list of file names to be read by the user
   * @return An instance of IR based on the specifications of th file
   */
  private static IR createIR(String[] electionFileNames) {
      ArrayList<Candidate> candidateArrayList = electionHeader(electionFileNames[0], ", ", "(\\w+) \\((\\w+)\\)");
      if(candidateArrayList == null){
        System.out.println("Error: Invalid File Format");
        return null;
      }
    try{
      int numberOfCandidates = candidateArrayList.size();

      //Count the rest of the ballots putting results into ballotCounter
      HashMap<String,Integer> ballotCounter = new HashMap<>();
      String currentBallot;
      int totalNumberOfBallots = 0;
      BufferedReader currentFile;
      PrintWriter invalidFileAudit = makeFile();
      int halfCandidates = (numberOfCandidates+1)/2;
      int numInvalidBallots = 0;

      for(int j = 0; j < electionFileNames.length; j++){
        currentFile = openElectionFile(electionFileNames[j]);
        if (currentFile == null) {
          throw new IOException();
        }
        if (!iterateThroughFirstLines(currentFile, 3,"IR")) {
          return null; // stop execution if one of the input files is not valid
        }
        int fileNumberOfBallots = Integer.parseInt(currentFile.readLine());
        totalNumberOfBallots += fileNumberOfBallots;

        for(int i = 0; i < fileNumberOfBallots;i++) {
          currentBallot = currentFile.readLine();
          String[] orderedCandidatesBallot = currentBallot.split(",", -1);
          String[] formattedBallotArray = new String[numberOfCandidates];
          Arrays.fill(formattedBallotArray, "");

          //Checks if the ballot has at least half of the candidates ranked, ignores ballot if it does not.
          int numCandidatesRanked = 0;
          for (String rank : orderedCandidatesBallot) {
            if (!rank.isEmpty()) {
              numCandidatesRanked++;
            }
          }
          if (numCandidatesRanked < halfCandidates) {
            numInvalidBallots++;
            invalidFileAudit.println(currentBallot);
            continue;
          }


          for(int k = 0; k<numberOfCandidates; k++){
            if(!orderedCandidatesBallot[k].isEmpty()){
              int candidatePreferenceNumber = Integer.parseInt(orderedCandidatesBallot[k])-1;
              String candidateParty = candidateArrayList.get(k).getParty();
              formattedBallotArray[candidatePreferenceNumber]=("("+candidateParty+")");
            }
          }
          String formatedBallot = String.join("",formattedBallotArray);
          int currBallotCount = 0;
          if(ballotCounter.containsKey(formatedBallot)){
            currBallotCount = ballotCounter.get(formatedBallot);
          }
          ballotCounter.put(formatedBallot,currBallotCount + 1);
        }
        currentFile.close();
      }
      invalidFileAudit.close();
      totalNumberOfBallots -= numInvalidBallots;
      return new IR(ballotCounter,candidateArrayList,totalNumberOfBallots);
    }
    catch (IOException ex){
      System.out.println("Error: Invalid File Format");
      return null;
    }
  }

  /**
   * This function reads the rest of the files passed to it by
   * the createElection function. It processes each file and
   * returns the OPL object based on all of the files
   *
   * @param electionFileNames This is a list of all the csv input file names
   * @return An instance of OPL based on the specifications of the file
   */
  private static OPL createOPL(String[] electionFileNames) {
    // get important information from the first election file
    ArrayList<Candidate> candidateArrayList = electionHeader(electionFileNames[0], "],\\s?\\[",
            "\\[?(\\w+),(\\w+)]?");
    if(candidateArrayList == null){
      System.out.println("Error: Invalid File Format");
      return null;
    }

    // get the number of seats in the first file
    int totalNumberOfSeats = getTotalSeatsOPL(electionFileNames[0]);
    if (totalNumberOfSeats == -1) {
      System.out.println("Error: Invalid File Format");
      return null;
    } else if (totalNumberOfSeats == 0) {
      System.out.println("Error: An OPL Election cannot be created with 0 seats available.");
      return null;
    }

    // now that we have the list of candidates and parties for the election, we must go through
    // each CSV input file and count up the ballots
    int totalNumberOfBallots = 0; // add each file's count to this tally
    String currentBallot;
    int currentNumOfBallots;
    BufferedReader currentFile;
    for (int i = 0; i < electionFileNames.length; i++) {
      // open up the next file and skip through the header list
      currentFile = openElectionFile(electionFileNames[i]);
      if (currentFile == null) {
        return null;
      }

      // skips first four lines of input file -- should also check if the election types are
      // the same
      if (!iterateThroughFirstLines(currentFile, 4,"OPL")) {
        return null; // stop execution if one of the input files is not valid
      }

      try {
        currentNumOfBallots = Integer.parseInt(currentFile.readLine());
        totalNumberOfBallots += currentNumOfBallots;
        for (int j = 0; j < currentNumOfBallots; j++) {
          currentBallot = currentFile.readLine();
          int voteTo = currentBallot.indexOf("1");
          candidateArrayList.get(voteTo).incrementCurNumVotes(1);
        }
      } catch (IOException ex) {
        System.out.println("Error: Invalid File Format");
      }

      // close current file (done counting up ballots)
      try {
        currentFile.close();
      } catch (IOException e) {
        // something unexpected happened when closing the election file
        e.printStackTrace();
      }
    }

    // now create all the Party and Candidate objects and assign the candidates to their
    // respective parties
    ArrayList<Party> partyArrayList = createPartyListForOPL(candidateArrayList);

    return new OPL(totalNumberOfBallots, totalNumberOfSeats, partyArrayList);
  }

  /**
   * This function reads the rest of the files passed to it by
   * the createElection function. It processes each file and
   * returns the PO object based on all of the files
   *
   * @param fileNames This is a list of file names for all the csv input files
   * @return An instance of PO based on the specifications of the file
   */
  private static PO createPO(String[] fileNames) {
    if (fileNames == null || fileNames.length == 0) return null; // bad stuff

    try {
      ArrayList<Candidate> candidateArrayList = new ArrayList<> (); // candidates list
      ArrayList<Party> partyArrayList = new ArrayList<>();          // party list
      Map<String, Party> partyMap = new HashMap<>();                // party names mapped to their party objects
      int currentCandidateID = 0, totalNumberOfBallots = 0;

      // open first file and get to the candidates/parties line
      BufferedReader currentCSV = openElectionFile(fileNames[0]);
      assert currentCSV != null;
      currentCSV.readLine();
      currentCSV.readLine();

      String candidatesAndPartiesString = currentCSV.readLine(), candidatePartyRegex = "\\[?(\\w+),(\\w+)]?";
      String[] candidatesAndPartiesList = candidatesAndPartiesString.split("],\\s?\\[",0); // split list of the candidates and their parties

      // store all candidates based on the pattern given to us for election files
      Pattern candidatePartyPattern = Pattern.compile(candidatePartyRegex);
      for (String candidateAndParty : candidatesAndPartiesList) {
        Matcher candidatePartyMatcher = candidatePartyPattern.matcher(candidateAndParty);
        if (!candidatePartyMatcher.matches()) throw new IOException();
        candidateArrayList.add(new Candidate(candidatePartyMatcher.group(1), candidatePartyMatcher.group(2), currentCandidateID++));
      }

      // iterate over all election files
      for (String fileName : fileNames) {
        currentCSV = openElectionFile(fileName);
        assert currentCSV != null;

        // get to the total ballots line
        currentCSV.readLine();
        currentCSV.readLine();
        currentCSV.readLine();

        // read in ballots and update candidates accordingly
        int currentNumOfBallots = Integer.parseInt(currentCSV.readLine());
        totalNumberOfBallots += currentNumOfBallots;
        for (int i = 0; i < currentNumOfBallots; i++)
          candidateArrayList.get(currentCSV.readLine().indexOf("1")).incrementCurNumVotes(1);
      }

      // now create all the Party and Candidate objects and assign the candidates to their respective parties
      for (Candidate currentCandidate : candidateArrayList) {
        String candidateParty = currentCandidate.getParty();

        if (!partyMap.containsKey(candidateParty)) {
          Party currentParty = new Party(candidateParty);
          currentParty.addCandidate(currentCandidate);
          currentParty.setTotalVotes(currentCandidate.getCurNumVotes());
          partyArrayList.add(currentParty);
          partyMap.put(currentParty.getName(), currentParty);
        } else {
          int currentTotalPartyVotes = partyMap.get(candidateParty).getTotalVotes();
          partyMap.get(candidateParty).addCandidate(currentCandidate);
          partyMap.get(candidateParty).setTotalVotes(currentTotalPartyVotes + currentCandidate.getCurNumVotes());
        }
      }

      return new PO(totalNumberOfBallots, partyArrayList);
    }
    catch (IOException ex){
      System.out.println("Error: Invalid File Format");
      return null;
    }
  }


  ////////////////////////// Private Helper Functions //////////////////////////////////////////////

  /**
   * Returns the total seats in an OPL election found within an election file
   * @param electionFileName The election file that contains the header information
   * @return the total number of seats in the election. Returns -1 if there is some invalid file
   * format issue.
   */
  private static int getTotalSeatsOPL(String electionFileName) {
    BufferedReader currentFile = openElectionFile(electionFileName);
    try {
      if (currentFile == null) {
        throw new IOException();
      }
      // read through the election type, number of candidates, and candidate list
      currentFile.readLine();
      currentFile.readLine();
      currentFile.readLine();
      return Integer.parseInt(currentFile.readLine());
    } catch (IOException e) {
      return -1;
    }
  }

  /**
   * Processes the header of the Election file given to it
   * @param filePath This is the first file path given in the list of file paths. This function
   *                 will read the election header information from this file.
   * @param initialSplit This is the regex of the initial split on the string of candidates and
   *                     parties
   * @param candidatePartyRegex This is the regex of splitting the candidate from the party itself
   * @return An Arraylist of the Candidates present in the file
   */
  private static ArrayList<Candidate> electionHeader(String filePath, String initialSplit,
                                                     String candidatePartyRegex) {
    BufferedReader currentFile = openElectionFile(filePath);
    try {
      if (currentFile == null) {
        throw new IOException();
      }
      currentFile.readLine(); // skip over election type

      // read in the parties and candidates
      int numberOfCandidates = Integer.parseInt(currentFile.readLine());
      String candidatesAndPartiesString = currentFile.readLine();

      //Processes the Candidates and Parties read in
      String[] candidatesAndPartiesList = candidatesAndPartiesString.split(initialSplit, 0);
      ArrayList<Candidate> candidateArrayList = new ArrayList<>();

      /*Trying to match String "Candidate (Party)" capturing
      "Candidate" and "Party"*/
      Pattern candidatePartyPattern = Pattern.compile(candidatePartyRegex);
      getCandidatesFromString(candidatesAndPartiesList, candidateArrayList, candidatePartyPattern);
      currentFile.close();
      return candidateArrayList;
    }
    catch (IOException ex){
      return null;
    }
  }

  /**
   * Opens up the election filename passed in a parameter and checks for invalid filename.
   *
   * @param filename the Election CSV filename
   * @return The instance of a BufferedReader for the file. Null if the file is not found.
   */
  private static BufferedReader openElectionFile(String filename) {
    try {
      // single buffered reader to read in election type
      return new BufferedReader(
              new FileReader(filename));
    } catch (FileNotFoundException ex) {
      System.out.println("Error: File (" + filename + ") Not Found");
    }
    return null;
  }

  /**
   * Iterates through a file a certain number of lines (essentially skipping them).
   * Also makes sure the election type is the one that is expected.
   * The numLines should be at least 1 so the election type can be read in and checked.
   * @param currentFile The file that is iterated through
   * @param numLines The number of lines to skip
   * @param electionType The election type as a string
   * @return true the lines were iterated through and the election type was correct, false otherwise
   */
  private static boolean iterateThroughFirstLines(BufferedReader currentFile, int numLines, String
          electionType) {
    if (numLines < 1) {
      // don't iterate through anything
      return true;
    }
    try {
      // ensure the election type is the one that is expected
      String currentElection = currentFile.readLine();
      if (!currentElection.equals(electionType)) {
        System.out.println("Error: The election type of one of the input files is not the same as" +
                " the expected");
        return false; // shouldn't continue with invalid file
      }

      // skip the remaining lines
      for (int i = 1; i < numLines; i++) {
        currentFile.readLine();
      }
      return true;
    } catch (IOException ex){
      System.out.println("Error: Invalid File Format");
      return false; // shouldn't continue with invalid file
    }
  }

  /**
   * Goes through all the candidates in an OPL election and groups them into parties
   * @param candidateArrayList The candidates in the OPL Election
   * @return The parties of candidates in the OPL election
   */
  private static ArrayList<Party> createPartyListForOPL(ArrayList<Candidate> candidateArrayList) {
    ArrayList<Party> partyArrayList = new ArrayList<>();
    for (Candidate currentCandidate : candidateArrayList) {
      String candidateParty = currentCandidate.getParty();
      boolean newParty = true;
      for (Party currentParty: partyArrayList) {
        if (currentParty.getName().equals(candidateParty)) {
          newParty = false;
          int currentTotalPartyVotes = currentParty.getTotalVotes();
          currentParty.addCandidate(currentCandidate);
          currentParty.setTotalVotes(currentTotalPartyVotes + currentCandidate.getCurNumVotes());
          break;
        }
      }
      if (newParty) {
        Party currentParty = new Party(candidateParty);
        currentParty.addCandidate(currentCandidate);
        currentParty.setTotalVotes(currentCandidate.getCurNumVotes());
        partyArrayList.add(currentParty);
      }
    }

    return partyArrayList;
  }

  /**
   * Parses through a string to obtain the candidates and their parties based on the formatted
   * expression passed in. Will also give each candidate a unique ID starting from 0.
   * @param candidatesAndPartiesList The list of candidates and their parties in some predefined
   *                                 format
   * @param candidateArrayList A list of the candidates in the election (should already be
   *                           initialized)
   * @param candidatePartyPattern The predefined pattern of how parties and the candidates are
   *                              split up.
   * @throws IOException If the file does not match the expected format
   */
  private static void getCandidatesFromString(String[] candidatesAndPartiesList,
                                              ArrayList<Candidate> candidateArrayList,
                                              Pattern candidatePartyPattern) throws IOException {
    int currentCandidateID = 0;
    for (String candidateAndParty : candidatesAndPartiesList) {
      Matcher candidatePartyMatcher = candidatePartyPattern.matcher(candidateAndParty);
      if (!candidatePartyMatcher.matches()) {
        throw new IOException();
      }
      String candidateName = candidatePartyMatcher.group(1);
      String candidateParty = candidatePartyMatcher.group(2);
      candidateArrayList.add(new Candidate(candidateName, candidateParty, currentCandidateID));
      currentCandidateID++;
    }
  }
}

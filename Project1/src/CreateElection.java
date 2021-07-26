import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit tests for the Create Election Class
 *
 * @author John Foley
 */
public class CreateElection {

  /**
   * This function takes the file name a ballot CSV file
   * and returns the election object based on that file.
   * If the file cannot be found or has incorrect formatting a
   * statement prints and the program exits.
   *
   * @param electionFileName The name of the CSV file with the election data
   * @return An Election object. In this case an OPL or IR Election Object
   */
  public static Election createElection(String electionFileName) {
    try{
      BufferedReader electionBufferedReader = new BufferedReader(
                                              new FileReader(electionFileName));
      String strFirstLine;
      strFirstLine = electionBufferedReader.readLine();
      switch (strFirstLine){
        case "IR":
          return createIR(electionBufferedReader);
        case "OPL":
          return createOPL(electionBufferedReader);
        default:
          System.out.println("Error: Invalid Election Type");
          return null;
      }
    }catch (IOException ex){
      if(ex instanceof FileNotFoundException){
        System.out.println("Error: File Not Found");
      }
      else{
        System.out.println("Error: Invalid File Format");
      }
      return null;
    }
  }

  /**
   * This function reads the rest of the file passed to it by
   * the createElection function. It processes the file and
   * returns the IR object based on the file
   *
   * @param electionBufferReader This is the buffer reader of the file
   *                             passed to create election
   * @return An instance of IR based on the specifications of th file
   */
  private static IR createIR(BufferedReader electionBufferReader) {
    try{
      int numberOfCandidates = Integer.parseInt(electionBufferReader.readLine());
      String candidatesAndPartiesString = electionBufferReader.readLine();

      //Processes the Candidates and Parties read in
      String[] candidatesAndPartiesList = candidatesAndPartiesString.split(", ",0);
      ArrayList<Candidate> candidateArrayList = new ArrayList<> ();
      int currentCandidateID = 0;
      /*Trying to match String "Candidate (Party)" capturing
      "Candidate" and "Party"*/
      String candidatePartyRegex = "(\\w+) \\((\\w+)\\)";
      Pattern candidatePartyPattern = Pattern.compile(candidatePartyRegex);
      for(String candidateAndParty: candidatesAndPartiesList){
        Matcher candidatePartyMatcher = candidatePartyPattern.matcher(candidateAndParty);
        if(!candidatePartyMatcher .matches()){
          throw new IOException();
        }
        String candidateName = candidatePartyMatcher.group(1);
        String candidateParty = candidatePartyMatcher.group(2);
        candidateArrayList.add(new Candidate(candidateName,candidateParty,currentCandidateID));
        currentCandidateID++;
      }
      //Count the rest of the ballots putting results into ballotCounter
      HashMap<String,Integer> ballotCounter = new HashMap<>();
      String currentBallot;
      int totalNumberOfBallots = Integer.parseInt(electionBufferReader.readLine());
      for(int i = 0; i <totalNumberOfBallots;i++){
        currentBallot=electionBufferReader.readLine();
        String[] orderedCandidatesBallot = currentBallot.split(",",-1);
        String[] formattedBallotArray = new String[numberOfCandidates];
        Arrays.fill(formattedBallotArray, "");
        for(int j = 0; j<numberOfCandidates; j++){
          if(!orderedCandidatesBallot[j].isEmpty()){
            int candidatePreferenceNumber = Integer.parseInt(orderedCandidatesBallot[j])-1;
            String candidateParty = candidateArrayList.get(j).getParty();
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
      return new IR(ballotCounter,candidateArrayList,totalNumberOfBallots);
    }
    catch (IOException ex){
      System.out.println("Error: Invalid File Format");
      return null;
    }
  }


  /**
   * This function reads the rest of the file passed to it by
   * the createElection function. It processes the file and
   * returns the OPL object based on the file
   *
   * @param electionBufferReader This is the buffer reader of the file
   *                             passed to create election
   * @return An instance of OPL based on the specifications of th file
   */
  private static OPL createOPL(BufferedReader electionBufferReader) {
    try{
      int CtotalNumberOfCandidates = Integer.parseInt(electionBufferReader.readLine());
      String candidatesAndPartiesString = electionBufferReader.readLine();
      //Processes the Candidates and Parties read in
      String[] candidatesAndPartiesList = candidatesAndPartiesString.split("\\],\\s?\\[",0);
      ArrayList<Candidate> candidateArrayList = new ArrayList<> ();
      int currentCandidateID = 0;
      /*Trying to match String "[Candidate,Party]" capturing
      "Candidate" and "Party"*/
      String candidatePartyRegex = "\\[?(\\w+),(\\w+)\\]?";
      Pattern candidatePartyPattern = Pattern.compile(candidatePartyRegex);
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
      String currentBallot;
      int totalNumberOfSeats = Integer.parseInt(electionBufferReader.readLine());
      int totalNumberOfBallots = Integer.parseInt(electionBufferReader.readLine());
      for(int i = 0; i <totalNumberOfBallots;i++){
        currentBallot = electionBufferReader.readLine();
        int voteTo = currentBallot.indexOf("1");
        candidateArrayList.get(voteTo).incrementCurNumVotes(1);
      }
      ArrayList<Party> partyArrayList = new ArrayList<>();
      for(Candidate currentCandidate: candidateArrayList){
        String candidateParty = currentCandidate.getParty();
        boolean newParty = true;
        for(Party currentParty: partyArrayList){
          if(currentParty.getName().equals(candidateParty)){
            newParty = false;
            int currentTotalPartyVotes = currentParty.getTotalVotes();
            currentParty.addCandidate(currentCandidate);
            currentParty.setTotalVotes(currentTotalPartyVotes + currentCandidate.getCurNumVotes());
            break;
          }
        }
        if(newParty){
          Party currentParty = new Party(candidateParty);
          currentParty.addCandidate(currentCandidate);
          currentParty.setTotalVotes(currentCandidate.getCurNumVotes());
          partyArrayList.add(currentParty);
        }
      }
      return new OPL(totalNumberOfBallots,totalNumberOfSeats,partyArrayList);
    }
    catch (IOException ex){
      System.out.println("Error: Invalid File Format");
      return null;
    }
  }

}

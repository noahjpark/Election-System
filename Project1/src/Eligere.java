/**
 * The main driver of the program
 *
 * @author John Foley
 */

public class Eligere {

  /**
   * Driver class for the entire program. If there are
   * the wrong number of argument a help message is
   * given to the user.
   *
   * @param args Args[0] should be the name of the file
   */
  public static void main(String[] args) {
    if (args.length != 1){
      System.out.println("java Eligere <name_of_ballot_csv>");
      System.exit(0);
    }
    Election election = CreateElection.createElection(args[0]);
    if(election==null){
      System.exit(-1);
    }
    election.runVotingAlgorithm();
  }

}

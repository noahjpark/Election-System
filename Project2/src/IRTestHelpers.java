import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

 /**
 * Helper functions used by IR Test
 *
 * @author Mohammad Essawy, Michael Markiewicz
 */
public class IRTestHelpers {

    /**
     * A helper function for the unit tests that will delete any instance of a Media Report or an OPL Audit File
     * from the current working directory
     */
    public static void deleteElectionOutputFiles() {
        File currentDirectory = new File(System.getProperty("user.dir")); // current working directory
        File[] listOfFiles = currentDirectory.listFiles();
        String currentFileName;
        String[] currentSplit;
        for (File file: listOfFiles) {
            currentFileName = file.getName();
            currentSplit = currentFileName.split("_");
            if ("IRMediaReport".equals(currentSplit[0]) || "IRAuditFile".equals(currentSplit[0])
                    || "Invalidated".equals(currentSplit[0])) {
                // delete the files
                file.delete();
            }
        }
    }

     /**
      * Helper function for many different unit tests. Creates a basic candidate list with candidates populated.
      * Candidate names will be c0, c1, ..., c&lt;numCandidates - 1&gt;
      * Candidate votes will correspond to index in votes array
      * all candidates are in party p0
      * @param votes array where index i in array = votes for candidate i
      * @return An ArrayList of Candidate instances
      */
     public static ArrayList<Candidate> createBasicCandidateInstance(int votes[]) {
         // create <numCandidates> candidates for each party
         int uniqueCandidateID = 0;
         ArrayList<Candidate> candidates = new ArrayList<>();
         // create a candidate for each element in the array with the
         for (int i = 0; i < votes.length; i++) {
             candidates.add(new Candidate("c".concat(String.valueOf(i)), "p".concat(String.valueOf(i)), uniqueCandidateID++));
             candidates.get(i).setCurNumVotes(votes[i]);
         }
         return candidates;
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
            if ("IRMediaReport".equals(currentSplit[0])) {
                mediaName = currentFileName;
            } else if ("IRAuditFile".equals(currentSplit[0])) {
                auditName = currentFileName;
            }
        }

        return new String[] {mediaName, auditName};
    }

     /**
      * Checks if the given file is empty or not.
      *
      * @param filename Filename of the file you want to check for emptiness.
      * @return true if the file is empty, false if otherwise.
      */
     public static boolean checkFileIsEmpty(String filename) {
         File file = new File(filename);
         if(file.length() == 0) {
             return true;
         }
         return false;
     }
}

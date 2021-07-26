# Eligere

In order to run the program you first must compile it using the command `javac *.java`

Once compilation is complete use the command `java Eligere <INSERT_CSV_FILE_HERE>`

Where the csv file includes the election data to be processed.

The results of the election will be outputted to the screen. 

An audit and media file will be created in the same directory.


## Special Notes
When running the Unit/System tests, the working directory is assumed to be the `Project1` directory. This is because the paths to example files in the tests are `testing/<testing subdirectory>/<example test file>`.

## Test Files
Below is a list of the `.java` files that are only used for unit/system testing:
- `CandidateTest.java`
- `CreateElectionTest.java`
- `IRManualTest.java`
- `IRSystemTest.java`
- `IRTest.java`
- `IRTestHelpers.java`
- `OPLManualTest.java`
- `OPLSystemTest.java`
- `OPLTest.java`
- `OPLTestHelpers.java`
- `PartyTest.java`

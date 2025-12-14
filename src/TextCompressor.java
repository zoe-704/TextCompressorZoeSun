/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Zoe Sun
 */
public class TextCompressor {

    /*
    8 bits
    alice.txt 669872/1104032 = 60.68%
    abra.txt = 104/136 = 76.47%
    shakespeare.txt = 26800008/41370776 = 64.78%
    tricky.txt = 40/56 = 71.43%

    12 bits
    alice.txt 477688/1104032 = 43.27%
    abra.txt = 160/136 = 117.65%
    shakespeare.txt = 19301280/41370776 = 46.65%
    tricky.txt = 64/56 = 114.29%


    14 bits
    alice.txt 448760/1104032 = 40.65%
    abra.txt = 184/136 = 135.29%
    shakespeare.txt = 17944280/41370776 = 43.37%
    tricky.txt = 72/56 = 128.57%


    16 bits
    alice.txt 488144/1104032 = 44.21%
    abra.txt = 208/136 = 152.94%
    shakespeare.txt = 16128576/41370776 = 38.99%
    tricky.txt = 80/56 = 142.86%
    total = 16617008/42475000 = 39.12%

    18 bits
    alice.txt 549168/1104032 = 49.74%
    abra.txt = 240/136 = 176.47%
    shakespeare.txt = 15117736/41370776 = 36.54%
    tricky.txt = 96/56 = 171.43%
    total = 15667240/42475000 = 36.89%

    19 bits
    shakespeare.txt = 14992672/41370776 = 36.24%

    20 bits
    shakespeare.txt = 15464360/41370776 = 37.38%
     */


    // Constants
    // Adjust number of bits per code
    private static final int BITS = 18;
    private static final int MAX_CODES = 1 << BITS;
    // EOF code 0x80 = 128
    private static final int EOF = 128;

    private static void compress() {
        // Read in string and create new tst
        String str = BinaryStdIn.readString();
        TST t = new TST();

        // Declare local variables for later use
        int n = str.length();
        int i = 0; // Index for iterating over str
        int code = EOF + 1; // First of ASCII codes to assign

        // Insert first 128 ASCII codes into tst
        for (int c = 0; c < 128; c++) {
            t.insert(String.valueOf((char) c), c);
        }

        // Iterate and compress str
        while (i < n) {
            // Get the longest prefix and its code
            String prefix = t.getLongestPrefix(str, i);
            int curCode = t.lookup(prefix);

            // Add new code to TST if there is enough space
            if (i + prefix.length() < n && code < MAX_CODES) {
                char next = str.charAt(i + prefix.length());
                t.insert(prefix + next, code);
                code++;
            }
            // Increment index to next letter and write out code
            i += prefix.length();
            BinaryStdOut.write(curCode, BITS);
        }
        BinaryStdOut.write(EOF, BITS);
        BinaryStdOut.close();
    }

    private static void expand() {
        // Declare map and fill it in with common ASCII codes
        String[] map = new String[MAX_CODES];
        for (int i = 0; i < 128; i++) {
            map[i] = Character.toString(i);
        }
        // First ASCII code to begin assigning
        int code = EOF + 1;
        // Get current code and make sure not at end of file
        int curCode = BinaryStdIn.readInt(BITS);
        if (curCode == EOF) {
            BinaryStdOut.close();
            return;
        }
        // Get first string and write it out
        String cur_string = map[curCode];
        BinaryStdOut.write(cur_string);

        // Expand entire compressed sequence
        while (!BinaryStdIn.isEmpty()) {
            // Get next code and make sure it's not end of file
            int next_code = BinaryStdIn.readInt(BITS);
            if (next_code == EOF) break;

            String next_string;
            // Lookahead for next code and string
            if (map[next_code] != null) { // Case 1: next string in map
                next_string = map[next_code];
            } else { // Case 2: next string not in map
                next_string = cur_string + cur_string.charAt(0);
            }
            // Add next string to map
            if (code < MAX_CODES) {
                map[code++] = cur_string + next_string.charAt(0);
            }
            // Move on to next string
            cur_string = next_string;
            BinaryStdOut.write(cur_string);
        }
        BinaryStdOut.close();
    }


    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}

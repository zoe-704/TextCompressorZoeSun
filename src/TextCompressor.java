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

    private static void compress() {
        // Read in string and create new tst
        String str = BinaryStdIn.readString();
        TST t = new TST();

        // Declare local variables for later use
        int n = str.length();
        int i = 0; // Index for iterating over str
        int code = 129; // First of ascii codes to assign

        // Insert first 128 ascii codes into tst
        for (int c = 0; c < 128; c++) {
            t.insert(String.valueOf((char) c), c);
        }

        // Iterate and compress str
        while (i < n) {
            // Get the longest prefix and its code
            String prefix = t.getLongestPrefix(str, i);
            int cur_code = t.lookup(prefix);

            // Add new code to TST if there is enough space
            if (i + prefix.length() < n) {
                char next = str.charAt(i + prefix.length());
                t.insert(prefix + next, code++);
            }
            // Increment index to next letter and write out code
            i += prefix.length();
            BinaryStdOut.write(cur_code, 8);
        }
        BinaryStdOut.write(128, 8); // EOF code 0x80 = 128
        BinaryStdOut.close();
    }

    private static void expand() {
        String[] map = new String[256];
        for (int i = 0; i < 128; i++) {
            map[i] = Character.toString(i);
        }
        int code = 129;

        int cur_code = BinaryStdIn.readInt(8);
        if (cur_code == 128) {
            BinaryStdOut.close();
            return;
        }
        // expand
        while (!BinaryStdIn.isEmpty()) {
            int next_code = BinaryStdIn.readInt(8);
            if (next_code == 128) {
                BinaryStdOut.write(map[cur_code]);
                return;
            }
            // current string
            // lookahead
                // check ahead case
            // write out
            // next code
        }
        BinaryStdOut.close();
    }


    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}

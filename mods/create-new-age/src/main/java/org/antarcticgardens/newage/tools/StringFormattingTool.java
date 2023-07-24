package org.antarcticgardens.newage.tools;

public class StringFormattingTool {

    public static final String[] postfixes = new String[] {
            "",
            "K",
            "M",
            "B",
            "T",
            "Q"
    };


    public static String formatLong(long l) {
        int d = 0;
        while (l >= 1000 && d < postfixes.length) {
            l/=1000;
            d++;
        }
        return l + postfixes[d];
    }


    public static String formatFloat(float f) {
        int d = 0;
        while (f >= 1000 && d < postfixes.length) {
            f/=1000;
            d++;
        }
        return String.format("%.1f%s", f, postfixes[d]);
    }

}

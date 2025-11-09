package gg.amecute.auralithutilities.Utils;

public class ShapeFixer
{
    public static String[][] transposeLayers(String[][] orig) {
        int L = orig.length;                     // number of original layers
        int R = orig[0].length;                  // rows per layer (assume rectangular)
        String[][] out = new String[R][L];       // new: rows become "layers", layers become "rows"

        for (int i = 0; i < L; i++) {
            for (int j = 0; j < R; j++) {
                out[j][i] = orig[i][j];
            }
        }
        return out;
    }

    public static String[][] reverseOuter(String[][] arr) {
        int n = arr.length;
        String[][] out = new String[n][];
        for (int i = 0; i < n; i++) out[i] = arr[n - 1 - i];
        return out;
    }

    public static String[] reverseEachRow(String[] layer) {
        String[] out = new String[layer.length];
        for (int r = 0; r < layer.length; r++) {
            out[r] = new StringBuilder(layer[r]).reverse().toString();
        }
        return out;
    }

    public static String[][] reverseEachRowInAll(String[][] arr) {
        String[][] out = new String[arr.length][];
        for (int i = 0; i < arr.length; i++) out[i] = reverseEachRow(arr[i]);
        return out;
    }

}

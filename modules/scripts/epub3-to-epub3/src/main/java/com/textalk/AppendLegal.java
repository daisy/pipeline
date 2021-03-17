package com.textalk;

import java.io.File;

public class AppendLegal {

    public void appendLegalDoc(File input, File output) {
        appendLegalDoc(input, output, "Ylva");
    }

    public void appendLegalDoc(File input, File output, String voice) {

    }

    public static void main(String[] args) {
        AppendLegal al = new AppendLegal();
        al.appendLegalDoc(new File("X60352A.epub"), new File("X60352A_out.epub"));
    }
}

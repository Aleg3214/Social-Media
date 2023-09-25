package org.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Comment {
    private final String Creator;
    private final String Content;
    private int CommentNumLike;
    private final long CommentPostId;
    private final long CommentId;
    private ArrayList<String> LikeFlag;

    public Comment(String Creator, String Content) throws IOException {
        this.Creator = Creator;
        this.Content = Content;
        this.CommentNumLike = 0;
        this.CommentPostId = 0;
        this.CommentId = DatabaseManager.commentIdMaker();
        this.LikeFlag = new ArrayList<>();
    }



    //Ritorna il numero di mi piace del commento
    public int getCommentNumLike() {
        return this.CommentNumLike;
    }

    //Ritorna gli username di chi ha messo mi piace al commento
    public ArrayList<String> getLikes() {
        return this.LikeFlag;
    }

    //Ritorna il contenuto del commento
    public String getContent() {
        return this.Content;
    }

    //Ritorna il creatore del commento
    public String getCreator() {
        return this.Creator;
    }

    //Ritorna l'ID del commento
    public long getCommentId() {
        return this.CommentId;
    }

    //Ritorna l'ID del post al quale il commento è associato
    public long getCommentPostId() {
        return this.CommentPostId;
    }

    //la funzionalità di questo metodo è prettamente grafica, creo un box nel quale inserisco i commenti e
    //le informazioni associate. (overload)
    public static void visualize(String Content, int NumLike, PrintWriter Out) {
        int BoxWidth = 33;
        //Divido il contenuto del commento (guarda funzione)
        List<String> Splits = DatabaseManager.splitString(Content, BoxWidth);
        int SplitNum = Splits.size();

        //Dipendentemente da quante righe di contenuto ho, gestisco la stampa delle parole all'interno del box dei commenti
        for (int i = 0; i < Splits.size(); i++) {
            int Space;

            if (i == 0) {
                Space = 34 - (Splits.get(i).length());
                Out.print("                             | " + Splits.get(i));

                for (int k = 0; k < Space; k++) {
                    Out.print(" ");
                }

            } else {
                Space = 34 - (Splits.get(i).length());
                Out.print("                             | " + Splits.get(i));

                for (int k = 0; k < Space; k++) {
                    Out.print(" ");
                }
            }

            if (SplitNum > 1)   Out.print("   |\n");

            else {
                Out.print("-" + NumLike + "-");
                Out.print("|\n");
            }
            SplitNum--;
        }
    }
}

package org.project;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;


public class Post {
    private final String Creator;
    private final long Id;
    private final String Content;
    private int LikeNum;
    private int CommentNum;
    private final String Time;
    private ArrayList<Comment> CommentSection;
    private ArrayList<String> LikeFlag;

    public Post(String Creatore, String Content) {
        this.Creator = Creatore;
        this.Id = DatabaseManager.postIdMaker();
        this.Content = Content;
        this.LikeNum = 0;
        this.CommentNum = 0;
        this.Time = DatabaseManager.calculateTime();
        this.CommentSection = new ArrayList<>();
        this.LikeFlag = new ArrayList<>();
    }

    public Post(String Content, int LikeNum, int CommentNum, String Time, long Id, String Creator, ArrayList<Comment> CommentSection, ArrayList<String> Likes) {
        this.Creator = Creator;
        this.Id = Id;
        this.Content = Content;
        this.LikeNum = LikeNum;
        this.CommentNum = CommentNum;
        this.Time = Time;
        this.CommentSection = CommentSection;
        this.LikeFlag = Likes;
    }


    //Ritorna l'orario di pubblicazione del post
    public String getTime(){
        return this.Time;
    }

    //Ritorna l'autore di pubblicazione del post
    public String getCreator(){
        return this.Creator;
    }

    //Ritorna l'ID del post
    public long getId() {
        return this.Id;
    }

    //Ritorna il numero di like del post
    public int getLikeNum(){
        return this.LikeNum;
    }

    //Ritorna il numero di commenti del post
    public int getCommentNum(){
        return this.CommentNum;
    }

    //Ritorna gli username di chi ha messo mi piace al post
    public ArrayList<String> getLikeFlag(){
        return this.LikeFlag;
    }

    //Ritorna tutti i commenti del post
    public ArrayList<Comment> getCommentSection(){
        return this.CommentSection;
    }

    //Ritorna il cintenuto del post
    public String getContent(){
        return this.Content;
    }

}
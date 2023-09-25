package org.project;

import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

public class DatabaseManager {
    private MongoClient Client;
    private static MongoDatabase Database;
    private static MongoCollection<Document> UserCollection;
    private static MongoCollection<Document> CommentCollection;
    private static MongoCollection<Document> PostCollection;

    //Il database presenta 3 collezioni: Utenti, commenti e post
    public DatabaseManager(String Uri, String DName) {
        Client = MongoClients.create(Uri);
        Database = Client.getDatabase(DName);
        UserCollection = Database.getCollection("Utenti");
        CommentCollection = Database.getCollection("Commenti");
        PostCollection = Database.getCollection("Post");
    }



    //crea un ID univoco e incrementale
    public static long commentIdMaker() {   //Crea un ID univoco per i commenti
        return commentMaxId() + 1;
    }

    //ritorna l'ID più alto tra tutti i commenti creati (ID dell'ultimo commento)
    public static long commentMaxId() {
        Document MaxDocument = DatabaseManager.CommentCollection.aggregate(Arrays.asList(Aggregates.sort(Sorts.descending("IdCommento")), Aggregates.limit(1))).first();
        //Se non trova alcun commento, significa che non c'è ne sono. Ritorna dunque 2 in quanto fino al 2, tutti i numeri sono occupati per azioni
        if (MaxDocument == null) return 2;
        return (long)MaxDocument.get("IdCommento");
    }

    //Ritorna l'ID del post associato al commento
    public long getPostCommentId(Document Document, PrintWriter Out) {
        long Id = 999999;

        try {
            Id = (long) Document.get("IdPost");
        }

        catch (NullPointerException exception) {
            Out.println("Id non trovato");
        }

        finally {
            return Id;
        }
    }

    //Ritorna il contenuto del post
    public static String getPostContent(Document Document) {
        return (String) Document.get("Contenuto");
    }

    //Ritorna il contenuto del commento
    public static String getCommentContent(Document Document) {
        return (String) Document.get("Commento");
    }

    //Ritorna il creatore del commento
    public static String getCreator(Document Document) {
        return (String) Document.get("Creatore");
    }

    //Ritorna il numero di mi piace
    public static int getLikeNum(Document Document) {
        return (int) Document.get("NumeroLike");
    }

    //Ritorna il numero di commenti
    public static int getCommentNum(Document Document) {
        return (int) Document.get("NumeroCommenti");
    }

    //Ritorna l'orario di pubblicazione del post
    public static String getTime(Document Document) {
        return (String) Document.get("Orario");
    }

    //Ritorna il creatore del commento
    public static long getPostId(Document Document) {
        return (long) Document.get("Id");
    }

    //Ritorna la sezione commenti del post
    public static ArrayList<Comment> getCommentSection(Document Document) {
        return (ArrayList<Comment>) Document.get("Commenti");
    }

    //Ritorna i nomi di coloro che hanno messo mi piace
    public static ArrayList<String> getLikeFlag(Document Document, PrintWriter Out) {
        ArrayList<String> Likes = null;

        try {
            Likes = (ArrayList<String>) Document.get("Like");
        }
        catch (NullPointerException exception) {
            Out.println("Nessuno ha ancora messo mi piace");
        }

        finally {
            return Likes;
        }
    }

    //Viene ricercato il post con l'ID univoco come chiave e viene creato un post da ritornare con le sue variabili
    public Post findPost(long PostId, PrintWriter Out) {
        Document Doc = PostCollection.find(eq("Id", PostId)).first();
        return new Post(getPostContent(Doc), getLikeNum(Doc), getCommentNum(Doc), getTime(Doc),
                getPostId(Doc), getCreator(Doc), getCommentSection(Doc), getLikeFlag(Doc, Out));
    }

    //Cerca tra i nomi degli utenti registrati e ritorna true se viene trovato
    public boolean findName(String Name) {
        Document Doc = UserCollection.find(eq("Username", Name)).first();
        return Doc != null;
    }

    //Cerca la corrispondenza tra password inserita ed username inserito nello stesso user
    public boolean findUser(String Name, String Password) {
        boolean Flag = false;
        Document Doc = UserCollection.find(eq("Username", Name)).first();

        if (Doc != null) {
            String StoredPassword = Doc.getString("Password");

            if (Password.equals(StoredPassword)) {
                Flag = true;
            }
        }
        return Flag;
    }

    //Inserisce un user nella collezione utenti del database
    public void insertUser(User User) {
        UserCollection.insertOne(new Document("Username", User.getUsername()).append("Password", User.getPassword()).append("Super", false));
    }

    public void insertSuperUser(SuperUser User) {
        UserCollection.insertOne(new Document("Username", User.getUsername()).append("Password", User.getPassword()).append("Super", User.getSuper()));
    }

    public boolean isSuper(User User) {
        Document Doc = UserCollection.find(and(eq("Username", User.getUsername()),eq("Password", User.getPassword()), eq("Super",true))).first();
        return Doc != null;
    }

    //Verifica l'esistenza di un determinato commento sotto un determinato post
    public static boolean commentIdExist(long IdPost, long IdComment) {
        Document Doc = CommentCollection.find(and(eq("IdPost", IdPost), eq("IdCommento", IdComment))).first();
        return Doc != null;
    }

    //Inserisce un post nella collezione dei post del database
    public void insertPost(Post Post) {
        PostCollection.insertOne(new Document("Contenuto", Post.getContent()).append("NumeroLike",
                Post.getLikeNum()).append("NumeroCommenti", Post.getCommentNum()).append("Orario",
                Post.getTime()).append("Commenti", Post.getCommentSection()).append("Like",
                Post.getLikeFlag()).append("Id", Post.getId()).append("Creatore", Post.getCreator()));
    }

    //Inserisce un nuovo commento nella collezione dei commenti. In aggiunta il commento appartiene ad un determinato post,
    //quindi è importante associare al commento, l'ID del post nella quale è inserito.
    //Infine, nella collezione dei post, aggiungo il commento nella sezione commenti relativa ed incremento il counter del numero commenti
    public void insertComment(long PostId, String creator, String Content) throws IOException {
        Comment comment = new Comment(creator, Content);
        CommentCollection.insertOne(new Document("IdPost", PostId).append("IdCommento", comment.getCommentId())
                .append("Commento", ("|" + comment.getCommentId() + "| " + comment.getCreator() + ": " + comment.getContent()))
                .append("NumeroLike", comment.getCommentNumLike()).append("Creatore", comment.getCreator()).append("Like", comment.getLikes()));
        FindIterable<Document> documents = CommentCollection.find(eq("IdPost", PostId));

        for (Document document : documents) {
            PostCollection.updateOne(eq("Id", PostId), addToSet("Commenti", (document.get("Commento"))));
        }
        PostCollection.updateOne(eq("Id", PostId), inc("NumeroCommenti", 1));
    }

    //Ricerca e stampa tutti i post presenti nella collezione
    public static void printAllPosts(PrintWriter Out) {
        FindIterable<Document> Documents = PostCollection.find();

        for (Document Document : Documents) {
            Post Post = new Post(getPostContent(Document), getLikeNum(Document), getCommentNum(Document), getTime(Document),
                    getPostId(Document), getCreator(Document), getCommentSection(Document), getLikeFlag(Document, Out));
            ClientManager.visualize(Post, Out);
        }
    }

    //Ricerca tutti i commenti presenti nella collezione
    public void printAllComments(long PostId, PrintWriter Out) {
        //Il flag viene triggerato dipendentemente dalla presenza o assenza di commenti sotto al post cercato
        boolean Flag = false;
        FindIterable<Document> Documents = CommentCollection.find();

        for (Document Document : Documents) {

            if (getPostCommentId(Document, Out) == PostId) {
                Flag = true;
                Comment.visualize(getCommentContent(Document), getLikeNum(Document), Out);
                Out.println("                             |                                      |");
            }
        }

        if (Flag) Out.println("                             |______________________________________|");

        else {
            Out.println("                             |      NON CI SONO ANCORA COMMENTI     |");
            Out.println("                             |______________________________________|");
        }
    }

    //Verifica se è già stato messo mi piace al post, eventualmente lo mette
    public void putPostLike(long ID, String Username, PrintWriter Out) {
        Document Doc = PostCollection.find(eq("Id", ID)).first();
        ArrayList<String> Likes = getLikeFlag(Doc, Out);

        if (Likes != null && Likes.contains(Username)) {
            Out.println("Non puoi mettere più di 1 mi piace");
        }

        else {
            PostCollection.updateOne(eq("Id", ID), inc("NumeroLike", 1));
            PostCollection.updateOne(eq("Id", ID), addToSet("Like", Username));
            Out.println("Hai messo mi piace!");
        }
    }

    //Verifica se è già stato messo mi piace al commento, eventualmente lo mette
    public void putCommentLike(long postId, long commentId, String Username, PrintWriter Out) {
        Document Doc = CommentCollection.find(eq("IdCommento", commentId)).first();

        //Verifica se il commento appartiene al post indicato
        if (!commentIdExist(postId, commentId)) {
            Out.println("Il commento selezionato non appartiene a questo post");
        }
        else {
            ArrayList<String> Likes = getLikeFlag(Doc, Out);

            if (Likes != null && Likes.contains(Username)) {
                Out.println("Non puoi mettere più di 1 mi piace");
            }
            else {
                CommentCollection.updateOne(eq("IdCommento", commentId), inc("NumeroLike", 1));
                CommentCollection.updateOne(eq("IdCommento", commentId), addToSet("Like", Username));
                Out.println("Hai messo mi piace!");
            }
        }
    }

    //ritorna l'ID più alto tra tutti i post creati (ID dell'ultimo post)
    public static long postMaxId() {
        Document MaxDocument = DatabaseManager.PostCollection.aggregate(Arrays.asList(Aggregates.sort(Sorts.descending("Id")), Aggregates.limit(1))).first();
        //Se non trova alcun post, significa che non c'è ne sono. Ritorna dunque 0
        if (MaxDocument == null) return 0;
        return (long)MaxDocument.get("Id");
    }

    //Creo un metodo per dividere la stringa in ingresso (che sarà il contenuto del post) in modo che possa
    //fittare perfettamente il box grafico.
    public static List<String> splitString(String Content, int BoxWidth) {
        List<String> Splits = new ArrayList<>();
        //Separo parola per parola il contenuto del post
        String[] Words = Content.split("\\s+");
        StringBuilder CurrentSplit = new StringBuilder();

        //Per ogni parola verifico che essa entri all'interno del box, se così non fosse vado a capo
        for (String Word : Words) {

            if (CurrentSplit.length() + Word.length() + 1 <= BoxWidth) {

                if (CurrentSplit.length() > 0) {
                    CurrentSplit.append(" ");
                }
                CurrentSplit.append(Word);

            } else {
                Splits.add(CurrentSplit.toString());
                CurrentSplit = new StringBuilder();
            }
        }

        if (CurrentSplit.length() > 0) {
            Splits.add(CurrentSplit.toString());
        }

        return Splits;
    }

    //crea un ID univoco e incrementale
    public static long postIdMaker() {
        return DatabaseManager.postMaxId() + 1;
    }

    //Tramite localdatetime ottengo la data e l'ora del mio sistema, tuttavia le informazioni ottenute vengono
    //stampate in modo confusionario, tramite il formatter riesco a scriverle nel formato che desidero
    public static String calculateTime() {
        LocalDateTime Now = LocalDateTime.now();
        DateTimeFormatter Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return Now.format(Formatter);
    }

    //Funzione che rimuove il post selezionato
    public static void removePost(Post Post) {
        PostCollection.deleteOne(new Document("Contenuto", Post.getContent()).append("NumeroLike",
                Post.getLikeNum()).append("NumeroCommenti", Post.getCommentNum()).append("Orario",
                Post.getTime()).append("Commenti", Post.getCommentSection()).append("Like",
                Post.getLikeFlag()).append("Id", Post.getId()).append("Creatore", Post.getCreator()));
    }
}



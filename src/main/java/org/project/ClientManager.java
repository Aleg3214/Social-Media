package org.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.List;

public class ClientManager implements Runnable {
    //carattere che indica al client di voler chiudere la connessione
    private static final String CLOSE = "-";
    //caratattere che indica al client di volere una risposta
    private static final String ASK = "+";
    Socket Socket;
    BufferedReader In;
    PrintWriter Out;
    DatabaseManager DM;

    public ClientManager(Socket Socket, DatabaseManager DM) {
        this.Socket = Socket;
        this.DM = DM;

        //Collego in e out con il server
        try {
            this.In = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
            this.Out = new PrintWriter(Socket.getOutputStream(),true);
        }
        catch(IOException e) {
            Out.println("Errore di comunicazione con il server");
            System.exit(-1);
        }
    }

    public void MainMenu(DatabaseManager DM) throws Exception {
        //Apertura del menù principale
        String Choice;
        while (true) {
            Out.println("      Digita 'RR' per registrare un Super User     ");
            Out.println("Digita 'R' per registrarti. Digita 'L' per loggarti");
            Out.println("        Digita 'C' per chiudere il programma       ");
            //Il server indica di volere una risposta
            Out.println(ASK);
            Choice = In.readLine();

            //Se viene selezionata la registrazione di un User
            if (Choice.equals("R")) {
                RegisterUser();
            }

            //Se viene selezionata la registrazione di un SuperUser
            else if (Choice.equals("RR")) {
                Out.println("Inserisci la password di sicurezza");
                Out.println(ASK);
                if (In.readLine().equals("30elode"))   RegisterSuperUser();
                else Out.println("Password errata");
            }


            else if (Choice.equals("C")) {
                Out.println("Arrivederci");
                Out.println(CLOSE); //il server indica di voler chiudere la comunicazione
                In.close();
                Out.close();
                Socket.close();
                break;
            }
            //Se viene selezionato il login
            else if (Choice.equals("L")) {
                //Nella stringa "input" inserisce le credenziali dell'user appena loggato
                String Input = Login().getCredentials();
                //Divide le credenziali e le inserisce in 2 variabili
                String[] Parts = Input.split(" ");
                User ActuallyLogged = new User(Parts[0], Parts[1]);

                //Verifica che il login abbia fallito
                if (ActuallyLogged.getUsername().equals("o")) ;

                    //Se il login avviene con successo
                else {
                    HomeMenu(ActuallyLogged, DM);
                }
            }
        }
    }

    public void HomeMenu(User ActuallyLogged, DatabaseManager DM) throws IOException {
        long Choice1;

        //Si apre il menù principale dopo il login
        while (true) {
            //L'user loggato vedrà la homepage (tutti i post pubblicati)
            DM.printAllPosts(Out);
            Out.println("                  Aggiungi un post digitando '0'                  ");
            Out.println("Interagisci con un post digitando il suo ID (numero in basso a dx)");
            Out.println("             Digita '999999' per effettuare il logout             ");
            Out.println(ASK);

            //Gestisce eventuali mismatch nella ricezione del numero della scelta
            try {
                Choice1 = Long.parseLong(In.readLine());
            } catch (Exception exception) {
                Out.println("Digita un numero valido");
                continue;
            }

            //Se viene selezionato il codice del logout
            if (Choice1 == 999999) break;

                //Se viene selezionata l'aggiunta di un post
            else if (Choice1 == 0) {
                String Content = write(1);
                Post Post = new Post(ActuallyLogged.getUsername(), Content);
                DM.insertPost(Post);
            }

            //Se viene digitato un numero non associato ad alcun post
            else if (Choice1 > DM.postMaxId()) {
                Out.println("Non esistono post con questo ID");
            }

            //Se viene digitato l'ID di uno dei post presenti
            else {
                //Stampa nuovamente il post preso in considerazione
                visualize(DM.findPost(Choice1, Out), Out);
                DM.printAllComments(Choice1, Out);
                PostMenu(DM, Choice1, ActuallyLogged,DM.findPost(Choice1, Out));
            }
        }
    }

    public void PostMenu(DatabaseManager DM, long Choice1, User ActuallyLogged, Post Post) throws IOException {
        while (true) {
            //Menù del singolo post
            if (DM.isSuper(ActuallyLogged))   Out.println("             Premi '123456' per eliminare il seguente post            ");
            Out.println("  Premi '0' per mettere mi piace - Premi '1' per lasciare un commento ");
            Out.println("                Premi '2' per selezionare un altro post               ");
            Out.println("        Digita l'ID (|ID|) di un commento per mettere mi piace        ");
            Out.println(ASK);
            int Action;

            //Gestisce eventuali mismatch nella ricezione del numero del post
            try {
                Action = Integer.parseInt(In.readLine());
            }

            catch (InputMismatchException | IOException exception) {
                Out.println("Digita un numero valido");
                continue;
            }

            //Se si vuole mettere mi piace al post
            if (Action == 0) {
                DM.putPostLike(Choice1, ActuallyLogged.getUsername(), Out);
                break;
            }

            //Se si vuole commentare il post
            else if (Action == 1) {
                String Content = write();
                DM.insertComment(Choice1, ActuallyLogged.getUsername(), Content);   //Inserisce il commento nella collezione dei commenti
                break;
            }

            //Se si vuole tornare alla homepage
            else if (Action == 2) break;

            else if (DM.isSuper(ActuallyLogged) && Action == 123456) {
                DM.removePost(Post);
                Out.println("Post rimosso!");
                break;
            }
            //Se si vuole mettere mi piace ad un commento del post
            else {
                DM.putCommentLike(Choice1, Action, ActuallyLogged.getUsername(), Out);
                break;
            }
        }
    }

    //Funzione di login che prevede tutti i controlli su username e password,
    //ritorna un user con nome = 'o' e password 'o' se il login fallisce
    public User Login() throws Exception {
        int Tentativi = 3;
        String Username = null;
        String Password = null;

        //Vengono dati 3 tentativi per il corretto inserimento dell'Username
        while (Tentativi > 0) {
            Out.println("Inserisci il tuo nome:");
            Out.println(ASK);
            Username = In.readLine();

            //Se il nome utente viene trovato nella collezione degli utenti
            if (DM.findName(Username)) {
                Out.println("Inserisci la tua password:");
                Tentativi = 3;

                //Vengono dati 3 tentativi per il corretto inserimento della password
                while (Tentativi > 0) {
                    Out.println(ASK);
                    Password = In.readLine();

                    //Se il nome utente e la password fanno parte dello stesso utente
                    if (DM.findUser(Username, Password)) {
                        Out.println("\n" + "Bentornato " + Username + "!" + "\n");
                        break;
                    }

                    else {
                        Tentativi--;

                        if (Tentativi == 1) {
                            Out.println("Password errata, " + Tentativi + " tentativo rimasto");
                        }

                        //Se ho finito i tentativi, inserisco 'o' ed 'o' come credenziali di un utente
                        else if (Tentativi == 0) {
                            Out.println("Login non riuscito");
                            Username = "o";
                            Password = "o";
                            break;
                        }

                        else {
                            Out.println("Password errata, " + Tentativi + " tentativi rimasti");
                        }
                    }
                }
            }
            else {
                Tentativi--;

                if (Tentativi == 1) {
                    Out.println("Nome non esistente, " + Tentativi + " tentativo rimasto");
                    continue;
                }

                //Se ho finito i tentativi, inserisco 'o' ed 'o' come credenziali di un utente
                else if (Tentativi == 0) {
                    Out.println("Login non riuscito");
                    Username = "o";
                    Password = "o";
                    break;
                }

                else {
                    Out.println("Nome non esistente, " + Tentativi + " tentativi rimasti");
                    continue;
                }
            }
            break;
        }
        return new User(Username, Password);
    }

    //Funzione che prevede la registrazione dell'utente
    public void RegisterUser() throws Exception {
        String Name;
        Out.println("Per iniziare, crea il tuo username!");
        Out.println("Ricorda che esso verrà visualizzato da tutti.");

        while (true) {
            Out.println(ASK);
            Name = In.readLine();

            //Verifica sulla lunghezza dal nome
            if (Name.length() < 4 || Name.length() > 15) {
                Out.println("L'username deve contenere tra 4 e 15 lettere.");
                continue;
            }

            //Verifica se il nome c'è già nel database,
            // se ritorna true, significa che è già presente
            if (DM.findName(Name)) {
                Out.println("Ops! Username già in uso. Inseriscine uno diverso.");
                continue;
            }

            Out.println("Crea ora la tua password. Ricorda di non rivelarla a nessuno!");
            while (true) {
                Out.println(ASK);
                String Password = In.readLine();

                //Verifica sulla lunghezza della password
                if (Password.length() < 4) {
                    Out.println("La password deve contenere almeno 4 lettere.");
                    continue;
                }

                //Crea un user con le le credenziali appena create
                User User = new User(Name, Password);
                //Inserisce il nuovo user nella collezione utenti del database
                DM.insertUser(User);
                Out.println("Registrazione andata a buon fine, benvenuto nella community!");
                break;
            }
            break;
        }
    }

    //Funzione che prevede la registrazione del super utente
    public void RegisterSuperUser() throws Exception {
        String Name;
        Out.println("Per iniziare, crea il tuo username!");
        Out.println("Ricorda che esso verrà visualizzato da tutti.");

        while (true) {
            Out.println(ASK);
            Name = In.readLine();

            //Verifica sulla lunghezza dal nome
            if (Name.length() < 4 || Name.length() > 15) {
                Out.println("L'username deve contenere tra 4 e 15 lettere.");
                continue;
            }

            //Verifica se il nome c'è già nel database,
            // se ritorna true, significa che è già presente
            if (DM.findName(Name)) {
                Out.println("Ops! Username già in uso. Inseriscine uno diverso.");
                continue;
            }

            Out.println("Crea ora la tua password. Ricorda di non rivelarla a nessuno!");
            while (true) {
                Out.println(ASK);
                String Password = In.readLine();

                //Verifica sulla lunghezza della password
                if (Password.length() < 4) {
                    Out.println("La password deve contenere almeno 4 lettere.");
                    continue;
                }

                //Crea un user con le le credenziali appena create
                SuperUser User1 = new SuperUser(Name, Password);
                //Inserisce il nuovo user nella collezione utenti del database
                DM.insertSuperUser(User1);
                Out.println("Registrazione andata a buon fine, benvenuto nella community!");
                break;
            }
            break;
        }
    }

    //La funzionalità di questo metodo è prettamente grafica, creo un box nel quale inserisco il post ed
    //i dati che ne sono associati. (overload)
    public static void visualize(Post Post, PrintWriter OutP) {
        //Creo la parte superiore del box
        OutP.print(" ___________________________________________________________________\n");
        int BoxWidth = 64;
        //Divido il contenuto del post (guarda funzione)
        List<String> Splits = DatabaseManager.splitString(Post.getContent(), BoxWidth);
        int Flag = 15 - Post.getCreator().length();
        //Stampo il nome del creatore del post
        OutP.print("|                                                |" + Post.getCreator() + "|");

        //Riempo di spazi fino alla fine del box creato
        while (Flag > 0) {
            OutP.print(" ");
            Flag--;
        }
        OutP.print("  |\n");
        OutP.print("|                                                                   |\n");

        //Dipendentemente da quante righe di contenuto ho, gestisco la stampa delle parole all'interno del box del post
        for (int i = 0; i < Splits.size(); i++) {
            int Space;

            if (i == 0) {
                Space = 65 - (Splits.get(i).length());
                OutP.print("| -" + Splits.get(i));

                for (int k = 0; k < Space; k++){
                    OutP.print(" ");
                }

            } else {
                Space = 66 - (Splits.get(i).length());
                OutP.print("| " + Splits.get(i));

                for (int k = 0; k < Space; k++){
                    OutP.print(" ");
                }

            }
            OutP.print("|\n");
        }

        //Stampo la parte bassa del post con le informazioni del caso
        OutP.print("|                                                                   |\n");
        OutP.print("|                                                                   |\n");
        OutP.print("|                                                                   |\n");
        OutP.println("| Likes: " + Post.getLikeNum() + ",   Comments: " + Post.getCommentNum() + ",   Check out the comments  |              |");
        OutP.println("| " + Post.getTime() + "                                V         |" + Post.getId() + "|  |");
        OutP.println("|___________________________________________________________________|");
    }


    //Funzione puramente grafica per la scrittura di un post (overload)
    public String write(int i) throws IOException {
        Out.println("A cosa stai pensando?");
        Out.println(ASK);
        String Post = In.readLine();
        return Post;
    }

    //Funzione grafica per la scrittura del contenuto del commento (overload)
    public String write() throws IOException {
        Out.println("Aggiungi commento:");
        Out.println(ASK);
        String Commento = In.readLine();
        return Commento;
    }

    public void run() {
        while (true) {
            try {
                MainMenu(DM);
            }
            //Qualsiasi eccezione arrivi dal MainMenu verra' gestita ed il menù rilanciato
            catch (Exception e) {
                Out.println("Risposta non valida, riprovare");
                continue;
            }
            //Se non ci sono eccezioni, esco
            break;
        }
    }
}

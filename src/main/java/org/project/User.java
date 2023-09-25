package org.project;

public class User {
    private final String Username;
    private final String Password;

    public User(String Username, String Password){
        this.Username = Username;
        this.Password = Password;
    }



    //Ritorna l'username dell'user
    public String getUsername() {
        return this.Username;
    }

    //Ritorna la password dell'user
    public String getPassword() {
        return this.Password;
    }

    //Ritorna username e password dell'utente in un'unica stringa distanziati da uno spazio
    public String getCredentials() {
        return this.Username + " " + this.Password;
    }
}

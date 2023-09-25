package org.project;

public class SuperUser extends User {
    private final boolean Super;

    public SuperUser(String Username, String Password) {
        super(Username,Password);
        this.Super = true;
    }

    public boolean getSuper() {
        return this.Super;
    }
}

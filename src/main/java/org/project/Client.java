package org.project;

import org.w3c.dom.Element;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    //Carattere per chiusura connessione
    private static final String CLOSE = "-";
    //Carattere per chiedere una risposta
    private static final String ASK = "+";

    public static void main (String[] args) throws Exception {
        Scanner Client = new Scanner(System.in);
        String ClientString, ServerString;
        ParserXML Parser = new ParserXML("ClientConfig.xml");
        Element Root = Parser.getDOMParsedDocumentRoot();
        String Ip = Root.getElementsByTagName("ip").item(0).getTextContent();
        int Port = Integer.parseInt(Root.getElementsByTagName("port").item(0).getTextContent());
        Socket Socket = new Socket(Ip, Port);
        PrintWriter Out = new PrintWriter(Socket.getOutputStream(), true);
        BufferedReader In = new BufferedReader(new InputStreamReader(Socket.getInputStream()));

        //il client comunica con il server leggendo sempre cio' che scrive fin quando la connessione non viene chiusa
        while (true) {
            ServerString = In.readLine();

            //Se il server vuole smettere di comunicare
            if(ServerString.equals(CLOSE))   break;

            //Se il server vuole una risposta
            else if(ServerString.equals(ASK)) {
                ClientString = Client.nextLine();
                Out.println(ClientString);
            }

            else
                System.out.println(ServerString);
        }
        //Chiusura collegamento
        In.close();
        Out.close();
        Out.close();
    }
}

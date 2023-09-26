package org.project;

import org.w3c.dom.Element;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main (String[] args) throws Exception{
        Thread T;
        Socket Socket;
        ClientManager CM;
        //Parser per leggere la configurazione di server e DB, prendo la root
        ParserXML Parser = new ParserXML("ServerConfig.xml");
        Element Root = Parser.getDOMParsedDocumentRoot();
        Element Server = (Element) Root.getElementsByTagName("server").item(0);
        int Port = Integer.parseInt(Server.getElementsByTagName("port").item(0).getTextContent());
        ServerSocket ServerSocket = new ServerSocket(Port);
        Element DB = (Element) Root.getElementsByTagName("db").item(0);
        String Uri = DB.getElementsByTagName("uri").item(0).getTextContent();
        String DName = DB.getElementsByTagName("dbname").item(0).getTextContent();
        DatabaseManager DM = new DatabaseManager(Uri, DName);


        //Il server deve sempre stare in ascolto
        while(true) {
            System.out.println("Server in ascolto");
            Socket = ServerSocket.accept();
            System.out.println("E' arrivato un cliente");
            //Gestisco il cliente con un thread
            CM = new ClientManager(Socket,DM);
            T = new Thread(CM);
            T.start();
        }
    }
}

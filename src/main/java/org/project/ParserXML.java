package org.project;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ParserXML {
    private DocumentBuilderFactory dbf;
    private File file;

    //Costruttore che richiede il nome del documento di cui vogliamo fare il parsing
    public ParserXML(String fileName) {
        this.dbf = DocumentBuilderFactory.newInstance();
        this.file = new File(fileName);
    }

    //Metodo che ritorna la root del documento XML
    public Element getDOMParsedDocumentRoot() {
        Element root = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);
            root = document.getDocumentElement();

        } catch (Exception Exception) {
            Exception.printStackTrace();
        }
        return root;
    }
}

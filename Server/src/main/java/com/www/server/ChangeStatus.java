package com.www.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ChangeStatus extends HttpServlet {

    String xmlPath;

    @Override
    public void init() {
        xmlPath = getServletContext().getInitParameter("mainPath");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String row = null;
        try {
            row = request.getParameter("row");
            HttpSession session = request.getSession(true);
            String username = (String) session.getAttribute("username");

            int id = Integer.parseInt(row);
            SAXBuilder builder = new SAXBuilder();
            File xmlFile = new File(xmlPath + "/" + username + "/" + id + "/" + id + ".xml");
            Document document = (Document) builder.build(xmlFile);
            Element rootNode = document.getRootElement();
            rootNode.getChild("new").setText("false");
            rootNode.getChild("counter").setText("0");
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, new FileWriter(xmlPath + "/" + username + "/" + id + "/" + id + ".xml"));
        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException e) {
            System.out.println(e.getMessage());
        }
        response.sendRedirect(response.encodeRedirectURL("main/view.jsp?row=" + row));
    }
}

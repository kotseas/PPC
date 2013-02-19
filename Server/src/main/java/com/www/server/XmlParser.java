package com.www.server;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XmlParser extends HttpServlet {

    String xmlPath;

    @Override
    public void init() {
        xmlPath = getServletContext().getInitParameter("mainPath");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        String username = (String) session.getAttribute("username");
        File userDir = new File(xmlPath + "/" + username);
        int k = 0;
        int F = 0;
        if (userDir.listFiles().length != 0) {
            F = userDir.listFiles().length;
        }
        String[] tasks = new String[6 * F];
        File[] files = userDir.listFiles();
        CustomComparator comparator = new CustomComparator();
        Arrays.sort(files,comparator);
        for (File fileName : files) {
            String folderName = fileName.getName();            
            SAXBuilder builder = new SAXBuilder();
            File xmlFile = new File(userDir + "/" + folderName + "/" + folderName + ".xml");
            try {
                Document document = (Document) builder.build(xmlFile);
                Element rootNode = document.getRootElement();
                tasks[k] = rootNode.getChildText("name");
                tasks[k + 1] = rootNode.getChildText("comment");
                tasks[k + 3] = rootNode.getChildText("new");
                tasks[k + 4] = rootNode.getChildText("counter");
                Element upload = rootNode.getChild("upload");
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                //get current date time with Date()
                Date date = new Date();
                if ((upload.getChildText("date")).equals(dateFormat.format(date))) {
                    tasks[k + 2] = upload.getChildText("time");
                } else {
                    tasks[k + 2] = upload.getChildText("date");
                }
                tasks[k + 5] = folderName;
                k = k + 6;
            } catch (IOException io) {
                System.out.println(io.getMessage());
            } catch (JDOMException jdomex) {
                System.out.println(jdomex.getMessage());
            }
        }
        session.setAttribute("data", tasks);
        response.sendRedirect(response.encodeRedirectURL("main/home.jsp"));
        //RequestDispatcher view = request.getRequestDispatcher("main/basic.jsp");
        //view.forward(request, response);
    }
}

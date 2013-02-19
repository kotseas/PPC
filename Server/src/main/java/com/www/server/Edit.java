package com.www.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Edit extends HttpServlet {

    private String CONSOLE;
    private String Path;
    private String LIB;
    private String JAVAC;
    private String DX;
    private boolean isMultipart;
    private String USERS_INFO;
    private String USERNAME;
    private String PASSWORD;

    @Override
    public void init() {
        Path = getServletContext().getInitParameter("mainPath");
        CONSOLE = getServletContext().getInitParameter("console");
        LIB = getServletContext().getInitParameter("library");
        JAVAC = getServletContext().getInitParameter("javac");
        DX = getServletContext().getInitParameter("dx");
        USERNAME = getServletContext().getInitParameter("username");
        PASSWORD = getServletContext().getInitParameter("password");
        USERS_INFO = getServletContext().getInitParameter("users_info");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
        try {
            isMultipart = ServletFileUpload.isMultipartContent(request);
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload Edit = new ServletFileUpload(factory);
            HttpSession session = request.getSession(true);
            String id = (String) session.getAttribute("id");
            String javaName = (String) session.getAttribute("javaName");
            String javaCode = "";
            //String javaCode=(String) session.getAttribute("javaCode");
            List fileItems = Edit.parseRequest(request);
            Iterator i = fileItems.iterator();
            while (i.hasNext()) {
                FileItem fi = (FileItem) i.next();
                String fieldName = fi.getFieldName();
                if (fieldName.equals("javatext")) {
                    javaCode = fi.getString();
                }
            }
            //String javaCode=request.getParameter("javatext");
            String username = (String) session.getAttribute("username");
            File javafile = new File(Path + "/" + username + "/" + id + "/" + javaName);
            FileWriter fw = new FileWriter(javafile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(javaCode);
            bw.close();
            System.out.println(username + "|" + javaName + "|" + id);
            compileFile(username, javaName, id);
            response.sendRedirect(response.encodeRedirectURL("main/home.jsp"));
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Edit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Edit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Edit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(Edit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileUploadException ex) {
            Logger.getLogger(Edit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void compileFile(String user, String file, String id) throws IOException, InterruptedException, JDOMException, ClassNotFoundException, SQLException {
        String result = "";
        String name = FilenameUtils.removeExtension(file);
        String url = Path + "/" + user + "/" + id;
        String javac = JAVAC + " -cp " + LIB + " " + url + "/" + name + ".java";
        String dx = DX + " --dex --no-strict --output=" + url + "/classes.dex " + url + "/" + name + ".class";
        Process p = Runtime.getRuntime().exec(CONSOLE);
        OutputStream os = p.getOutputStream();
        os.write((javac + "\n").getBytes());
        os.write((dx + "\n").getBytes());
        os.flush();
        os.close();
        BufferedReader bf = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line = bf.readLine();
        while (line != null) {
            result = result + line + "\n";
            line = bf.readLine();
        }
        bf.close();
        xmlEditor(user, file, id, result);
    }

    private void zipFile(String user, String id) throws IOException {
        String url = Path + "/" + user + "/" + id;
        Process p = Runtime.getRuntime().exec(CONSOLE);
        OutputStream os = p.getOutputStream();
        os.write(("cd " + url + "\n").getBytes());
        os.write(("zip " + id + ".zip " + "classes.dex" + "\n").getBytes());
        os.flush();
        os.close();
    }

    private void xmlEditor(String user, String file, String id, String string) throws JDOMException, ClassNotFoundException, SQLException {
        SAXBuilder builder = new SAXBuilder();
        String url = Path + "/" + user + "/" + id + "/" + id + ".xml";
        File xmlFile = new File(url);
        try {
            Document document = (Document) builder.build(xmlFile);
            Element rootNode = document.getRootElement();
            Element childNode = rootNode.getChild("compile");
            if (string.length() > 10) {
                System.out.println("COMPILATION ERROR");
                childNode.getChild("status").setText("error");
                childNode.getChild("output").setText(string);
            } else {
                System.out.println("OK");
                zipFile(user, id);
                childNode.getChild("status").setText("true");
                childNode.getChild("output").setText("OK");
                String connectionURL = USERS_INFO;
                Connection connection = null;
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(connectionURL, USERNAME, PASSWORD);
                Statement s = connection.createStatement();
                String sql2 = "INSERT INTO users_info.compiled (`username`, `id` ,`filename`) VALUES ('" + user + "', '" + id + "','" + file + "')";
                s.executeUpdate(sql2);
                s.close();
                connection.close();
            }
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, new FileWriter(url));
        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException jdomex) {
            System.out.println(jdomex.getMessage());
        }
    }
}

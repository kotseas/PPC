package com.www.server;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.io.FilenameUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Upload extends HttpServlet {

    private String CONSOLE;
    private String Path;
    private boolean isMultipart;
    private boolean areaCode = false;
    private boolean uploadCode = false;
    private boolean nameArea = false;
    private String userPath;
    private String fileFolder;
    private String fileName;
    private String fileName1;
    private String fileId;
    private String sizeFile;
    private String fileComment;
    private String timeFile;
    private String dateFile;
    private String userNameF;
    private String newCode;
    private Date fileDate;
    private Date fileTime;
    private FileWriter fileWriter;
    private String LIB;
    private String JAVAC;
    private String DX;
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
        isMultipart = ServletFileUpload.isMultipartContent(request);
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        String message = "";
        //Http session = null;

        try {
            List fileItems = upload.parseRequest(request);
            Iterator i = fileItems.iterator();
            FileItem fi = null;
            FileItem code = null;

            while (i.hasNext()) {
                fi = (FileItem) i.next();
                String fieldName = fi.getFieldName();
                if (fieldName.equals("comment")) {
                    fileComment = fi.getString();
                    //System.out.println(fileComment);
                }
                if (fieldName.equals("username")) {
                    userNameF = fi.getString();
                    //System.out.println(userNameF);
                }
                if (fieldName.equals("filename")) {
                    fileName1 = fi.getString();
                    //System.out.println(fileName1);
                    if (fileName1 == null || fileName1.equals("")) {
                        nameArea = false;
                    } else {
                        nameArea = true;
                    }
                }
                if (fieldName.equals("myTextarea")) {
                    newCode = fi.getString();
                    //System.out.println(newCode);
                    if (newCode == null || newCode.equals("")) {
                        areaCode = false;
                    } else {
                        areaCode = true;
                    }
                    if (areaCode == true) {
                        if (!nameArea) {
                            message = "Give name to your file please";
                            request.setAttribute("message", message);
                            RequestDispatcher view = request.getRequestDispatcher("main/upload.jsp");
                            view.forward(request, response);
                            break;
                        }
                    }
                }
                if (!fi.isFormField()) {
                    fileName = fi.getName();
                    code = fi;
                    System.out.println(fileName);
                    if ((fileName == null) || (fileName.equals(""))) {
                        uploadCode = false;
                    } else {
                        uploadCode = true;
                    }

                }
            }
            System.out.println(areaCode);
            System.out.println(uploadCode);
            //-----CASE:1 upload and textarea both-----//
            if (uploadCode == true && areaCode == true) {
                message = "Only one code";
                request.setAttribute("message", message);
                RequestDispatcher view = request.getRequestDispatcher("main/upload.jsp");
                view.forward(request, response);
            }//-----CASE:2 no upload no textarea-----//
            else if ((uploadCode == false) && (areaCode == false)) {
                message = "Give some code";
                request.setAttribute("message", message);
                RequestDispatcher view = request.getRequestDispatcher("main/upload.jsp");
                view.forward(request, response);
            }//-----CASE:3 only upload no textarea-----//
            else if (uploadCode == true && areaCode == false) {
                if (nameArea) {
                    message = "Only one name";
                    request.setAttribute("message", message);
                    RequestDispatcher view = request.getRequestDispatcher("main/upload.jsp");
                    view.forward(request, response);
                    //break;
                } else {
                    System.out.println("in1");
                    if (fileName.substring(fileName.lastIndexOf(".") + 1).equalsIgnoreCase("java")) {
                        System.out.println("in2");
                        getValues(fileName);
                        System.out.println("in3");
                        new File(fileFolder).mkdir();
                        String location = (fileFolder + "/" + fileName);
                        File file = new File(location);
                        code.write(file);
                        long size = file.length();
                        sizeFile = Long.toString(size) + "B";
                        System.out.println("in4");
                        composeXml(fileName);
                        System.out.println("in5");
                        compileFile(userNameF, fileName, fileId);
                        System.out.println("in6");
                        response.sendRedirect(response.encodeRedirectURL("XmlParser"));
                    } else {
                        message = "The file must be a .java file";
                        request.setAttribute("message", message);
                        RequestDispatcher view = request.getRequestDispatcher("main/upload.jsp");
                        view.forward(request, response);
                        //break;
                    }
                }
            } //-----CASE:4 no upload only textarea-----//
            else if (uploadCode == false && areaCode == true) {
                getValues(fileName1 + ".java");
                new File(fileFolder).mkdir();
                File fileCode = new File(fileFolder + "/" + fileName1 + ".java");
                fileWriter = new FileWriter(fileCode);
                fileWriter.write(newCode);
                fileWriter.close();
                long size = fileCode.length();
                sizeFile = Long.toString(size) + "B";
                composeXml(fileName1 + ".java");
                compileFile(userNameF, fileName1 + ".java", fileId);
                response.sendRedirect(response.encodeRedirectURL("XmlParser"));
            }



        } catch (Exception ex) {
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
    }

    protected void composeXml(String nameOfFile) throws ParserConfigurationException, TransformerException {
        try {
            String file_Name = nameOfFile;
            Element root = new Element("file");
            Document doc = new Document(root);
            doc.setRootElement(root);
            Element username = new Element("username").setText(userNameF);
            Element name = new Element("name").setText(file_Name);
            Element id = new Element("id").setText(fileId);
            Element comment = new Element("comment").setText(fileComment);
            Element size = new Element("size").setText(sizeFile);
            Element unread = new Element("new").setText("true");
            Element counter = new Element("counter").setText("0");
            Element upload = new Element("upload");
            upload.addContent(new Element("time").setText(timeFile));
            upload.addContent(new Element("date").setText(dateFile));
            Element compile = new Element("compile");
            compile.addContent(new Element("status").setText("false"));
            compile.addContent(new Element("time").setText(timeFile));
            compile.addContent(new Element("date").setText(dateFile));
            compile.addContent(new Element("output").setText(""));
            Element sent = new Element("sent");
            sent.addContent(new Element("status").setText("false"));
            sent.addContent(new Element("time").setText(timeFile));
            sent.addContent(new Element("date").setText(dateFile));
            sent.addContent(new Element("output").setText(""));
            Element reply = new Element("reply");
            reply.addContent(new Element("status").setText("false"));
            reply.addContent(new Element("time").setText(timeFile));
            reply.addContent(new Element("date").setText(dateFile));
            reply.addContent(new Element("output").setText(""));
            doc.getRootElement().addContent(username);
            doc.getRootElement().addContent(name);
            doc.getRootElement().addContent(id);
            doc.getRootElement().addContent(comment);
            doc.getRootElement().addContent(size);
            doc.getRootElement().addContent(unread);
            doc.getRootElement().addContent(counter);
            doc.getRootElement().addContent(upload);
            doc.getRootElement().addContent(compile);
            doc.getRootElement().addContent(sent);
            doc.getRootElement().addContent(reply);
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(doc, new FileWriter(fileFolder + "/" + fileId + ".xml"));
        } catch (Exception ex) {
            //Logger.getLogger(UploadFile.class
            //.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void getValues(String NameOfFile) throws ClassNotFoundException, SQLException {
        System.out.println("1");
        String file_Name = NameOfFile;
        String connectionURL = USERS_INFO;
        Connection connection = null;
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(connectionURL, USERNAME, PASSWORD);
        ResultSet rs;
        Statement s = connection.createStatement();
        System.out.println("2");
        s.executeUpdate("UPDATE down_up SET id = (id + 1) WHERE status='upload'");
        String sql = "SELECT id FROM down_up WHERE status='upload'";
        rs = s.executeQuery(sql);
        rs.next();
        fileId = rs.getString("id");
        String sql2 = "INSERT INTO users_info.file_id (`username`, `id` ,`filename`) VALUES ('" + userNameF + "', '" + fileId + "','" + file_Name + "')";
        s.executeUpdate(sql2);
        System.out.println("3");
        DateFormat date = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calen = Calendar.getInstance();
        fileDate = calen.getTime();
        dateFile = date.format(fileDate);
        DateFormat time = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        fileTime = cal.getTime();
        timeFile = time.format(fileTime);
        userPath = Path + "/" + userNameF;
        fileFolder = userPath + "/" + fileId;
        System.out.println(fileFolder);
        new File(fileFolder).mkdir();
        rs.close();
        s.close();
        connection.close();
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
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(connectionURL, USERNAME, PASSWORD);
                Statement s = connection.createStatement();
                String sql2 = "INSERT INTO users_info.compiled (`username`, `id` ,`filename`) VALUES ('" + user + "', '" + id + "','" + file + "')";
                s.executeUpdate(sql2);
                s.close();
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

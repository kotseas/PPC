
<%@page import="org.apache.commons.io.FileUtils"%>
<%@page import="java.io.DataInputStream"%>
<%@page import="java.io.BufferedInputStream"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="org.w3c.dom.*, javax.xml.parsers.*" %>
<%@page import="java.io.File"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page language="java" %>
<%@page import="org.w3c.dom.*" %>
<%@page import="org.jdom.Document" %>
<%@page import="org.jdom.Element" %>
<%@page import="org.jdom.JDOMException" %>
<%@page import="org.jdom.input.SAXBuilder" %>
<%@page import="java.io.IOException;" %>
<%@page import="java.lang.*" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>


<html>
    <%String url = "http://83.212.101.72:8080/Server/";%>
<%String xmlPath = "/var/lib/tomcat6/webapps/Server/DB";%>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="shortcut icon" href="../ppc.ico" />
        <title>Details - <%=session.getAttribute("username")%> - PPC </title>
        <link href=<%= url + "/main/style.css"%> rel="stylesheet" type="text/css" />
    </head>
    <body>
        <div id="top">
            <a id="header" href=<%= url + "/XmlParser"%>><h1>PPC</h1></a>
            <nav id="username">
                <ul>
                    <li>                    <a href="#"><%=session.getAttribute("username")%></a>
                        <ul>
                            <li><a href=<%= url + "/Signout"%>>Sign out</a></li>
                        </ul>
                    </li>
                </ul>
            </nav>
        </div>


        <div id="container">  
            <div id="left">
                <form action=<%= url + "/main/upload.jsp"%> method="get" enctype="multipart/form-data">
                    <div id="button_home"><input type="submit" value="NEW" /></div>
                </form>
            </div>

            <form action=<%= url + "/Edit"%> method="post" enctype="multipart/form-data">
                <%
                    String row = request.getParameter("row");
                    String username = (String) session.getAttribute("username");
                    String java = "";
                    int id = Integer.valueOf(row);
                    SAXBuilder builder = new SAXBuilder();
                    File xmlFile = new File(xmlPath + "/" + username + "/" + id + "/" + id + ".xml");
                    Document document = (Document) builder.build(xmlFile);
                    Element rootNode = document.getRootElement();
                    String javaName = rootNode.getChildText("name");
                    Element upload = rootNode.getChild("upload");
                    Element compile = rootNode.getChild("compile");
                    Element sent = rootNode.getChild("sent");
                    Element reply = rootNode.getChild("reply");
                    //System.out.println(javaName);
                    File file = new File(xmlPath + "/" + username + "/" + id + "/" + javaName);
                    FileInputStream fis = null;
                    BufferedInputStream bis = null;
                    DataInputStream dis = null;
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    dis = new DataInputStream(bis);
                    while (dis.available() != 0) {
                        java = java + (dis.readLine()) + "\n";
                    }
                    File dir = new File(xmlPath + "/" + username + "/" + id);
                    File[] listOfFiles = dir.listFiles();
                    String content = "";
                    int flaglog = 0;
                    for (int i = 0; i < listOfFiles.length; i++) {
                        File log = listOfFiles[i];
                        if (log.isFile() && log.getName().endsWith(".log")) {
                            content = FileUtils.readFileToString(log);
                            flaglog = 1;
                        }
                    }

                %>
                <div id="details1" >
                    <div id="names1">Filename: </div>
                    <div id="values1"> <%= rootNode.getChildText("name")%></div>
                </div>
                <div id="details1" >
                    <div id="names1">Comment: </div>
                    <div id="values1"> <%= rootNode.getChildText("comment")%></div>
                </div>  
                <div id="details1" >
                    <div id="names1">Time: </div>
                    <div id="values1"> <%= upload.getChildText("time")%> </div>
                </div> 
                <div id="details1" >
                    <div id="names1">Date: </div>
                    <div id="values1"> <%= upload.getChildText("date")%> </div>
                </div> 
                <div id="details1" >
                    <div id="names1">Size: </div>
                    <div id="values1"> <%= rootNode.getChildText("size")%> </div>
                </div>
                <% if (flaglog == 1) {%>
                <div id="details1" >
                    <p id="names1">LOG: </p>
                    <pre id="values1"> <%= content%></pre>
                </div>
                <%}%>
                <% if (compile.getChildText("status").equals("true")) {
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = new Date();
                        String compileDate = "";
                        if ((compile.getChildText("date")).equals(dateFormat.format(date))) {
                            compileDate = upload.getChildText("time");
                        } else {
                            compileDate = upload.getChildText("date");
                        }%>
                <div id="details1" >
                    <div id="names1">Code: </div>
                    <div id="values1">   <pre><code><textarea  name="javatext" readonly> <%= java%></textarea> </code></pre></div>
                </div> 
                <div id="details1" >
                    <div id="names1">Compile: </div>
                    <div id="values1"> <font color='green'>DONE</font> <%= compileDate%> </div>
                </div>
                <%}
                    if (compile.getChildText("status").equals("error")) {
                        session = request.getSession(true);
                        //String javaCode=request.getParameter("javatext");
                        //session.setAttribute("javaCode", javaCode);
                        session.setAttribute("javaName", javaName);
                        session.setAttribute("id", row);%>
                <div id="details1" >
                    <div id="names1">Code: </div>
                    <div id="values1"> <pre> <code><textarea  name="javatext"> <%= java%></textarea> </code></pre></div>
                    <div id="edit"><div id="button_upload"><input type="submit" value="OK" /></div>
                        <form action=<%= url + "/XmlParser"%> method="get" enctype="multipart/form-data">
                            <div id="button_cancel1"><input type="submit" value="CANCEL" /></div>
                        </form>
                    </div>
                </div>


                <div id="details1" >
                    <div id="names1">Compile: </div>
                    <div id="values1"><font color='red'>FAILED</font></div>
                    <div id="out"><%= compile.getChildText("output")%></div>
                </div>  


                <% }
                    if (compile.getChildText("status").equals("false")) {%>
                <div id="details1" >
                    <div id="names1"> Code:</div>
                    <div id="values1"><pre><code><textarea  name="javatext" readonly><%= java%></textarea></code></pre></div>
                </div> 
                <%     }
                    if (sent.getChildText("status").equals("true")) {
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = new Date();
                        String sentDate = "";
                        if ((sent.getChildText("date")).equals(dateFormat.format(date))) {
                            sentDate = upload.getChildText("time");
                        } else {
                            sentDate = upload.getChildText("date");
                        }%>
                <div id="details1" >
                    <div id="names1">Sent for execution: </div>
                    <div id="values1"><font color='green'>DONE</font> <%= sentDate%> </div>
                </div>
                <%}
                    if (sent.getChildText("status").equals("false")) {
                    }
                    if (reply.getChildText("status").equals("true")) {
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = new Date();
                        String replyDate = "";
                        if ((reply.getChildText("date")).equals(dateFormat.format(date))) {
                            replyDate = upload.getChildText("time");
                        } else {
                            replyDate = upload.getChildText("date");
                        }%>
                <div id="details1" >
                    <div id="names1">Reply: </div>
                    <div id="values1"> <font color='green'>DONE</font> <%= replyDate%> </div>
                </div>
                <%}
                    if (reply.getChildText("status").equals("false")) {
                    }%>

            </form>

        </div> 

    </body>
</html>
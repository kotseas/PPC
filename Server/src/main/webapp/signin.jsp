<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page language="Java" import="java.sql.*" %> 
<!DOCTYPE html>
<html>
    <head>
        <link rel="shortcut icon" href="ppc.ico" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title></title>
    </head>
    <body>
        <jsp:useBean id="signin" scope="request" class="com.www.server.SigninBean" >
            <jsp:setProperty name="signin" property="userName" value='<%=request.getParameter("user")%>'/>
            <jsp:setProperty name="signin" property="password" value='<%=request.getParameter("pass")%>'/>
        </jsp:useBean>
        <jsp:forward page="Signin">
            <jsp:param name="user" value="<%=signin.getUserName()%>" />
            <jsp:param name="pass" value="<%=signin.getPassword()%>" />
        </jsp:forward> 
    </body>
</html>

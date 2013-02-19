package com.www.server;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Signin extends HttpServlet {

    //private ServletConfig config;
    private String USERNAME;
    private String PASSWORD;
    private String USERS_INFO;

    @Override
    public void init() throws ServletException {
        USERNAME = getServletContext().getInitParameter("username");
        PASSWORD = getServletContext().getInitParameter("password");
        USERS_INFO = getServletContext().getInitParameter("users_info");
        System.out.println(USERNAME + "|" + PASSWORD + "|" + USERS_INFO);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session;
        PrintWriter out = response.getWriter();
        String connectionURL = USERS_INFO;
        Connection connection = null;
        ResultSet rs;
        String userName = "";
        String passwrd = "";
        String message = "";
        response.setContentType("text/html");
        try {
            // Load the database driver
            Class.forName("com.mysql.jdbc.Driver");
            // Get a Connection to the database
            connection = DriverManager.getConnection(connectionURL, USERNAME, PASSWORD);
            //System.out.println("Connected to the database");
            //Add the data into the database
            String sql = "SELECT username, password FROM users";
            Statement s = connection.createStatement();
            s.executeQuery(sql);
            rs = s.getResultSet();
            while (rs.next()) {
                userName = rs.getString("username");
                passwrd = rs.getString("password");
                if (userName.equals(request.getParameter("user")) && passwrd.equals(request.getParameter("pass"))) {
                    break;
                }
            }
            rs.close();
            s.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Exception is ;" + e);
        }
        if (userName.equals(request.getParameter("user")) && passwrd.equals(request.getParameter("pass"))) {
            session = request.getSession(true);
            session.setAttribute("username", userName);
            response.sendRedirect(response.encodeRedirectURL("XmlParser"));
        } else {
            message = "Invalid username or password";
            request.setAttribute("LoginMessage", message);
            RequestDispatcher view = request.getRequestDispatcher("index.jsp");
            view.forward(request, response);
        }
    }
}
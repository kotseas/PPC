package com.www.server;

import java.io.*;
import java.io.File;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Signup extends HttpServlet {

    private String mainPath;
    private PrintWriter out;
    private String USERNAME;
    private String PASSWORD;
    private String USERS_INFO;

    @Override
    public void init() {
        mainPath = getServletContext().getInitParameter("mainPath");
        USERNAME = getServletContext().getInitParameter("username");
        PASSWORD = getServletContext().getInitParameter("password");
        USERS_INFO = getServletContext().getInitParameter("users_info");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        out = response.getWriter();
		HttpSession session;
        String connectionURL = USERS_INFO;
        Connection connection = null;
        ResultSet rs;
        String email = "";
        String userName = "";
        String passwrd = "";
        String remoteAddr = "";
        response.setContentType("text/html");
        int error = 0;
        try {
            // Load the database driver
            Class.forName("com.mysql.jdbc.Driver");
            // Get a Connection to the database
            connection = DriverManager.getConnection(connectionURL, USERNAME, PASSWORD);
            //Add the data into the database
            String sql = "SELECT username, email FROM users";
            Statement s = connection.createStatement();
            s.executeQuery(sql);
            rs = s.getResultSet();
            while (rs.next()) {
                email = rs.getString("email");
                userName = rs.getString("username");
                if (email.equals(request.getParameter("email"))) {
                    String message = "Email" + email + "already exists";
                    request.setAttribute("RegisterMessage", message);
                    RequestDispatcher view = request.getRequestDispatcher("signup.jsp");
                    view.forward(request, response);
                    error = 1;
                }
                if (userName.equals(request.getParameter("user"))) {
                    String message = "Username '" + userName + "' already exists";
                    request.setAttribute("RegisterMessage", message);
                    RequestDispatcher view = request.getRequestDispatcher("signup.jsp");
                    view.forward(request, response);
                    error = 1;
                }
                if (error == 1) {
                    break;
                }
            }
            passwrd = request.getParameter("pass");
            if (!passwrd.equalsIgnoreCase(request.getParameter("pass2"))) {
                String message = "Passwords don't match";
                request.setAttribute("RegisterMessage", message);
                RequestDispatcher view = request.getRequestDispatcher("signup.jsp");
                view.forward(request, response);
                error = 1;
            }
            remoteAddr = request.getRemoteAddr();
            //ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
            //reCaptcha.setPrivateKey("6LezstoSAAAAAEE9lfB6TR2kEX81_peDt4n03K4l");
            //String challenge = request.getParameter("recaptcha_challenge_field");
            //String uresponse = request.getParameter("recaptcha_response_field");
            //ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
            /*if (!reCaptchaResponse.isValid()) { 
             print_wrong_once(error); 
             out.print("<h2 align=\"center\">Validation code is wrong.</h2>"); 
             error = 1; 
             }*/
            if (error == 1) {
                rs.close();
                s.close();
                return;
            } else {
                sql = "INSERT INTO users_info.users (`username`, `password`, `email`) VALUES ('" + request.getParameter("user") + "', '" + request.getParameter("pass") + "', '" + request.getParameter("email") + "')";
                s.executeUpdate(sql);
                File dir = new File(mainPath + "/" + request.getParameter("user"));
                dir.mkdir();
				session = request.getSession(true);
                session.setAttribute("username", request.getParameter("user"));
                response.sendRedirect(response.encodeRedirectURL("XmlParser"));
            }
            rs.close();
            s.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e);
        }
    }
}
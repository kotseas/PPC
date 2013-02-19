package com.www.server;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
public class SigninFilter implements Filter {
    //String url = "http://83.212.101.72:8080/Server/";
    String url = "http://localhost:8084/Server/";
    @Override
    public void init(FilterConfig filterConfig) {}
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession();
        //System.out.println("filter: " + (String) session.getAttribute("username"));
        if (session == null || session.getAttribute("username") == null) {
            // No logged-in user found, so redirect to login page.
            response.sendRedirect(url);
        } else {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            response.setDateHeader("Expires", 0); // Proxies.
            chain.doFilter(req, res);
        }
    }
    @Override
    public void destroy() {}
}
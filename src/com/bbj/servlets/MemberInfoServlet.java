package com.bbj.servlets;

import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class MemberInfoServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        
        JSONObject json = new JSONObject();
        
        if (session != null && session.getAttribute("userId") != null) {
            json.put("userId", session.getAttribute("userId"));
            json.put("firstName", session.getAttribute("firstName"));
            json.put("lastName", session.getAttribute("lastName"));
            json.put("email", session.getAttribute("email"));
            json.put("role", session.getAttribute("role"));
            json.put("authenticated", true);
        } else {
            json.put("authenticated", false);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        
        out.print(json);
    }
}

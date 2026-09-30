package com.lms.servlet;

import com.lms.util.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Simple health endpoint for Render. */
@WebServlet("/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            DBUtil.scalar("SELECT 1");
            resp.setStatus(200);
            resp.getWriter().write("{\"status\":\"UP\",\"db\":\"UP\"}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"status\":\"DOWN\"}");
        }
    }
}

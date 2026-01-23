package com.example.prodwebapp;

import java.io.IOException;

import com.example.prodwebapp.lib.ViewResolver;
import com.example.prodwebapp.product.ProductController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("*.do")
public class DispatcherServlet extends HttpServlet {

    ProductController pc = new ProductController();

    // localhost:8080/product.do?cmd=list
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 라우팅
        String cmd = req.getParameter("cmd");
        if ("list".equals(cmd)) {
            String viewName = pc.list(req, resp);
            ViewResolver.render(viewName).forward(req, resp);
        } else if ("insert-form".equals(cmd)) {
            String viewName = pc.insertForm(req, resp);
            ViewResolver.render(viewName).forward(req, resp);
        } else if ("detail".equals(cmd)) {
            String viewName = pc.detail(req, resp);
            ViewResolver.render(viewName).forward(req, resp);
        }
    }

    // localhost:8080/product.do?cmd=insert
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 1. 라우팅
        String cmd = req.getParameter("cmd");
        if ("insert".equals(cmd)) {
            // url = /product.do?cmd=list
            String url = pc.insert(req, resp);
            resp.setStatus(302);
            resp.setHeader("Location", url);
        }
    }
}

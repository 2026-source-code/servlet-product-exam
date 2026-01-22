package com.example.prodwebapp;

import java.io.IOException;

import com.example.prodwebapp.lib.View;
import com.example.prodwebapp.lib.ViewResolver;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("*.do")
public class DispatcherServlet extends HttpServlet {

    // localhost:8080/product.do?cmd=list
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 라우팅
        String cmd = req.getParameter("cmd");
        switch (cmd) {
            case "list":
                ViewResolver.render("list").forward(req, resp);
                break;
            case "insert-form":
                ViewResolver.render("insert-form").forward(req, resp);
                break;
            case "detail":
                ViewResolver.render("detail").forward(req, resp);
                break;
            default:
                break;
        }
    }

    // delete, post 두 요청을 이 메서드로 받을 예정
    // 이유 : form 태그는 post, get 요청만 할 수 있다.
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

}

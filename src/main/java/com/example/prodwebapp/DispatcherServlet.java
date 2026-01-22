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
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // DBConnection.getConnection();
        String cmd = req.getParameter("cmd");
        if ("list".equals(cmd)) {
            View view = ViewResolver.render("detail");
            view.forward(req, resp);
            return;
        }

    }

    // delete, post 두 요청을 이 메서드로 받을 예정
    // 이유 : form 태그는 post, get 요청만 할 수 있다.
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

}

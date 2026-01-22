package com.example.prodwebapp.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import jakarta.servlet.ServletException;

// SRP(단일책임의원칙) : 
// templates폴더에 mustache 파일이 있어서 그걸 연결해주는 친구
public class ViewResolver {

    // mustache 파일을 읽어서 -> Servlet으로 변경해서 리턴턴
    public static View render(String viewName) throws ServletException, IOException {
        String resourcePath = "templates/" + viewName + ".mustache";
        InputStream in = ViewResolver.class.getClassLoader()
                .getResourceAsStream(resourcePath);

        if (in == null) {
            throw new ServletException("Template not found: " + resourcePath);
        }

        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            Template template = Mustache.compiler().compile(reader);
            return new View(template);
        }
    }
}
package com.example.prodwebapp.lib;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.samskivert.mustache.Template;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class View {
    private Template template;

    public View(Template template) {
        this.template = template;
    }

    public void forward(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html; charset=UTF-8");

        // req에 저장된 모든 attribute를 model로 사용
        Map<String, Object> model = new HashMap<>();

        Enumeration<String> attributeNames = req.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            Object value = req.getAttribute(key);

            // List인 경우 그대로 저장
            if (value instanceof List) {
                model.put(key, value);
            }
            // Map인 경우 모든 항목을 개별 키로 추가
            else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                model.putAll(map);
            }
            // 그 외의 경우 키 이름 그대로 저장
            else {
                model.put(key, value);
            }
        }

        // Mustache 템플릿에서 request 객체에 접근할 수 있도록 추가
        model.put("request", req);

        template.execute(model, resp.getWriter());
        resp.getWriter().flush();
    }
}

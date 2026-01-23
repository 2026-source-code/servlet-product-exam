package com.example.prodwebapp.product;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProductController {

    ProductService productService = new ProductService();

    public String list(HttpServletRequest req, HttpServletResponse resp) {
        List<Product> models = productService.상품목록();
        req.setAttribute("models", models);
        req.setAttribute("what", "엉?");

        return "list"; // view 이름
    }

    public String insertForm(HttpServletRequest req, HttpServletResponse resp) {
        return "insert-form"; // view 이름
    }

    public String detail(HttpServletRequest req, HttpServletResponse resp) {
        int id = Integer.parseInt(req.getParameter("id"));
        Product model = productService.상품상세(id);
        req.setAttribute("model", model);
        return "detail"; // view 이름
    }

    public String insert(HttpServletRequest req, HttpServletResponse resp) {
        String name = req.getParameter("name");
        String price = req.getParameter("price");
        String qty = req.getParameter("qty");

        int price2 = Integer.parseInt(price);
        int qty2 = Integer.parseInt(qty);
        productService.상품등록(name, price2, qty2);

        return "/product.do?cmd=list"; // 주소 url
    }

    public String delete(HttpServletRequest req, HttpServletResponse resp) {
        int id = Integer.parseInt(req.getParameter("id"));
        productService.상품삭제(id);
        return "/product.do?cmd=list"; // 주소 url
    }

}

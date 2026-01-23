package com.example.prodwebapp.product;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProductController {

    ProductService productService = new ProductService();

    public String list(HttpServletRequest req, HttpServletResponse resp) {
        List<Product> models = productService.상품목록();
        req.setAttribute("models", models);
        req.setAttribute("what", "엉?");

        return "list";
    }

    public String insertForm(HttpServletRequest req, HttpServletResponse resp) {
        return "insert-form";
    }

    public String detail(HttpServletRequest req, HttpServletResponse resp) {
        int id = Integer.parseInt(req.getParameter("id"));
        Product model = productService.상품상세(id);
        req.setAttribute("model", model);
        return "detail";
    }

    public String insert(HttpServletRequest req, HttpServletResponse resp) {
        String name = req.getParameter("name");
        String price = req.getParameter("price");
        String qty = req.getParameter("qty");

        int price2 = Integer.parseInt(price);
        int qty2 = Integer.parseInt(qty);
        productService.상품등록(name, price2, qty2);
        return "/product.do?cmd=list";
    }

}

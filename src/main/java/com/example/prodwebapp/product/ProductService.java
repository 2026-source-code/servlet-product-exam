package com.example.prodwebapp.product;

import java.util.List;

// 트랜잭셔 관리
public class ProductService {

    ProductRepository repo = new ProductRepository();

    public void 상품등록(String name, int price, int qty) {
        int result = repo.insert(name, price, qty);
        if (result != 1)
            throw new RuntimeException("상품등록이 완료되지 않았습니다");
    }

    public List<Product> 상품목록() {
        return repo.findAll();
    }

    public Product 상품상세(int id) {
        Product product = repo.findById(id);
        if (product == null)
            throw new RuntimeException("상품을 찾을 수 없어요 id를 확인하세요 : 명령어 get");
        return product;
    }

    public void 상품삭제(int id) {
        int result = repo.deleteById(id);
        if (result != 1)
            throw new RuntimeException("상품삭제가 완료되지 않았습니다");
    }
}
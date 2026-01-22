package com.example.prodwebapp.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.prodwebapp.DBConnection;

public class ProductRepository {

    Connection conn = DBConnection.getConnection();

    // 1. insert(String name, int price, int qty)
    public int insert(String name, int price, int qty) {
        String sql = "insert into product(name, price, qty) values(?,?,?)";
        try {
            // 2. 버퍼달기
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, price);
            pstmt.setInt(3, qty);

            // 3. 쿼리전송
            int result = pstmt.executeUpdate();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // -1, 0, 1이 머지? 정리!!
    }

    // 2. deleteById(int id)
    public int deleteById(int id) {
        String sql = "delete from product where id = ?";
        try {
            // 2. 버퍼달기
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            // 3. 쿼리전송
            int result = pstmt.executeUpdate();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // -1, 0, 1이 머지? 정리!!
    }

    // 3. findById(int id)
    public Product findById(int id) {
        try {
            String sql = "select * from product where id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            // 조회해서 view로 응답받기
            ResultSet rs = pstmt.executeQuery(); // select할때!!

            // 커서 한칸 내리기
            boolean isRow = rs.next();

            // 행이 존재하면 프로젝션(열 선택하기)
            if (isRow) {
                int c1 = rs.getInt("id");
                String c2 = rs.getString("name");
                int c3 = rs.getInt("price");
                int c4 = rs.getInt("qty");
                Product product = new Product(c1, c2, c3, c4);
                return product;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 4. findAll()
    public List<Product> findAll() {
        try {
            String sql = "select * from product";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            // 조회해서 view로 응답받기
            ResultSet rs = pstmt.executeQuery(); // select할때!!

            // 행이 존재하면 프로젝션(열 선택하기)
            List<Product> list = new ArrayList<>();
            while (rs.next()) {
                int c1 = rs.getInt("id");
                String c2 = rs.getString("name");
                int c3 = rs.getInt("price");
                int c4 = rs.getInt("qty");
                Product product = new Product(c1, c2, c3, c4);
                list.add(product);
            }
            return list;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Arrays.asList();
    }
}

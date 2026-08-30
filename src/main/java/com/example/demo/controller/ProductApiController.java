package com.example.demo.controller;

import com.example.demo.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductApiController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/buscar")
    public List<Product> searchProducts(@RequestParam("q") String query) {
        if (query == null || query.trim().length() < 1) {
            return List.of();
        }

        String searchRegex = ".*" + query.trim() + ".*";
        Criteria criteria = new Criteria().andOperator(
            Criteria.where("active").is(true),
            new Criteria().orOperator(
                Criteria.where("name").regex(searchRegex, "i"),
                Criteria.where("brand").regex(searchRegex, "i"),
                Criteria.where("activeIngredient").regex(searchRegex, "i")
            )
        );

        Query searchQ = new Query(criteria).limit(6);
        return mongoTemplate.find(searchQ, Product.class);
    }
}

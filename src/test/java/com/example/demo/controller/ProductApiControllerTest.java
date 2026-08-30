package com.example.demo.controller;

import com.example.demo.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ProductApiController productApiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productApiController).build();
    }

    @Test
    @DisplayName("GET /api/productos/buscar debe retornar productos cuando coincide la búsqueda")
    void testSearchProductsReturnsList() throws Exception {
        Product p = new Product();
        p.setId("p1");
        p.setName("Ibuprofeno 800mg");
        p.setBrand("Genfar");
        p.setPrice(new BigDecimal("4500"));
        p.setActive(true);

        when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(List.of(p));

        mockMvc.perform(get("/api/productos/buscar").param("q", "ibuprofeno"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Ibuprofeno 800mg"))
                .andExpect(jsonPath("$[0].brand").value("Genfar"));

        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("GET /api/productos/buscar debe retornar lista vacía si la query está en blanco")
    void testSearchProductsEmptyQuery() throws Exception {
        mockMvc.perform(get("/api/productos/buscar").param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(mongoTemplate, never()).find(any(), any());
    }
}

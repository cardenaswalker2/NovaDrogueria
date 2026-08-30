package com.example.demo;

import com.example.demo.util.ColombianCurrencyFormatter;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColombianCurrencyFormatterTest {

    private final ColombianCurrencyFormatter formatter = new ColombianCurrencyFormatter();

    @Test
    void testFormatMoneyValue() {
        assertEquals("$8.500", formatter.format(new BigDecimal("8500")));
        assertEquals("$30.000", formatter.format(new BigDecimal("30000")));
        assertEquals("$100.000", formatter.format(new BigDecimal("100000")));
        assertEquals("$1.000.000", formatter.format(new BigDecimal("1000000")));
        assertEquals("$8.500,50", formatter.format(new BigDecimal("8500.50")));
        assertEquals("$0", formatter.format(BigDecimal.ZERO));
    }

    @Test
    void testFormatToWords() {
        assertEquals("Ocho mil quinientos pesos colombianos", formatter.formatToWords(new BigDecimal("8500")));
        assertEquals("Treinta mil pesos colombianos", formatter.formatToWords(new BigDecimal("30000")));
        assertEquals("Veintiún mil pesos colombianos", formatter.formatToWords(new BigDecimal("21000")));
        assertEquals("Cien mil pesos colombianos", formatter.formatToWords(new BigDecimal("100000")));
        assertEquals("Ciento veinticinco mil pesos colombianos", formatter.formatToWords(new BigDecimal("125000")));
        assertEquals("Un millón de pesos colombianos", formatter.formatToWords(new BigDecimal("1000000")));
        assertEquals("Dos millones quinientos mil pesos colombianos", formatter.formatToWords(new BigDecimal("2500000")));
        assertEquals("Ocho mil quinientos pesos colombianos con cincuenta centavos", formatter.formatToWords(new BigDecimal("8500.50")));
        assertEquals("Un peso colombiano con un centavo", formatter.formatToWords(new BigDecimal("1.01")));
    }
}

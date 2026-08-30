package com.example.demo.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component("currencyFormatter")
public class ColombianCurrencyFormatter {

    private static final String[] UNIDADES = {
        "", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"
    };

    private static final String[] DECENAS = {
        "", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"
    };

    private static final String[] ONCE_A_DIECINUEVE = {
        "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve"
    };

    private static final String[] VEINTES = {
        "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis", "veintitrés", "veintiocho", "veintinueve"
    };

    private static final String[] CENTENAS = {
        "", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"
    };

    public String format(BigDecimal amount) {
        if (amount == null) return "$0";
        
        // Locale.COLOMBIA format: check if it has fractional digits
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        
        DecimalFormat df;
        if (amount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            df = new DecimalFormat("$#,##0", symbols);
        } else {
            df = new DecimalFormat("$#,##0.00", symbols);
        }
        return df.format(amount);
    }

    public String formatToWords(BigDecimal amount) {
        if (amount == null) return "Cero pesos colombianos";
        
        long integerPart = amount.longValue();
        BigDecimal fractional = amount.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
        int fractionalPart = Math.round(fractional.floatValue());
        
        StringBuilder sb = new StringBuilder();
        if (integerPart == 0) {
            sb.append("cero");
        } else {
            sb.append(convertNumberToWords(integerPart));
        }

        if (integerPart == 1) {
            sb.append(" peso colombiano");
        } else {
            String words = sb.toString().trim();
            if (words.endsWith("millón") || words.endsWith("millones")) {
                sb.append(" de pesos colombianos");
            } else {
                sb.append(" pesos colombianos");
            }
        }

        if (fractionalPart > 0) {
            sb.append(" con ").append(convertNumberToWords(fractionalPart));
            if (fractionalPart == 1) {
                sb.append(" centavo");
            } else {
                sb.append(" centavos");
            }
        }

        // Capitalize first letter
        String result = sb.toString().trim();
        if (result.isEmpty()) return "";
        return Character.toUpperCase(result.charAt(0)) + result.substring(1);
    }

    private String convertNumberToWords(long number) {
        if (number < 0) {
            return "menos " + convertNumberToWords(-number);
        }
        if (number < 10) {
            return UNIDADES[(int) number];
        }
        if (number < 20) {
            return ONCE_A_DIECINUEVE[(int) (number - 10)];
        }
        if (number < 30) {
            if (number == 20) return "veinte";
            String unit = UNIDADES[(int) (number % 10)];
            if (unit.equals("un")) return "veintiún";
            return VEINTES[(int) (number - 20)];
        }
        if (number < 100) {
            int dec = (int) (number / 10);
            int uni = (int) (number % 10);
            if (uni == 0) return DECENAS[dec];
            String unitWord = UNIDADES[uni];
            if (unitWord.equals("un")) unitWord = "un";
            return DECENAS[dec] + " y " + unitWord;
        }
        if (number < 1000) {
            if (number == 100) return "cien";
            int cent = (int) (number / 100);
            long rest = number % 100;
            if (rest == 0) return CENTENAS[cent];
            return CENTENAS[cent] + " " + convertNumberToWords(rest);
        }
        if (number < 1000000) {
            long thousands = number / 1000;
            long rest = number % 1000;
            String thousandPart;
            if (thousands == 1) {
                thousandPart = "mil";
            } else {
                thousandPart = convertNumberToWords(thousands) + " mil";
            }
            // fix veintiun/un mil endings
            if (thousandPart.endsWith("un mil")) {
                thousandPart = thousandPart.substring(0, thousandPart.length() - 6) + "ún mil";
            } else if (thousandPart.startsWith("un mil")) {
                thousandPart = "mil";
            }
            if (rest == 0) return thousandPart;
            return thousandPart + " " + convertNumberToWords(rest);
        }
        if (number < 1000000000000L) {
            long millions = number / 1000000;
            long rest = number % 1000000;
            String millionPart;
            if (millions == 1) {
                millionPart = "un millón";
            } else {
                millionPart = convertNumberToWords(millions) + " millones";
            }
            if (millionPart.endsWith("un millones")) {
                millionPart = millionPart.substring(0, millionPart.length() - 11) + "ún millones";
            }
            
            if (rest == 0) return millionPart;
            return millionPart + " " + convertNumberToWords(rest);
        }
        return String.valueOf(number); // fallback safety
    }
}

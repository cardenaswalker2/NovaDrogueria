import 'package:intl/intl.dart';

class ColombianCurrencyFormatter {
  static String format(double amount) {
    // Format numeric value with '.' for thousands and ',' for decimals
    final NumberFormat formatter = NumberFormat.currency(
      locale: 'es_CO',
      symbol: '\$',
      decimalDigits: amount % 1 == 0 ? 0 : 2,
    );
    return formatter.format(amount);
  }

  static String formatToWords(double amount) {
    final int integerPart = amount.floor();
    final int fractionalPart = ((amount - integerPart) * 100).round();

    String result = _convertNumberToWords(integerPart);

    if (integerPart == 1) {
      result += " peso colombiano";
    } else {
      if (result.endsWith("millón") || result.endsWith("millones")) {
        result += " de pesos colombianos";
      } else {
        result += " pesos colombianos";
      }
    }

    if (fractionalPart > 0) {
      result += " con ${_convertNumberToWords(fractionalPart)}";
      result += fractionalPart == 1 ? " centavo" : " centavos";
    }

    if (result.isEmpty) return "";
    return result[0].toUpperCase() + result.substring(1).trim();
  }

  static final List<String> _UNIDADES = [
    "", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"
  ];

  static final List<String> _DECENAS = [
    "", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"
  ];

  static final List<String> _ONCE_A_DIECINUEVE = [
    "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve"
  ];

  static final List<String> _VEINTES = [
    "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis", "veintitrés", "veintiocho", "veintinueve"
  ];

  static final List<String> _CENTENAS = [
    "", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"
  ];

  static String _convertNumberToWords(int number) {
    if (number < 0) {
      return "menos ${_convertNumberToWords(-number)}";
    }
    if (number < 10) {
      return _UNIDADES[number];
    }
    if (number < 20) {
      return _ONCE_A_DIECINUEVE[number - 10];
    }
    if (number < 30) {
      if (number == 20) return "veinte";
      final String unit = _UNIDADES[number % 10];
      if (unit == "un") return "veintiún";
      return _VEINTES[number - 20];
    }
    if (number < 100) {
      final int dec = number ~/ 10;
      final int uni = number % 10;
      if (uni == 0) return _DECENAS[dec];
      return "${_DECENAS[dec]} y ${_UNIDADES[uni]}";
    }
    if (number < 1000) {
      if (number == 100) return "cien";
      final int cent = number ~/ 100;
      final int rest = number % 100;
      if (rest == 0) return _CENTENAS[cent];
      return "${_CENTENAS[cent]} ${_convertNumberToWords(rest)}";
    }
    if (number < 1000000) {
      final int thousands = number ~/ 1000;
      final int rest = number % 1000;
      String thousandPart;
      if (thousands == 1) {
        thousandPart = "mil";
      } else {
        thousandPart = "${_convertNumberToWords(thousands)} mil";
      }
      if (thousandPart.endsWith("un mil")) {
        thousandPart = "${thousandPart.substring(0, thousandPart.length - 6)}ún mil";
      } else if (thousandPart.startsWith("un mil")) {
        thousandPart = "mil";
      }
      if (rest == 0) return thousandPart;
      return "$thousandPart ${_convertNumberToWords(rest)}";
    }
    if (number < 1000000000000) {
      final int millions = number ~/ 1000000;
      final int rest = number % 1000000;
      String millionPart;
      if (millions == 1) {
        millionPart = "un millón";
      } else {
        millionPart = "${_convertNumberToWords(millions)} millones";
      }
      if (millionPart.endsWith("un millones")) {
        millionPart = "${millionPart.substring(0, millionPart.length - 11)}ún millones";
      }
      if (rest == 0) return millionPart;
      return "$millionPart ${_convertNumberToWords(rest)}";
    }
    return number.toString();
  }
}

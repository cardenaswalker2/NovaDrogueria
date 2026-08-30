import 'package:flutter/material.dart';

class NovaStyles {
  // Theme brand colors
  static const Color primary = Color(0xFF0D9488);
  static const Color primaryDark = Color(0xFF115E59);
  static const Color secondary = Color(0xFF0284C7);
  static const Color secondaryDark = Color(0xFF075985);
  static const Color background = Color(0xFFF8FAFC);
  static const Color cardBg = Colors.white;
  static const Color textDark = Color(0xFF0F172A);
  static const Color textMuted = Color(0xFF64748B);
  
  // Status colors
  static const Color colorPending = Color(0xFFF59E0B);
  static const Color colorConfirmed = Color(0xFF0284C7);
  static const Color colorPrepared = Color(0xFF8B5CF6);
  static const Color colorDelivered = Color(0xFF10B981);
  static const Color colorCancelled = Color(0xFFEF4444);

  // Status visual badge styling config
  static Color getStatusColor(String status) {
    switch (status) {
      case 'PENDIENTE': return colorPending;
      case 'CONFIRMADO': return colorConfirmed;
      case 'PREPARADO': return colorPrepared;
      case 'ENTREGADO': return colorDelivered;
      default: return colorCancelled;
    }
  }

  static ThemeData lightTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.light,
    colorScheme: const ColorScheme.light(
      primary: primary,
      secondary: secondary,
      background: background,
      surface: cardBg,
    ),
    fontFamily: 'Inter',
    appBarTheme: const AppBarTheme(
      backgroundColor: cardBg,
      foregroundColor: textDark,
      elevation: 0,
      centerTitle: true,
      titleTextStyle: TextStyle(
        fontFamily: 'Inter',
        fontSize: 18,
        fontWeight: FontWeight.bold,
        color: textDark,
      ),
    ),
  );
}

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:novadrogueria_app/core/theme/app_state.dart';
import 'package:novadrogueria_app/main.dart';

void main() {
  testWidgets('App smoke test - verifies rendering without crash', (WidgetTester tester) async {
    // Build our app wrapped in a provider to simulate main.dart startup environment
    await tester.pumpWidget(
      ChangeNotifierProvider<NovaAppState>(
        create: (_) => NovaAppState(),
        child: const NovaDrogueriaApp(),
      ),
    );
    expect(find.byType(NovaDrogueriaApp), findsOneWidget);
  });
}

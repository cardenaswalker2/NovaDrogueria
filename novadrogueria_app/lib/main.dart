import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/theme/app_state.dart';
import 'core/theme/nova_styles.dart';
import 'features/home/home_view.dart';
import 'features/catalog/catalog_view.dart';
import 'features/reservations/reservations_lookup_view.dart';
import 'features/information/information_view.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (_) => NovaAppState()..initApp(),
      child: const NovaDrogueriaApp(),
    ),
  );
}

class NovaDrogueriaApp extends StatelessWidget {
  const NovaDrogueriaApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Nova Droguería',
      debugShowCheckedModeBanner: false,
      theme: NovaStyles.lightTheme,
      home: const MainNavigationScreen(),
    );
  }
}

class MainNavigationScreen extends StatefulWidget {
  const MainNavigationScreen({Key? key}) : super(key: key);

  @override
  State<MainNavigationScreen> createState() => _MainNavigationScreenState();
}

class _MainNavigationScreenState extends State<MainNavigationScreen> {
  int _currentIndex = 0;

  final List<Widget> _screens = const [
    HomeView(),
    CatalogView(),
    ReservationsLookupView(),
    InformationView(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _currentIndex,
        children: _screens,
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (index) {
          setState(() {
            _currentIndex = index;
          });
        },
        type: BottomNavigationBarType.fixed,
        selectedItemColor: NovaStyles.primary,
        unselectedItemColor: NovaStyles.textMuted,
        showUnselectedLabels: true,
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.home_outlined),
            activeIcon: Icon(Icons.home),
            label: 'Inicio',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.search_outlined),
            activeIcon: Icon(Icons.search),
            label: 'Catálogo',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.assignment_outlined),
            activeIcon: Icon(Icons.assignment),
            label: 'Apartados',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.info_outline),
            activeIcon: Icon(Icons.info),
            label: 'Info',
          ),
        ],
      ),
    );
  }
}

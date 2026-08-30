import 'dart:async';
import 'package:flutter/material.dart';
import '../../core/network/api_provider.dart';
import '../../data/models/data_models.dart';

class NovaAppState extends ChangeNotifier {
  final NovaApiProvider api = NovaApiProvider();

  // Public parameters
  AppConfigModel? appConfig;
  List<CategoryModel> categories = [];
  List<ProductModel> featuredProducts = [];
  List<ProductModel> catalogProducts = [];
  List<ProductModel> searchSuggestions = [];
  
  bool isLoading = false;
  String? errorMessage;

  // Search input and autocomplete debounce timer
  Timer? _debounce;

  Future<void> initApp() async {
    isLoading = true;
    errorMessage = null;
    notifyListeners();
    try {
      appConfig = await api.fetchAppConfig();
      categories = await api.fetchCategories();
      featuredProducts = await api.fetchFeaturedProducts();
    } catch (e) {
      errorMessage = e.toString().replaceAll("Exception: ", "");
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  // Autocomplete Suggestions with Debounce
  void handleAutocomplete(String text) {
    if (_debounce?.isActive ?? false) _debounce!.cancel();
    if (text.trim().length < 2) {
      searchSuggestions = [];
      notifyListeners();
      return;
    }

    _debounce = Timer(const Duration(milliseconds: 300), () async {
      try {
        searchSuggestions = await api.autocompleteSuggestions(text);
        notifyListeners();
      } catch (_) {}
    });
  }

  // Public catalog queries
  Future<void> loadCatalog({String? categoryId, String? search, String sort = 'relevante'}) async {
    isLoading = true;
    errorMessage = null;
    notifyListeners();
    try {
      catalogProducts = await api.queryProducts(categoryId: categoryId, query: search, sortOption: sort);
    } catch (e) {
      errorMessage = e.toString().replaceAll("Exception: ", "");
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  // Reservation details loading
  Future<ReservationModel> executeReservation({
    required String name,
    required String phone,
    required String email,
    required String notes,
    required String productId,
    required int quantity,
  }) async {
    isLoading = true;
    notifyListeners();
    try {
      final res = await api.createReservation(
        name: name,
        phone: phone,
        email: email,
        notes: notes,
        productId: productId,
        quantity: quantity,
      );
      // reload featured/catalog stock updates
      initApp();
      return res;
    } catch (e) {
      throw Exception(e.toString().replaceAll("Exception: ", ""));
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  // Admin authentication state variables
  bool isAdminLogged = false;
  String? adminUsername;
  Map<String, int>? adminMetrics;
  List<ReservationModel> adminReservations = [];

  Future<void> performAdminLogin(String user, String pass) async {
    isLoading = true;
    errorMessage = null;
    notifyListeners();
    try {
      final ok = await api.adminLogin(user, pass);
      if (ok) {
        isAdminLogged = true;
        adminUsername = user;
        await loadAdminDashboard();
      }
    } catch (e) {
      errorMessage = e.toString().replaceAll("Exception: ", "");
      throw Exception(errorMessage);
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadAdminDashboard() async {
    try {
      adminMetrics = await api.fetchAdminMetrics();
      adminReservations = await api.fetchAdminReservations(sort: 'recent');
      notifyListeners();
    } catch (_) {}
  }

  Future<void> loadAdminReservations({String? search, String? status, String sort = 'recent'}) async {
    isLoading = true;
    notifyListeners();
    try {
      adminReservations = await api.fetchAdminReservations(search: search, status: status, sort: sort);
    } catch (e) {
      errorMessage = e.toString().replaceAll("Exception: ", "");
      if (errorMessage != null && (errorMessage!.contains("401") || errorMessage!.contains("Unauthorized") || errorMessage!.contains("403"))) {
        await logoutAdmin();
      }
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<void> updateReservationState(String id, String targetStatus, {String? notes}) async {
    isLoading = true;
    notifyListeners();
    try {
      await api.updateStatus(id, targetStatus, cancelNotes: notes);
      await loadAdminDashboard();
    } catch (e) {
      throw Exception(e.toString().replaceAll("Exception: ", ""));
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<void> logoutAdmin() async {
    isAdminLogged = false;
    adminUsername = null;
    adminMetrics = null;
    adminReservations = [];
    await api.clearAdminSession();
    notifyListeners();
  }
}

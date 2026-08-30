import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../../data/models/data_models.dart';

class NovaApiProvider {
  // Central API Configuration base URL. Fallback support for Android Emulator loopback.
  static const String devUrl = 'http://192.168.1.88:8080';
  static const String prodUrl = 'http://localhost:8080';

  String get baseUrl => devUrl;

  // 1. Get app configuration
  Future<AppConfigModel> fetchAppConfig() async {
    final res = await http.get(Uri.parse('$baseUrl/api/configuracion'));
    if (res.statusCode == 200) {
      return AppConfigModel.fromJson(jsonDecode(utf8.decode(res.bodyBytes)));
    }
    throw Exception('Error al conectar con el servidor.');
  }

  // 2. Get active categories
  Future<List<CategoryModel>> fetchCategories() async {
    final res = await http.get(Uri.parse('$baseUrl/api/categorias'));
    if (res.statusCode == 200) {
      final List raw = jsonDecode(utf8.decode(res.bodyBytes));
      return raw.map((c) => CategoryModel.fromJson(c)).toList();
    }
    throw Exception('Error al cargar categorías.');
  }

  // 3. Get featured products
  Future<List<ProductModel>> fetchFeaturedProducts() async {
    final res = await http.get(Uri.parse('$baseUrl/api/productos/destacados'));
    if (res.statusCode == 200) {
      final List raw = jsonDecode(utf8.decode(res.bodyBytes));
      return raw.map((p) => ProductModel.fromJson(p)).toList();
    }
    throw Exception('Error al cargar productos destacados.');
  }

  // 4. Get paginated products search suggestions/catalog
  Future<List<ProductModel>> queryProducts({
    String? categoryId,
    String? query,
    String sortOption = 'relevante',
    int page = 0,
  }) async {
    final uri = Uri.parse('$baseUrl/api/productos').replace(queryParameters: {
      if (categoryId != null && categoryId.isNotEmpty) 'categoria': categoryId,
      if (query != null && query.isNotEmpty) 'buscar': query,
      'orden': sortOption,
      'page': page.toString(),
      'size': '12',
    });

    final res = await http.get(uri);
    if (res.statusCode == 200) {
      final map = jsonDecode(utf8.decode(res.bodyBytes));
      final List content = map['content'] ?? [];
      return content.map((p) => ProductModel.fromJson(p)).toList();
    }
    throw Exception('Error al consultar productos.');
  }

  // 5. Search suggestions autocomplete endpoint
  Future<List<ProductModel>> autocompleteSuggestions(String query) async {
    final res = await http.get(Uri.parse('$baseUrl/api/productos/buscar?q=${Uri.encodeComponent(query)}'));
    if (res.statusCode == 200) {
      final List raw = jsonDecode(utf8.decode(res.bodyBytes));
      return raw.map((p) => ProductModel.fromJson(p)).toList();
    }
    return [];
  }

  // 6. Submit reservation
  Future<ReservationModel> createReservation({
    required String name,
    required String phone,
    required String email,
    required String notes,
    required String productId,
    required int quantity,
  }) async {
    final res = await http.post(
      Uri.parse('$baseUrl/api/apartados/crear'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'customerName': name,
        'customerPhone': phone,
        'customerEmail': email,
        'notes': notes,
        'productId': productId,
        'quantity': quantity,
      }),
    );

    final payload = jsonDecode(utf8.decode(res.bodyBytes));
    if (res.statusCode == 200) {
      final reservation = ReservationModel.fromJson(payload);
      // Cache reservation code locally
      await _cacheLocalReservation(reservation.code);
      return reservation;
    }
    throw Exception(payload['error'] ?? 'No se pudo completar el apartado.');
  }

  // 7. Search reservations by phone or code + phone
  Future<List<ReservationModel>> findReservations({String? code, required String phone}) async {
    final uri = Uri.parse('$baseUrl/api/apartados/buscar').replace(queryParameters: {
      if (code != null && code.isNotEmpty) 'code': code,
      'phone': phone,
    });

    final res = await http.get(uri);
    final decoded = jsonDecode(utf8.decode(res.bodyBytes));
    if (res.statusCode == 200) {
      final List raw = decoded is List ? decoded : [decoded];
      return raw.map((r) => ReservationModel.fromJson(r)).toList();
    }
    throw Exception(decoded['error'] ?? 'No se encontraron apartados.');
  }

  // 8. Admin authentication
  Future<bool> adminLogin(String username, String password) async {
    final res = await http.post(
      Uri.parse('$baseUrl/api/admin/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'username': username, 'password': password}),
    );

    if (res.statusCode == 200) {
      final pref = await SharedPreferences.getInstance();
      await pref.setString('admin_username', username);
      await pref.setString('admin_password', password);
      await pref.setBool('is_admin_logged', true);
      return true;
    }
    final errorDecoded = jsonDecode(utf8.decode(res.bodyBytes));
    throw Exception(errorDecoded['error'] ?? 'Credenciales inválidas.');
  }

  // Helper auth headers
  Future<Map<String, String>> _getAuthHeaders() async {
    final pref = await SharedPreferences.getInstance();
    final user = pref.getString('admin_username') ?? '';
    final pass = pref.getString('admin_password') ?? '';
    final String basicAuth = 'Basic ' + base64Encode(utf8.encode('$user:$pass'));
    return {
      'Authorization': basicAuth,
      'Content-Type': 'application/json',
    };
  }

  // 9. Admin dashboard metrics
  Future<Map<String, int>> fetchAdminMetrics() async {
    final headers = await _getAuthHeaders();
    final res = await http.get(Uri.parse('$baseUrl/api/admin/dashboard'), headers: headers);
    if (res.statusCode == 200) {
      final Map<String, dynamic> raw = jsonDecode(res.body);
      return raw.map((key, value) => MapEntry(key, value as int));
    }
    throw Exception('Error al cargar métricas del dashboard.');
  }

  // 10. Admin reservations query listing
  Future<List<ReservationModel>> fetchAdminReservations({
    String? search,
    String? status,
    String sort = 'recent',
    int page = 0,
  }) async {
    final uri = Uri.parse('$baseUrl/api/admin/apartados').replace(queryParameters: {
      if (search != null && search.isNotEmpty) 'search': search,
      if (status != null && status.isNotEmpty) 'status': status,
      'sort': sort,
      'page': page.toString(),
      'size': '15',
    });

    final headers = await _getAuthHeaders();
    final res = await http.get(uri, headers: headers);
    if (res.statusCode == 200) {
      final map = jsonDecode(utf8.decode(res.bodyBytes));
      final List content = map['content'] ?? [];
      return content.map((r) => ReservationModel.fromJson(r)).toList();
    }
    throw Exception('Error al consultar lista administrativa.');
  }

  // 11. Admin transition status change REST request
  Future<ReservationModel> updateStatus(String id, String status, {String? cancelNotes}) async {
    final uri = Uri.parse('$baseUrl/api/admin/apartados/estado/$id').replace(queryParameters: {
      'status': status,
      if (cancelNotes != null) 'cancelNotes': cancelNotes,
    });

    final headers = await _getAuthHeaders();
    final res = await http.post(uri, headers: headers);
    final payload = jsonDecode(utf8.decode(res.bodyBytes));
    if (res.statusCode == 200) {
      return ReservationModel.fromJson(payload);
    }
    throw Exception(payload['error'] ?? 'Error al actualizar estado.');
  }

  // Helper local caching reservations code
  Future<void> _cacheLocalReservation(String code) async {
    final pref = await SharedPreferences.getInstance();
    final List<String> list = pref.getStringList('my_reservations_codes') ?? [];
    if (!list.contains(code)) {
      list.add(code);
      await pref.setStringList('my_reservations_codes', list);
    }
  }

  Future<List<String>> getLocalCachedReservations() async {
    final pref = await SharedPreferences.getInstance();
    return pref.getStringList('my_reservations_codes') ?? [];
  }

  Future<void> clearAdminSession() async {
    final pref = await SharedPreferences.getInstance();
    await pref.remove('admin_username');
    await pref.remove('admin_password');
    await pref.remove('is_admin_logged');
  }
}

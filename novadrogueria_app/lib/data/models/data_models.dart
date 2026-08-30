class AppConfigModel {
  final String id;
  final String storeName;
  final String description;
  final String address;
  final String phone;
  final String whatsappNumber;
  final String schedule;
  final String welcomeMessage;

  AppConfigModel({
    required this.id,
    required this.storeName,
    required this.description,
    required this.address,
    required this.phone,
    required this.whatsappNumber,
    required this.schedule,
    required this.welcomeMessage,
  });

  factory AppConfigModel.fromJson(Map<String, dynamic> json) {
    return AppConfigModel(
      id: json['id'] ?? 'singleton',
      storeName: json['storeName'] ?? 'Nova Droguería',
      description: json['description'] ?? '',
      address: json['address'] ?? '',
      phone: json['phone'] ?? '',
      whatsappNumber: json['whatsappNumber'] ?? '',
      schedule: json['schedule'] ?? '',
      welcomeMessage: json['welcomeMessage'] ?? '',
    );
  }
}

class CategoryModel {
  final String id;
  final String name;
  final String slug;
  final String? description;
  final bool active;

  CategoryModel({
    required this.id,
    required this.name,
    required this.slug,
    this.description,
    required this.active,
  });

  factory CategoryModel.fromJson(Map<String, dynamic> json) {
    return CategoryModel(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      slug: json['slug'] ?? '',
      description: json['description'],
      active: json['active'] ?? true,
    );
  }
}

class ProductModel {
  final String id;
  final String name;
  final String slug;
  final String categoryId;
  final String brand;
  final String activeIngredient;
  final String presentation;
  final String description;
  final double price;
  final int stock;
  final String imageUrl;
  final bool featured;
  final bool active;
  final String? additionalInfo;
  final String? warnings;

  ProductModel({
    required this.id,
    required this.name,
    required this.slug,
    required this.categoryId,
    required this.brand,
    required this.activeIngredient,
    required this.presentation,
    required this.description,
    required this.price,
    required this.stock,
    required this.imageUrl,
    required this.featured,
    required this.active,
    this.additionalInfo,
    this.warnings,
  });

  factory ProductModel.fromJson(Map<String, dynamic> json) {
    return ProductModel(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      slug: json['slug'] ?? '',
      categoryId: json['categoryId'] ?? '',
      brand: json['brand'] ?? '',
      activeIngredient: json['activeIngredient'] ?? '',
      presentation: json['presentation'] ?? '',
      description: json['description'] ?? '',
      price: (json['price'] as num?)?.toDouble() ?? 0.0,
      stock: (json['stock'] as num?)?.toInt() ?? 0,
      imageUrl: json['imageUrl'] ?? 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?q=80&w=200&auto=format&fit=crop',
      featured: json['featured'] ?? false,
      active: json['active'] ?? true,
      additionalInfo: json['additionalInfo'],
      warnings: json['warnings'],
    );
  }
}

class ReservationItemModel {
  final String productId;
  final String productName;
  final double unitPrice;
  final int quantity;
  final double lineTotal;

  ReservationItemModel({
    required this.productId,
    required this.productName,
    required this.unitPrice,
    required this.quantity,
    required this.lineTotal,
  });

  factory ReservationItemModel.fromJson(Map<String, dynamic> json) {
    return ReservationItemModel(
      productId: json['productId'] ?? '',
      productName: json['productName'] ?? '',
      unitPrice: (json['unitPrice'] as num?)?.toDouble() ?? 0.0,
      quantity: (json['quantity'] as num?)?.toInt() ?? 0,
      lineTotal: (json['lineTotal'] as num?)?.toDouble() ?? 0.0,
    );
  }
}

class ReservationStatusHistoryModel {
  final String status;
  final String displayName;
  final String timestamp;
  final String notes;

  ReservationStatusHistoryModel({
    required this.status,
    required this.displayName,
    required this.timestamp,
    required this.notes,
  });

  factory ReservationStatusHistoryModel.fromJson(Map<String, dynamic> json) {
    return ReservationStatusHistoryModel(
      status: json['status'] ?? '',
      displayName: json['displayName'] ?? '',
      timestamp: json['timestamp'] ?? '',
      notes: json['notes'] ?? '',
    );
  }
}

class ReservationModel {
  final String id;
  final String code;
  final String customerName;
  final String customerPhone;
  final String customerEmail;
  final String notes;
  final double total;
  final String status;
  final String statusDisplayName;
  final String statusEmoji;
  final String createdAtStr;
  final List<ReservationItemModel> items;
  final List<ReservationStatusHistoryModel> history;

  ReservationModel({
    required this.id,
    required this.code,
    required this.customerName,
    required this.customerPhone,
    required this.customerEmail,
    required this.notes,
    required this.total,
    required this.status,
    required this.statusDisplayName,
    required this.statusEmoji,
    required this.createdAtStr,
    required this.items,
    required this.history,
  });

  factory ReservationModel.fromJson(Map<String, dynamic> json) {
    var rawItems = json['items'] as List? ?? [];
    List<ReservationItemModel> mappedItems = rawItems.map((i) => ReservationItemModel.fromJson(i)).toList();

    var rawHistory = json['history'] as List? ?? [];
    List<ReservationStatusHistoryModel> mappedHistory = rawHistory.map((h) => ReservationStatusHistoryModel.fromJson(h)).toList();

    return ReservationModel(
      id: json['id'] ?? '',
      code: json['code'] ?? '',
      customerName: json['customerName'] ?? '',
      customerPhone: json['customerPhone'] ?? '',
      customerEmail: json['customerEmail'] ?? '',
      notes: json['notes'] ?? '',
      total: (json['total'] as num?)?.toDouble() ?? 0.0,
      status: json['status'] ?? '',
      statusDisplayName: json['statusDisplayName'] ?? '',
      statusEmoji: json['statusEmoji'] ?? '📋',
      createdAtStr: json['createdAtStr'] ?? '',
      items: mappedItems,
      history: mappedHistory,
    );
  }
}

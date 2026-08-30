import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_state.dart';
import '../../core/theme/nova_styles.dart';
import '../../core/utils/currency_formatter.dart';
import '../../data/models/data_models.dart';
import '../../shared/widgets/shared_widgets.dart';
import '../reservations/reservation_create_view.dart';

class ProductDetailsView extends StatelessWidget {
  final ProductModel product;

  const ProductDetailsView({Key? key, required this.product}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text('Detalle de Producto'),
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Image header
            Image.network(
              product.imageUrl.isNotEmpty ? product.imageUrl : fallbackImage,
              width: double.infinity,
              height: 280,
              fit: BoxFit.cover,
              errorBuilder: (context, error, stackTrace) => Image.network(fallbackImage, width: double.infinity, height: 280, fit: BoxFit.cover),
            ),
            
            Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Brand label
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: const Color(0xFFE0F2FE),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      product.brand.isNotEmpty ? product.brand : 'Marca general',
                      style: const TextStyle(color: Color(0xFF0369A1), fontSize: 12, fontWeight: FontWeight.bold),
                    ),
                  ),
                  const SizedBox(height: 12),
                  
                  // Product name
                  Text(
                    product.name,
                    style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w800, color: NovaStyles.textDark),
                  ),
                  const SizedBox(height: 4),
                  
                  // Presentation & Ingredient
                  Text(
                    'Presentación: ${product.presentation}',
                    style: const TextStyle(fontSize: 14, color: NovaStyles.textMuted),
                  ),
                  if (product.activeIngredient.isNotEmpty) ...[
                    const SizedBox(height: 4),
                    Text(
                      'Principio Activo: ${product.activeIngredient}',
                      style: const TextStyle(fontSize: 14, color: NovaStyles.textMuted, fontWeight: FontWeight.w500),
                    ),
                  ],
                  
                  const Divider(height: 32, color: Color(0xFFF1F5F9)),

                  // Price card visual format representation
                  const Text('Precio de venta en local:', style: TextStyle(fontSize: 12, color: NovaStyles.textMuted)),
                  const SizedBox(height: 4),
                  Text(
                    ColombianCurrencyFormatter.format(product.price),
                    style: const TextStyle(fontSize: 28, fontWeight: FontWeight.w900, color: NovaStyles.primary),
                  ),
                  Text(
                    ColombianCurrencyFormatter.formatToWords(product.price),
                    style: const TextStyle(fontSize: 12, color: NovaStyles.textMuted, fontStyle: FontStyle.italic),
                  ),

                  const SizedBox(height: 16),
                  
                  // Stock badges
                  Row(
                    children: [
                      Icon(
                        Icons.circle,
                        size: 12,
                        color: product.stock > 5
                            ? Colors.green
                            : (product.stock > 0 ? Colors.amber : Colors.red),
                      ),
                      const SizedBox(width: 6),
                      Text(
                        product.stock > 5
                            ? 'Disponible'
                            : (product.stock > 0 ? 'Poco stock (${product.stock} un.)' : 'Agotado'),
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                    ],
                  ),

                  const Divider(height: 32, color: Color(0xFFF1F5F9)),

                  // Description
                  const Text('Descripción', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: NovaStyles.textDark)),
                  const SizedBox(height: 6),
                  Text(
                    product.description.isNotEmpty ? product.description : 'Sin descripción comercial.',
                    style: const TextStyle(fontSize: 14, color: Color(0xFF475569), height: 1.5),
                  ),

                  if (product.warnings != null && product.warnings!.isNotEmpty) ...[
                    const SizedBox(height: 20),
                    const Text('Contraindicaciones y Advertencias', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15, color: Color(0xFF991B1B))),
                    const SizedBox(height: 6),
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: const Color(0xFFFFF1F2),
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: const Color(0xFFFECDD3)),
                      ),
                      child: Text(
                        product.warnings!,
                        style: const TextStyle(fontSize: 13, color: Color(0xFF991B1B), height: 1.4),
                      ),
                    ),
                  ],

                  const SizedBox(height: 32),

                  // Submit button wrapper
                  NovaPrimaryButton(
                    label: 'Apartar producto',
                    icon: Icons.bookmark_add_outlined,
                    onPressed: product.stock > 0
                        ? () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => ReservationCreateView(product: product),
                              ),
                            );
                          }
                        : null,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

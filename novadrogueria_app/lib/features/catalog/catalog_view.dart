import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_state.dart';
import '../../core/theme/nova_styles.dart';
import '../../core/utils/currency_formatter.dart';
import '../../data/models/data_models.dart';
import '../../shared/widgets/shared_widgets.dart';
import '../products/product_details_view.dart';

class CatalogView extends StatefulWidget {
  const CatalogView({Key? key}) : super(key: key);

  @override
  State<CatalogView> createState() => _CatalogViewState();
}

class _CatalogViewState extends State<CatalogView> {
  String? selectedCategoryId;
  String selectedSort = 'relevante';
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<NovaAppState>().loadCatalog();
    });
  }

  void _triggerSearch() {
    context.read<NovaAppState>().loadCatalog(
      categoryId: selectedCategoryId,
      search: _searchController.text,
      sort: selectedSort,
    );
  }

  @override
  Widget build(BuildContext context) {
    final state = context.watch<NovaAppState>();
    final list = state.catalogProducts;

    return Scaffold(
      backgroundColor: NovaStyles.background,
      appBar: AppBar(
        title: const Text('Catálogo de Productos'),
      ),
      body: Column(
        children: [
          // Filter toolbar
          Container(
            color: Colors.white,
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
            child: Column(
              children: [
                // Search bar
                TextField(
                  controller: _searchController,
                  decoration: InputDecoration(
                    hintText: 'Buscar en catálogo...',
                    prefixIcon: const Icon(Icons.search, color: NovaStyles.primary),
                    suffixIcon: IconButton(
                      icon: const Icon(Icons.send, color: NovaStyles.primary),
                      onPressed: _triggerSearch,
                    ),
                    filled: true,
                    fillColor: const Color(0xFFF1F5F9),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(vertical: 0),
                  ),
                  onSubmitted: (_) => _triggerSearch(),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    // Category selector button
                    Expanded(
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8),
                        decoration: BoxDecoration(
                          color: const Color(0xFFF1F5F9),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: DropdownButton<String>(
                          value: selectedCategoryId,
                          hint: const Text('Categoría', style: TextStyle(fontSize: 13)),
                          isExpanded: true,
                          underline: const SizedBox(),
                          items: [
                            const DropdownMenuItem(value: null, child: Text('Todas', style: TextStyle(fontSize: 13))),
                            ...state.categories.map((c) => DropdownMenuItem(
                                  value: c.id,
                                  child: Text(c.name, style: const TextStyle(fontSize: 13)),
                                )),
                          ],
                          onChanged: (val) {
                            setState(() {
                              selectedCategoryId = val;
                            });
                            _triggerSearch();
                          },
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    // Sorting selector button
                    Expanded(
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8),
                        decoration: BoxDecoration(
                          color: const Color(0xFFF1F5F9),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: DropdownButton<String>(
                          value: selectedSort,
                          isExpanded: true,
                          underline: const SizedBox(),
                          items: const [
                            DropdownMenuItem(value: 'relevante', child: Text('Más relevante', style: TextStyle(fontSize: 13))),
                            DropdownMenuItem(value: 'precio_menor', child: Text('Menor precio', style: TextStyle(fontSize: 13))),
                            DropdownMenuItem(value: 'precio_mayor', child: Text('Mayor precio', style: TextStyle(fontSize: 13))),
                            DropdownMenuItem(value: 'mas_recientes', child: Text('Más recientes', style: TextStyle(fontSize: 13))),
                          ],
                          onChanged: (val) {
                            if (val != null) {
                              setState(() {
                                selectedSort = val;
                              });
                              _triggerSearch();
                            }
                          },
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // Catalog Products List
          Expanded(
            child: state.isLoading
                ? const Center(child: CircularProgressIndicator())
                : list.isEmpty
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Icon(Icons.inventory_2_outlined, size: 64, color: NovaStyles.textMuted),
                            const SizedBox(height: 16),
                            const Text('No se encontraron productos.', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                            const SizedBox(height: 8),
                            TextButton(
                              onPressed: () {
                                _searchController.clear();
                                setState(() {
                                  selectedCategoryId = null;
                                  selectedSort = 'relevante';
                                });
                                _triggerSearch();
                              },
                              child: const Text('Limpiar filtros'),
                            ),
                          ],
                        ),
                      )
                    : GridView.builder(
                        padding: const EdgeInsets.all(16),
                        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 2,
                          crossAxisSpacing: 12,
                          mainAxisSpacing: 12,
                          childAspectRatio: 0.72,
                        ),
                        itemCount: list.length,
                        itemBuilder: (context, index) {
                          final p = list[index];
                          return GestureDetector(
                            onTap: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(builder: (context) => ProductDetailsView(product: p)),
                              );
                            },
                            child: Card(
                              color: Colors.white,
                              elevation: 0,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12),
                                side: const BorderSide(color: Color(0xFFE2E8F0)),
                              ),
                              child: Padding(
                                padding: const EdgeInsets.all(12.0),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Expanded(
                                      child: Container(
                                        decoration: BoxDecoration(
                                          borderRadius: BorderRadius.circular(8),
                                          image: DecorationImage(
                                            image: NetworkImage(p.imageUrl.isNotEmpty ? p.imageUrl : fallbackImage),
                                            fit: BoxFit.cover,
                                          ),
                                        ),
                                      ),
                                    ),
                                    const SizedBox(height: 8),
                                    Text(
                                      p.name,
                                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: NovaStyles.textDark),
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                    Text(
                                      p.presentation,
                                      style: const TextStyle(fontSize: 11, color: NovaStyles.textMuted),
                                    ),
                                    const SizedBox(height: 6),
                                    Text(
                                      ColombianCurrencyFormatter.format(p.price),
                                      style: const TextStyle(fontWeight: FontWeight.w800, color: NovaStyles.primary, fontSize: 14),
                                    ),
                                    const SizedBox(height: 4),
                                    Text(
                                      p.stock > 0 ? '🟢 Disponible' : '🔴 Agotado',
                                      style: TextStyle(
                                        fontSize: 10,
                                        fontWeight: FontWeight.bold,
                                        color: p.stock > 0 ? Colors.green : Colors.red,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          );
                        },
                      ),
          ),
        ],
      ),
    );
  }
}

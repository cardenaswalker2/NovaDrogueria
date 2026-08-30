import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_state.dart';
import '../../core/theme/nova_styles.dart';
import '../../core/utils/currency_formatter.dart';
import '../../data/models/data_models.dart';
import '../../shared/widgets/shared_widgets.dart';
import '../products/product_details_view.dart';
import 'package:url_launcher/url_launcher.dart';

class HomeView extends StatefulWidget {
  const HomeView({Key? key}) : super(key: key);

  @override
  State<HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> {
  final TextEditingController _searchController = TextEditingController();
  final FocusNode _searchFocusNode = FocusNode();

  @override
  void dispose() {
    _searchController.dispose();
    _searchFocusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = context.watch<NovaAppState>();
    final featured = state.featuredProducts;
    final config = state.appConfig;

    return GestureDetector(
      onTap: () {
        _searchFocusNode.unfocus();
        state.handleAutocomplete('');
      },
      child: Scaffold(
        backgroundColor: NovaStyles.background,
        appBar: AppBar(
          title: Text(config?.storeName ?? 'Nova Droguería'),
          elevation: 0,
          scrolledUnderElevation: 0,
        ),
        body: RefreshIndicator(
          onRefresh: () => state.initApp(),
          color: NovaStyles.primary,
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Medical Banner Header
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [NovaStyles.primary, NovaStyles.primaryDark],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Nova Droguería',
                        style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        config?.description ?? 'Tu salud, más cerca.',
                        style: TextStyle(color: Colors.teal[50], fontSize: 14),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 20),

                // Search Input Title
                const Padding(
                  padding: EdgeInsets.only(left: 4.0, bottom: 8.0),
                  child: Text(
                    '🔎 ¿Qué medicamento estás buscando?',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: NovaStyles.textDark),
                  ),
                ),

                // Search Box container
                Card(
                  elevation: 0,
                  margin: EdgeInsets.zero,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                    side: const BorderSide(color: Color(0xFFE2E8F0)),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 4.0),
                    child: TextField(
                      controller: _searchController,
                      focusNode: _searchFocusNode,
                      decoration: InputDecoration(
                        hintText: 'Nombre, marca o principio activo...',
                        border: InputBorder.none,
                        suffixIcon: _searchController.text.isNotEmpty
                            ? IconButton(
                                icon: const Icon(Icons.clear, size: 20),
                                onPressed: () {
                                  _searchController.clear();
                                  state.handleAutocomplete('');
                                },
                              )
                            : null,
                      ),
                      onChanged: (val) {
                        state.handleAutocomplete(val);
                        setState(() {});
                      },
                    ),
                  ),
                ),

                // Instant overlay search suggestions results inside the layout screen
                if (_searchController.text.trim().length >= 2) ...[
                  const SizedBox(height: 12),
                  const Padding(
                    padding: EdgeInsets.only(left: 4.0, bottom: 8.0),
                    child: Text(
                      'Resultados sugeridos',
                      style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: NovaStyles.textMuted),
                    ),
                  ),
                  if (state.searchSuggestions.isEmpty)
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(24),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(color: const Color(0xFFE2E8F0)),
                      ),
                      child: Column(
                        children: [
                          const Icon(Icons.search_off, size: 48, color: NovaStyles.textMuted),
                          const SizedBox(height: 12),
                          const Text(
                            'No encontramos ese producto',
                            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: NovaStyles.textDark),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            'No encontramos coincidencias para "${_searchController.text}".',
                            style: const TextStyle(fontSize: 13, color: NovaStyles.textMuted),
                            textAlign: TextAlign.center,
                          ),
                          const SizedBox(height: 4),
                          const Text(
                            'Prueba con otro nombre, marca o principio activo.',
                            style: TextStyle(fontSize: 12, color: NovaStyles.textMuted),
                            textAlign: TextAlign.center,
                          ),
                        ],
                      ),
                    )
                  else
                    Container(
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(color: const Color(0xFFE2E8F0)),
                      ),
                      child: ListView.separated(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        itemCount: state.searchSuggestions.length,
                        separatorBuilder: (context, index) => const Divider(height: 1, color: Color(0xFFF1F5F9)),
                        itemBuilder: (context, index) {
                          final item = state.searchSuggestions[index];
                          return ListTile(
                            leading: Container(
                              width: 48,
                              height: 48,
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(8),
                                image: DecorationImage(
                                  image: NetworkImage(item.imageUrl.isNotEmpty ? item.imageUrl : fallbackImage),
                                  fit: BoxFit.cover,
                                ),
                              ),
                            ),
                            title: Text(
                              item.name,
                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: NovaStyles.textDark),
                            ),
                            subtitle: Text('${item.brand} • ${item.presentation}'),
                            trailing: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              crossAxisAlignment: CrossAxisAlignment.end,
                              children: [
                                Text(
                                  ColombianCurrencyFormatter.format(item.price),
                                  style: const TextStyle(fontWeight: FontWeight.bold, color: NovaStyles.primary, fontSize: 14),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  item.stock > 0 ? '🟢 Disponible' : '🔴 Agotado',
                                  style: TextStyle(
                                    fontSize: 10,
                                    fontWeight: FontWeight.bold,
                                    color: item.stock > 0 ? Colors.green : Colors.red,
                                  ),
                                ),
                              ],
                            ),
                            onTap: () {
                              _searchFocusNode.unfocus();
                              state.handleAutocomplete('');
                              _searchController.clear();
                              Navigator.push(
                                context,
                                MaterialPageRoute(builder: (context) => ProductDetailsView(product: item)),
                              );
                            },
                          );
                        },
                      ),
                    ),
                ],

                const SizedBox(height: 24),

                // Horizontal scroll categories
                const Text(
                  'Categorías',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: NovaStyles.textDark),
                ),
                const SizedBox(height: 12),
                SizedBox(
                  height: 48,
                  child: ListView.builder(
                    scrollDirection: Axis.horizontal,
                    itemCount: state.categories.length,
                    itemBuilder: (context, index) {
                      final cat = state.categories[index];
                      return Container(
                        margin: const EdgeInsets.only(right: 8),
                        child: ActionChip(
                          elevation: 0,
                          pressElevation: 0,
                          label: Text(cat.name),
                          backgroundColor: Colors.white,
                          labelStyle: const TextStyle(color: NovaStyles.primary, fontWeight: FontWeight.bold, fontSize: 13),
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(20),
                            side: const BorderSide(color: Color(0xFFE2E8F0)),
                          ),
                          onPressed: () {
                            // trigger navigation catalog with selected index category parameter
                            // catalog is route index 1
                          },
                        ),
                      );
                    },
                  ),
                ),

                const SizedBox(height: 24),

                // Featured products lists grid
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Productos destacados',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: NovaStyles.textDark),
                    ),
                    TextButton(
                      onPressed: () {
                        // Change tab state index directly
                      },
                      child: const Text('Ver todos', style: TextStyle(color: NovaStyles.primary, fontWeight: FontWeight.bold)),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                state.isLoading
                    ? const Center(child: CircularProgressIndicator())
                    : GridView.builder(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 2,
                          crossAxisSpacing: 12,
                          mainAxisSpacing: 12,
                          childAspectRatio: 0.72,
                        ),
                        itemCount: featured.length > 4 ? 4 : featured.length,
                        itemBuilder: (context, index) {
                          final p = featured[index];
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
                              margin: EdgeInsets.zero,
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
                                    Row(
                                      children: [
                                        Icon(
                                          Icons.circle,
                                          size: 8,
                                          color: p.stock > 0 ? Colors.green : Colors.red,
                                        ),
                                        const SizedBox(width: 4),
                                        Text(
                                          p.stock > 0 ? 'Disponible' : 'Agotado',
                                          style: TextStyle(
                                            fontSize: 10,
                                            fontWeight: FontWeight.bold,
                                            color: p.stock > 0 ? Colors.green : Colors.red,
                                          ),
                                        ),
                                      ],
                                    ),
                                    const Spacer(),
                                    Row(
                                      mainAxisAlignment: MainAxisAlignment.end,
                                      children: const [
                                        Text(
                                          'Ver producto',
                                          style: TextStyle(fontSize: 11, color: NovaStyles.primary, fontWeight: FontWeight.bold),
                                        ),
                                        Icon(Icons.arrow_forward, size: 12, color: NovaStyles.primary),
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          );
                        },
                      ),

                const SizedBox(height: 24),

                // WhatsApp callout help card
                Card(
                  color: const Color(0xFFE8FDF0),
                  elevation: 0,
                  margin: EdgeInsets.zero,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Row(
                      children: [
                        const Icon(Icons.chat, color: Color(0xFF25D366), size: 32),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: const [
                              Text('¿Necesitas ayuda?', style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF14532D))),
                              Text('Escríbenos por WhatsApp para consultas rápidas.', style: TextStyle(fontSize: 12, color: Color(0xFF166534))),
                            ],
                          ),
                        ),
                        IconButton(
                          icon: const Icon(Icons.arrow_forward_ios, size: 16, color: Color(0xFF14532D)),
                          onPressed: () async {
                            final number = config?.whatsappNumber ?? '573005722844';
                            // official formats +57 prefix logic
                            final normalized = number.startsWith('+') ? number : '+$number';
                            final url = Uri.parse('https://wa.me/${normalized.replaceAll(RegExp(r'[^0-9]'), '')}');
                            if (await canLaunchUrl(url)) {
                              await launchUrl(url, mode: LaunchMode.externalApplication);
                            }
                          },
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

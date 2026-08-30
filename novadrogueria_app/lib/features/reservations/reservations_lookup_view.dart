import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_state.dart';
import '../../core/theme/nova_styles.dart';
import '../../core/utils/currency_formatter.dart';
import '../../data/models/data_models.dart';
import '../../shared/widgets/shared_widgets.dart';
import 'reservation_detail_view.dart';

class ReservationsLookupView extends StatefulWidget {
  const ReservationsLookupView({Key? key}) : super(key: key);

  @override
  State<ReservationsLookupView> createState() => _ReservationsLookupViewState();
}

class _ReservationsLookupViewState extends State<ReservationsLookupView> {
  final _formKey = GlobalKey<FormState>();
  final _codeController = TextEditingController();
  final _phoneController = TextEditingController();

  bool searchByPhoneOnly = false;
  bool isSearching = false;
  List<ReservationModel> results = [];
  bool hasSearched = false;

  @override
  void dispose() {
    _codeController.dispose();
    _phoneController.dispose();
    super.dispose();
  }

  void _search() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      isSearching = true;
      hasSearched = false;
      results = [];
    });

    try {
      final state = context.read<NovaAppState>();
      final list = await state.api.findReservations(
        code: searchByPhoneOnly ? null : _codeController.text.trim(),
        phone: _phoneController.text.trim(),
      );

      setState(() {
        results = list;
        hasSearched = true;
      });

      // If exactly 1 reservation found, open detail screen directly
      if (list.length == 1 && mounted) {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => ReservationDetailView(reservation: list.first)),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: ${e.toString().replaceAll("Exception: ", "")}')),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          isSearching = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: NovaStyles.background,
      appBar: AppBar(
        title: const Text('Consultar Apartados'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Tabs toggle layout switcher
            Row(
              children: [
                Expanded(
                  child: ChoiceChip(
                    label: const Center(child: Text('Tengo mi código')),
                    selected: !searchByPhoneOnly,
                    onSelected: (val) {
                      setState(() {
                        searchByPhoneOnly = false;
                        results = [];
                        hasSearched = false;
                      });
                    },
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: ChoiceChip(
                    label: const Center(child: Text('Buscar por celular')),
                    selected: searchByPhoneOnly,
                    onSelected: (val) {
                      setState(() {
                        searchByPhoneOnly = true;
                        results = [];
                        hasSearched = false;
                      });
                    },
                  ),
                ),
              ],
            ),
            
            const SizedBox(height: 24),

            // Main Forms block
            Card(
              color: Colors.white,
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
                side: const BorderSide(color: Color(0xFFE2E8F0)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Form(
                  key: _formKey,
                  child: Column(
                    children: [
                      if (!searchByPhoneOnly) ...[
                        TextFormField(
                          controller: _codeController,
                          textCapitalization: TextCapitalization.characters,
                          decoration: const InputDecoration(
                            labelText: 'Código de apartado *',
                            hintText: 'Ej. NOVA-FJF3EW',
                            border: OutlineInputBorder(),
                          ),
                          validator: (val) {
                            if (val == null || val.trim().isEmpty) return 'Ingresa el código.';
                            if (!RegExp(r'^NOVA-[A-Z0-9]{6}$').hasMatch(val.trim().toUpperCase())) {
                              return 'Formato debe ser NOVA-XXXXXX.';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 16),
                      ],
                      TextFormField(
                        controller: _phoneController,
                        keyboardType: TextInputType.phone,
                        decoration: const InputDecoration(
                          labelText: 'Número de celular *',
                          hintText: 'Ej. 3005722844',
                          border: OutlineInputBorder(),
                        ),
                        validator: (val) {
                          if (val == null || val.trim().isEmpty) return 'Ingresa tu celular.';
                          return null;
                        },
                      ),
                      const SizedBox(height: 20),
                      NovaPrimaryButton(
                        label: searchByPhoneOnly ? 'Buscar mis apartados' : 'Consultar apartado',
                        isLoading: isSearching,
                        onPressed: _search,
                      ),
                    ],
                  ),
                ),
              ),
            ),

            const SizedBox(height: 24),

            // Search outcomes listing
            if (hasSearched && results.isEmpty)
              Center(
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 32.0),
                  child: Column(
                    children: const [
                      Icon(Icons.search_off, size: 48, color: NovaStyles.textMuted),
                      SizedBox(height: 12),
                      Text('No encontramos apartados asociados.', style: TextStyle(fontWeight: FontWeight.bold)),
                      SizedBox(height: 4),
                      Text('Verifica que el número coincida con el registro.', style: TextStyle(color: NovaStyles.textMuted), textAlign: TextAlign.center),
                    ],
                  ),
                ),
              ),

            if (results.isNotEmpty) ...[
              Text(
                'Encontramos ${results.length} apartados:',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
              ),
              const SizedBox(height: 12),
              ListView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: results.length,
                itemBuilder: (context, index) {
                  final res = results[index];
                  return Container(
                    margin: const EdgeInsets.only(bottom: 12),
                    child: Card(
                      color: Colors.white,
                      elevation: 0,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                        side: const BorderSide(color: Color(0xFFE2E8F0)),
                      ),
                      child: ListTile(
                        title: Text(res.code, style: const TextStyle(fontWeight: FontWeight.bold, fontFamily: 'Courier')),
                        subtitle: Text('${res.items[0].productName} x${res.items[0].quantity}'),
                        trailing: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: NovaStyles.getStatusColor(res.status).withOpacity(0.1),
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: Text(
                            res.statusDisplayName,
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: NovaStyles.getStatusColor(res.status),
                            ),
                          ),
                        ),
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => ReservationDetailView(reservation: res)),
                          );
                        },
                      ),
                    ),
                  );
                },
              ),
            ],
          ],
        ),
      ),
    );
  }
}

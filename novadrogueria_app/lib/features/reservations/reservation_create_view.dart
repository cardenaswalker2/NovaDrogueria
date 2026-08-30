import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_state.dart';
import '../../core/theme/nova_styles.dart';
import '../../core/utils/currency_formatter.dart';
import '../../data/models/data_models.dart';
import '../../shared/widgets/shared_widgets.dart';
import 'reservation_result_view.dart';

class ReservationCreateView extends StatefulWidget {
  final ProductModel product;

  const ReservationCreateView({Key? key, required this.product}) : super(key: key);

  @override
  State<ReservationCreateView> createState() => _ReservationCreateViewState();
}

class _ReservationCreateViewState extends State<ReservationCreateView> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _emailController = TextEditingController();
  final _notesController = TextEditingController();

  int quantity = 1;
  bool isSubmitting = false;

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _emailController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  void _submit() async {
    if (!_formKey.currentState!.validate()) return;
    
    // Safety check with backend logic
    if (quantity > widget.product.stock) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('La cantidad solicitada supera el stock disponible (${widget.product.stock} un.)')),
      );
      return;
    }

    setState(() {
      isSubmitting = true;
    });

    try {
      final state = context.read<NovaAppState>();
      final res = await state.executeReservation(
        name: _nameController.text.trim(),
        phone: _phoneController.text.trim(),
        email: _emailController.text.trim(),
        notes: _notesController.text.trim(),
        productId: widget.product.id,
        quantity: quantity,
      );

      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => ReservationResultView(reservation: res)),
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
          isSubmitting = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final totalCost = widget.product.price * quantity;

    return Scaffold(
      backgroundColor: NovaStyles.background,
      appBar: AppBar(
        title: const Text('Confirmar Apartado'),
        elevation: 0,
        scrolledUnderElevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Step wizard indicator
              Container(
                margin: const EdgeInsets.only(bottom: 20),
                padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: const Color(0xFFE2E8F0)),
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: Row(
                        children: [
                          Container(
                            width: 24,
                            height: 24,
                            decoration: const BoxDecoration(color: NovaStyles.primary, shape: BoxShape.circle),
                            alignment: Alignment.center,
                            child: const Text('1', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12)),
                          ),
                          const SizedBox(width: 8),
                          const Text('Tus datos', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                        ],
                      ),
                    ),
                    const Icon(Icons.arrow_forward_ios, size: 12, color: NovaStyles.textMuted),
                    Expanded(
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          Container(
                            width: 24,
                            height: 24,
                            decoration: BoxDecoration(color: Colors.grey[200], shape: BoxShape.circle),
                            alignment: Alignment.center,
                            child: Text('2', style: TextStyle(color: Colors.grey[600], fontWeight: FontWeight.bold, fontSize: 12)),
                          ),
                          const SizedBox(width: 8),
                          Text('Confirmación', style: TextStyle(color: Colors.grey[600], fontWeight: FontWeight.bold, fontSize: 13)),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              // Product Ticket Card layout
              Card(
                color: Colors.white,
                elevation: 0,
                margin: EdgeInsets.zero,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: const BorderSide(color: Color(0xFFE2E8F0)),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Row(
                    children: [
                      ClipRRect(
                        borderRadius: BorderRadius.circular(8),
                        child: Image.network(
                          widget.product.imageUrl.isNotEmpty ? widget.product.imageUrl : fallbackImage,
                          width: 64,
                          height: 64,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Image.network(fallbackImage, width: 64, height: 64),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(widget.product.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                            Text(widget.product.presentation, style: const TextStyle(fontSize: 12, color: NovaStyles.textMuted)),
                            const SizedBox(height: 4),
                            Text(
                              ColombianCurrencyFormatter.format(widget.product.price),
                              style: const TextStyle(fontWeight: FontWeight.bold, color: NovaStyles.primary),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 20),

              // Quantity Selector
              const Text('Cantidad a reservar:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
              const SizedBox(height: 8),
              Row(
                children: [
                  IconButton(
                    icon: const Icon(Icons.remove_circle_outline, size: 28, color: NovaStyles.primary),
                    onPressed: quantity > 1 ? () => setState(() => quantity--) : null,
                  ),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16.0),
                    child: Text(quantity.toString(), style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  ),
                  IconButton(
                    icon: const Icon(Icons.add_circle_outline, size: 28, color: NovaStyles.primary),
                    onPressed: quantity < widget.product.stock ? () => setState(() => quantity++) : null,
                  ),
                  const Spacer(),
                  Text(
                    'Stock: ${widget.product.stock} un.',
                    style: const TextStyle(color: NovaStyles.textMuted, fontSize: 13),
                  ),
                ],
              ),

              const Divider(height: 32, color: Color(0xFFE2E8F0)),

              // Data fields forms
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(
                  labelText: 'Nombre completo *',
                  hintText: 'Ej. Juan Pérez',
                  border: OutlineInputBorder(),
                  filled: true,
                  fillColor: Colors.white,
                ),
                validator: (val) {
                  if (val == null || val.trim().isEmpty) return 'Ingresa tu nombre completo.';
                  return null;
                },
              ),
              const SizedBox(height: 16),

              TextFormField(
                controller: _phoneController,
                keyboardType: TextInputType.phone,
                decoration: const InputDecoration(
                  labelText: 'Celular de contacto *',
                  hintText: 'Ej. 3001234567',
                  border: OutlineInputBorder(),
                  filled: true,
                  fillColor: Colors.white,
                ),
                validator: (val) {
                  if (val == null || val.trim().isEmpty) return 'Ingresa tu número de celular.';
                  if (val.replaceAll(RegExp(r'[^0-9]'), '').length < 7) {
                    return 'Ingresa un número de celular válido.';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),

              TextFormField(
                controller: _emailController,
                keyboardType: TextInputType.emailAddress,
                decoration: const InputDecoration(
                  labelText: 'Correo electrónico (Opcional)',
                  hintText: 'Ej. juan@correo.com',
                  border: OutlineInputBorder(),
                  filled: true,
                  fillColor: Colors.white,
                ),
              ),
              const SizedBox(height: 16),

              TextFormField(
                controller: _notesController,
                maxLines: 2,
                decoration: const InputDecoration(
                  labelText: 'Notas adicionales (Opcional)',
                  hintText: 'Ej. Lo recogeré al final de la tarde.',
                  border: OutlineInputBorder(),
                  filled: true,
                  fillColor: Colors.white,
                ),
              ),

              const SizedBox(height: 24),

              // Summary invoice total area
              Card(
                color: const Color(0xFFF0FDFA),
                elevation: 0,
                margin: EdgeInsets.zero,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: const BorderSide(color: Color(0xFF99F6E4)),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('Total a pagar en mostrador:', style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF0F766E))),
                          Text(
                            ColombianCurrencyFormatter.format(totalCost),
                            style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF0D9488)),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        ColombianCurrencyFormatter.formatToWords(totalCost),
                        style: const TextStyle(fontSize: 11, color: Color(0xFF0D9488), fontStyle: FontStyle.italic),
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 24),

              NovaPrimaryButton(
                label: 'Apartar por ${ColombianCurrencyFormatter.format(totalCost)}',
                isLoading: isSubmitting,
                onPressed: _submit,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

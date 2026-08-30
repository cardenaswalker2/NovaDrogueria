import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:share_plus/share_plus.dart';
import '../../core/theme/nova_styles.dart';
import '../../core/utils/currency_formatter.dart';
import '../../data/models/data_models.dart';
import '../../shared/widgets/shared_widgets.dart';

class ReservationResultView extends StatelessWidget {
  final ReservationModel reservation;

  const ReservationResultView({Key? key, required this.reservation}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: NovaStyles.background,
      appBar: AppBar(
        title: const Text('Apartado Exitoso'),
        automaticallyImplyLeading: false,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          children: [
            const Icon(Icons.check_circle_rounded, size: 72, color: Colors.green),
            const SizedBox(height: 16),
            const Text(
              '¡Apartado Realizado!',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Colors.green),
            ),
            const SizedBox(height: 8),
            const Text(
              'Hemos reservado tu producto de forma exitosa en nuestro inventario.',
              textAlign: TextAlign.center,
              style: TextStyle(color: NovaStyles.textMuted),
            ),
            const SizedBox(height: 28),

            // Comprobante ticket layout
            Card(
              color: Colors.white,
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
                side: const BorderSide(color: Color(0xFFE2E8F0)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  children: [
                    const Text(
                      'CÓDIGO DE APARTADO ÚNICO',
                      style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: NovaStyles.textMuted, letterSpacing: 0.8),
                    ),
                    const SizedBox(height: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                      decoration: BoxDecoration(
                        color: const Color(0xFFF1F5F9),
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: const Color(0xFFCBD5E1)),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            reservation.code,
                            style: const TextStyle(fontFamily: 'Courier', fontSize: 24, fontWeight: FontWeight.bold, color: NovaStyles.textDark),
                          ),
                          const SizedBox(width: 8),
                          IconButton(
                            icon: const Icon(Icons.copy, size: 18, color: NovaStyles.textMuted),
                            onPressed: () {
                              Clipboard.setData(ClipboardData(text: reservation.code));
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text('✓ Código copiado al portapapeles.')),
                              );
                            },
                          ),
                        ],
                      ),
                    ),
                    
                    const Divider(height: 32, color: Color(0xFFF1F5F9)),

                    // Items display
                    ...reservation.items.map((item) => Padding(
                          padding: const EdgeInsets.symmetric(vertical: 4.0),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('${item.productName} x${item.quantity}', style: const TextStyle(fontWeight: FontWeight.bold)),
                              Text(ColombianCurrencyFormatter.format(item.lineTotal)),
                            ],
                          ),
                        )),
                    
                    const Divider(height: 24, color: Color(0xFFF1F5F9)),

                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Cliente:', style: TextStyle(color: NovaStyles.textMuted)),
                        Text(reservation.customerName, style: const TextStyle(fontWeight: FontWeight.bold)),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Teléfono registrado:', style: TextStyle(color: NovaStyles.textMuted)),
                        Text(reservation.customerPhone, style: const TextStyle(fontWeight: FontWeight.bold)),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Estado:', style: TextStyle(color: NovaStyles.textMuted)),
                        Text(reservation.statusDisplayName, style: const TextStyle(fontWeight: FontWeight.bold, color: NovaStyles.colorPending)),
                      ],
                    ),

                    const Divider(height: 24, color: Color(0xFFCBD5E1)),

                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Total a pagar:', style: TextStyle(fontWeight: FontWeight.bold)),
                        Text(
                          ColombianCurrencyFormatter.format(reservation.total),
                          style: const TextStyle(fontWeight: FontWeight.bold, color: NovaStyles.primary, fontSize: 16),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Align(
                      alignment: Alignment.centerRight,
                      child: Text(
                        ColombianCurrencyFormatter.formatToWords(reservation.total),
                        style: const TextStyle(fontSize: 10, color: NovaStyles.textMuted, fontStyle: FontStyle.italic),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 28),

            // Share & Done buttons
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                    icon: const Icon(Icons.share, size: 20),
                    label: const Text('Compartir'),
                    onPressed: () {
                      Share.share(
                        'Nova Droguería\nApartado: ${reservation.code}\nProducto: ${reservation.items[0].productName}\nTotal: ${ColombianCurrencyFormatter.format(reservation.total)}\nEstado: ${reservation.statusDisplayName}',
                      );
                    },
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: NovaPrimaryButton(
                    label: 'Volver a inicio',
                    onPressed: () {
                      Navigator.popUntil(context, (route) => route.isFirst);
                    },
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

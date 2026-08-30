import 'package:flutter/material.dart';
import '../../core/theme/nova_styles.dart';
import '../../core/utils/currency_formatter.dart';
import '../../data/models/data_models.dart';
import 'package:url_launcher/url_launcher.dart';

class ReservationDetailView extends StatelessWidget {
  final ReservationModel reservation;

  const ReservationDetailView({Key? key, required this.reservation}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final statusColor = NovaStyles.getStatusColor(reservation.status);

    return Scaffold(
      backgroundColor: NovaStyles.background,
      appBar: AppBar(
        title: const Text('Detalle de Apartado'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          children: [
            // Status callout banner card
            Card(
              color: statusColor.withOpacity(0.08),
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
                side: BorderSide(color: statusColor.withOpacity(0.3)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  children: [
                    Text(
                      reservation.statusEmoji,
                      style: const TextStyle(fontSize: 28),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            reservation.statusDisplayName,
                            style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16, color: statusColor),
                          ),
                          const SizedBox(height: 2),
                          const Text('Tu solicitud se encuentra en este estado actualmente.', style: TextStyle(fontSize: 12, color: NovaStyles.textMuted)),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            // Comprobante receipt bill layout card
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
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Center(
                      child: Column(
                        children: [
                          const Text('CÓDIGO DE SEGUIMIENTO', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: NovaStyles.textMuted)),
                          const SizedBox(height: 4),
                          Text(
                            reservation.code,
                            style: const TextStyle(fontFamily: 'Courier', fontSize: 22, fontWeight: FontWeight.bold),
                          ),
                        ],
                      ),
                    ),
                    
                    const Divider(height: 32, color: Color(0xFFF1F5F9)),
                    
                    const Text('Productos reservados:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                    const SizedBox(height: 10),
                    ...reservation.items.map((item) => Padding(
                          padding: const EdgeInsets.only(bottom: 12.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(item.productName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text('Cantidad: ${item.quantity} × ${ColombianCurrencyFormatter.format(item.unitPrice)}', style: const TextStyle(fontSize: 12, color: NovaStyles.textMuted)),
                                  Text(ColombianCurrencyFormatter.format(item.lineTotal), style: const TextStyle(fontWeight: FontWeight.bold)),
                                ],
                              ),
                            ],
                          ),
                        )),
                    
                    const Divider(height: 24, color: Color(0xFFF1F5F9)),
                    
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Total a pagar en local:', style: TextStyle(fontWeight: FontWeight.bold)),
                        Text(
                          ColombianCurrencyFormatter.format(reservation.total),
                          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: NovaStyles.primary),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Align(
                      alignment: Alignment.centerRight,
                      child: Text(
                        ColombianCurrencyFormatter.formatToWords(reservation.total),
                        style: const TextStyle(fontSize: 11, color: NovaStyles.textMuted, fontStyle: FontStyle.italic),
                      ),
                    ),

                    const Divider(height: 24, color: Color(0xFFF1F5F9)),

                    const Text('Información del cliente:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                    const SizedBox(height: 8),
                    Text('Nombre: ${reservation.customerName}', style: const TextStyle(fontSize: 13)),
                    const SizedBox(height: 4),
                    Text('Celular: ${reservation.customerPhone}', style: const TextStyle(fontSize: 13)),
                    if (reservation.customerEmail.isNotEmpty) ...[
                      const SizedBox(height: 4),
                      Text('Email: ${reservation.customerEmail}', style: const TextStyle(fontSize: 13)),
                    ],
                  ],
                ),
              ),
            ),

            const SizedBox(height: 24),

            // Stepper timeline
            const Align(
              alignment: Alignment.centerLeft,
              child: Padding(
                padding: EdgeInsets.only(left: 8.0, bottom: 12.0),
                child: Text('Línea de progreso:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              ),
            ),
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
                    _buildTimelineNode('Pendiente de confirmación', 'Recibido en sistema', reservation.status == 'PENDIENTE', true),
                    _buildTimelineNode('Confirmado', 'Stock separado en estante', reservation.status == 'CONFIRMADO', reservation.status != 'PENDIENTE'),
                    _buildTimelineNode('Listo para recoger', 'Disponible en mostrador físico', reservation.status == 'PREPARADO', (reservation.status == 'PREPARADO' || reservation.status == 'ENTREGADO')),
                    _buildTimelineNode('Entregado', 'Transacción completada', reservation.status == 'ENTREGADO', reservation.status == 'ENTREGADO'),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 24),

            // Support help button
            SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF25D366),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                icon: const Icon(Icons.chat),
                label: const Text('Contactar por WhatsApp', style: TextStyle(fontWeight: FontWeight.bold)),
                onPressed: () async {
                  final text = Uri.encodeComponent('Hola, quiero consultar sobre mi apartado ${reservation.code}.');
                  final url = Uri.parse('https://wa.me/573005722844?text=$text');
                  if (await canLaunchUrl(url)) {
                    await launchUrl(url, mode: LaunchMode.externalApplication);
                  }
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTimelineNode(String label, String sub, bool isActive, bool isPassed) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Column(
          children: [
            Container(
              width: 14,
              height: 14,
              decoration: BoxDecoration(
                color: isActive ? NovaStyles.primary : (isPassed ? Colors.green : Colors.grey[300]),
                shape: BoxShape.circle,
                border: Border.all(color: Colors.white, width: 2),
              ),
            ),
            Container(
              width: 2,
              height: 36,
              color: isPassed ? Colors.green : Colors.grey[200],
            ),
          ],
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 14,
                  color: isActive ? NovaStyles.primary : (isPassed ? NovaStyles.textDark : NovaStyles.textMuted),
                ),
              ),
              Text(sub, style: const TextStyle(fontSize: 11, color: NovaStyles.textMuted)),
            ],
          ),
        ),
      ],
    );
  }
}

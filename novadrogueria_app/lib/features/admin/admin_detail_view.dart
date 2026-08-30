import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_state.dart';
import '../../core/theme/nova_styles.dart';
import '../../core/utils/currency_formatter.dart';
import '../../data/models/data_models.dart';
import '../../shared/widgets/shared_widgets.dart';

class AdminDetailView extends StatefulWidget {
  final ReservationModel reservation;

  const AdminDetailView({Key? key, required this.reservation}) : super(key: key);

  @override
  State<AdminDetailView> createState() => _AdminDetailViewState();
}

class _AdminDetailViewState extends State<AdminDetailView> {
  bool isProcessing = false;
  final TextEditingController _cancelNotesController = TextEditingController();

  @override
  void dispose() {
    _cancelNotesController.dispose();
    super.dispose();
  }

  void _changeStatus(String targetStatus, String actionLabel) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('¿$actionLabel este apartado?'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Esta acción actualizará el estado de la reserva en el backend.'),
            if (targetStatus == 'CANCELADO') ...[
              const SizedBox(height: 12),
              TextField(
                controller: _cancelNotesController,
                decoration: const InputDecoration(
                  labelText: 'Motivo de la cancelación',
                  border: OutlineInputBorder(),
                ),
              ),
            ],
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancelar'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: NovaStyles.primary, foregroundColor: Colors.white),
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Confirmar'),
          ),
        ],
      ),
    );

    if (confirm != true) return;

    setState(() {
      isProcessing = true;
    });

    try {
      final state = context.read<NovaAppState>();
      await state.updateReservationState(
        widget.reservation.id,
        targetStatus,
        notes: targetStatus == 'CANCELADO' ? _cancelNotesController.text.trim() : null,
      );

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('✓ Estado actualizado correctamente.')),
        );
        Navigator.pop(context);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: ${e.toString()}')),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          isProcessing = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final res = widget.reservation;
    final statusColor = NovaStyles.getStatusColor(res.status);

    return Scaffold(
      backgroundColor: NovaStyles.background,
      appBar: AppBar(
        title: const Text('Detalle Administrativo'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Status bar card
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
                    Text(res.statusEmoji, style: const TextStyle(fontSize: 28)),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(res.statusDisplayName, style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: statusColor)),
                          Text('Código: ${res.code}', style: const TextStyle(fontSize: 12, color: NovaStyles.textMuted)),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            // Customer details card
            Card(
              color: Colors.white,
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
                side: const BorderSide(color: Color(0xFFE2E8F0)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('DATOS DEL CLIENTE', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: NovaStyles.textMuted)),
                    const SizedBox(height: 12),
                    Text('Nombre: ${res.customerName}', style: const TextStyle(fontWeight: FontWeight.bold)),
                    const SizedBox(height: 4),
                    Text('Celular: ${res.customerPhone}'),
                    if (res.customerEmail.isNotEmpty) ...[
                      const SizedBox(height: 4),
                      Text('Email: ${res.customerEmail}'),
                    ],
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Products summary card
            Card(
              color: Colors.white,
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
                side: const BorderSide(color: Color(0xFFE2E8F0)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('PRODUCTOS RESERVADOS', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: NovaStyles.textMuted)),
                    const SizedBox(height: 12),
                    ...res.items.map((item) => Padding(
                          padding: const EdgeInsets.only(bottom: 8.0),
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
                        const Text('Total:', style: TextStyle(fontWeight: FontWeight.bold)),
                        Text(ColombianCurrencyFormatter.format(res.total), style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 16, color: NovaStyles.primary)),
                      ],
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 32),

            // Context Actions buttons toolbar
            if (isProcessing)
              const Center(child: CircularProgressIndicator())
            else ...[
              if (res.status == 'PENDIENTE') ...[
                NovaPrimaryButton(
                  label: 'Confirmar Apartado',
                  icon: Icons.check,
                  onPressed: () => _changeStatus('CONFIRMADO', 'Confirmar'),
                ),
                const SizedBox(height: 12),
                NovaPrimaryButton(
                  label: 'Cancelar Apartado',
                  icon: Icons.cancel,
                  backgroundColor: Colors.red,
                  onPressed: () => _changeStatus('CANCELADO', 'Cancelar'),
                ),
              ] else if (res.status == 'CONFIRMADO') ...[
                NovaPrimaryButton(
                  label: 'Marcar como preparado',
                  icon: Icons.inventory_2_outlined,
                  backgroundColor: Colors.purple,
                  onPressed: () => _changeStatus('PREPARADO', 'Preparar'),
                ),
                const SizedBox(height: 12),
                NovaPrimaryButton(
                  label: 'Cancelar Apartado',
                  icon: Icons.cancel,
                  backgroundColor: Colors.red,
                  onPressed: () => _changeStatus('CANCELADO', 'Cancelar'),
                ),
              ] else if (res.status == 'PREPARADO') ...[
                NovaPrimaryButton(
                  label: 'Marcar como entregado',
                  icon: Icons.check_circle_outline,
                  backgroundColor: Colors.green,
                  onPressed: () => _changeStatus('ENTREGADO', 'Entregar'),
                ),
                const SizedBox(height: 12),
                NovaPrimaryButton(
                  label: 'Cancelar Apartado',
                  icon: Icons.cancel,
                  backgroundColor: Colors.red,
                  onPressed: () => _changeStatus('CANCELADO', 'Cancelar'),
                ),
              ] else ...[
                Center(
                  child: Text(
                    res.status == 'ENTREGADO' ? '✅ Apartado Entregado' : '❌ Apartado Cancelado',
                    style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: NovaStyles.textMuted),
                  ),
                ),
              ]
            ],
          ],
        ),
      ),
    );
  }
}

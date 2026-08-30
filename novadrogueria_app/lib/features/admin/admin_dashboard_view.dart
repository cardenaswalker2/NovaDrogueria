import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_state.dart';
import '../../core/theme/nova_styles.dart';
import '../../core/utils/currency_formatter.dart';
import '../../data/models/data_models.dart';
import 'admin_detail_view.dart';

class AdminDashboardView extends StatefulWidget {
  const AdminDashboardView({Key? key}) : super(key: key);

  @override
  State<AdminDashboardView> createState() => _AdminDashboardViewState();
}

class _AdminDashboardViewState extends State<AdminDashboardView> {
  String selectedFilterStatus = '';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<NovaAppState>().loadAdminDashboard();
    });
  }

  void _filter(String status) {
    setState(() {
      selectedFilterStatus = status;
    });
    context.read<NovaAppState>().loadAdminReservations(
      status: status.isEmpty ? null : status,
    );
  }

  @override
  Widget build(BuildContext context) {
    final state = context.watch<NovaAppState>();
    final metrics = state.adminMetrics;
    final list = state.adminReservations;

    return Scaffold(
      backgroundColor: NovaStyles.background,
      appBar: AppBar(
        title: const Text('Gestión de Apartados'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout, color: Colors.red),
            onPressed: () async {
              await state.logoutAdmin();
              if (context.mounted) {
                Navigator.pop(context);
              }
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Brief metrics top bar row
          if (metrics != null)
            Container(
              color: Colors.white,
              padding: const EdgeInsets.symmetric(vertical: 12.0, horizontal: 16.0),
              child: SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: [
                    _buildMetricCard('Pendientes', metrics['countPending'] ?? 0, Colors.amber),
                    const SizedBox(width: 8),
                    _buildMetricCard('Confirmados', metrics['countConfirmed'] ?? 0, Colors.blue),
                    const SizedBox(width: 8),
                    _buildMetricCard('Preparados', metrics['countPrepared'] ?? 0, Colors.purple),
                    const SizedBox(width: 8),
                    _buildMetricCard('Entregados Hoy', metrics['countDeliveredToday'] ?? 0, Colors.green),
                  ],
                ),
              ),
            ),

          // Filters Choice Row chips
          Container(
            color: Colors.white,
            padding: const EdgeInsets.only(bottom: 12, left: 16, right: 16),
            alignment: Alignment.centerLeft,
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  _buildFilterChip('Todos', ''),
                  const SizedBox(width: 6),
                  _buildFilterChip('Pendientes', 'PENDIENTE'),
                  const SizedBox(width: 6),
                  _buildFilterChip('Confirmados', 'CONFIRMADO'),
                  const SizedBox(width: 6),
                  _buildFilterChip('Preparados', 'PREPARADO'),
                  const SizedBox(width: 6),
                  _buildFilterChip('Entregados', 'ENTREGADO'),
                  const SizedBox(width: 6),
                  _buildFilterChip('Cancelados', 'CANCELADO'),
                ],
              ),
            ),
          ),

          // Reservations lists outcomes
          Expanded(
            child: state.isLoading
                ? const Center(child: CircularProgressIndicator())
                : list.isEmpty
                    ? const Center(child: Text('No hay apartados en esta sección.'))
                    : ListView.builder(
                        padding: const EdgeInsets.all(16),
                        itemCount: list.length,
                        itemBuilder: (context, index) {
                          final res = list[index];
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
                                title: Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Text(res.code, style: const TextStyle(fontWeight: FontWeight.bold, fontFamily: 'Courier')),
                                    Text(
                                      ColombianCurrencyFormatter.format(res.total),
                                      style: const TextStyle(fontWeight: FontWeight.w800, color: NovaStyles.primary, fontSize: 13),
                                    ),
                                  ],
                                ),
                                subtitle: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    const SizedBox(height: 4),
                                    Text('Cliente: ${res.customerName}'),
                                    Text('Celular: ${res.customerPhone}'),
                                    Text('Producto: ${res.items[0].productName} x${res.items[0].quantity}', style: const TextStyle(fontSize: 12, color: NovaStyles.textMuted)),
                                  ],
                                ),
                                trailing: Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                  decoration: BoxDecoration(
                                    color: NovaStyles.getStatusColor(res.status).withOpacity(0.1),
                                    borderRadius: BorderRadius.circular(20),
                                  ),
                                  child: Text(
                                    res.statusDisplayName,
                                    style: TextStyle(
                                      fontSize: 10,
                                      fontWeight: FontWeight.bold,
                                      color: NovaStyles.getStatusColor(res.status),
                                    ),
                                  ),
                                ),
                                onTap: () {
                                  Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                      builder: (context) => AdminDetailView(reservation: res),
                                    ),
                                  ).then((_) => state.loadAdminDashboard());
                                },
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

  Widget _buildMetricCard(String label, int value, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: color.withOpacity(0.08),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: color.withOpacity(0.3)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            value.toString(),
            style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16, color: color),
          ),
          const SizedBox(width: 6),
          Text(label, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }

  Widget _buildFilterChip(String label, String value) {
    final isSelected = selectedFilterStatus == value;
    return ChoiceChip(
      label: Text(label, style: TextStyle(fontSize: 12, color: isSelected ? Colors.white : NovaStyles.textDark)),
      selected: isSelected,
      selectedColor: NovaStyles.primary,
      onSelected: (_) => _filter(value),
    );
  }
}

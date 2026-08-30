import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_state.dart';
import '../../core/theme/nova_styles.dart';
import '../admin/admin_login_view.dart';
import 'package:url_launcher/url_launcher.dart';

class InformationView extends StatelessWidget {
  const InformationView({Key? key}) : super(key: key);

  void _showError(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message, style: const TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: Colors.redAccent,
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final state = context.watch<NovaAppState>();
    final config = state.appConfig;

    return Scaffold(
      backgroundColor: NovaStyles.background,
      appBar: AppBar(
        title: const Text('Sobre Nosotros'),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 20.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // 1. Store Header Info
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
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: NovaStyles.primary.withOpacity(0.1),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.local_pharmacy, size: 50, color: NovaStyles.primary),
                      ),
                      const SizedBox(height: 12),
                      Text(
                        config?.storeName ?? 'Nova Droguería',
                        style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: NovaStyles.textDark),
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'Tu farmacia de confianza con tecnología y cercanía.',
                        textAlign: TextAlign.center,
                        style: TextStyle(color: NovaStyles.textMuted, fontSize: 13, height: 1.4),
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 16),

              // 2. Business Details Card
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
                      _buildInfoRow(Icons.phone, 'Teléfono', config?.phone ?? '+57 300 572 2844'),
                      const Divider(height: 24, color: Color(0xFFF1F5F9)),
                      _buildInfoRow(Icons.access_time, 'Horarios de Atención', config?.schedule ?? 'Lunes a Sábado: 8:00 AM - 9:00 PM\nDomingos y Festivos: 9:00 AM - 6:00 PM'),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 16),

              // 3. committed to health card section
              Card(
                color: const Color(0xFFF0FDF4),
                elevation: 0,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                  side: const BorderSide(color: Color(0xFFDCFCE7)),
                ),
                child: const Padding(
                  padding: EdgeInsets.all(16.0),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Icon(Icons.favorite, color: Colors.green, size: 24),
                      SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Comprometidos con tu salud',
                              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: Color(0xFF166534)),
                            ),
                            SizedBox(height: 4),
                            Text(
                              'Trabajamos para ofrecer productos de calidad, atención cercana y una experiencia sencilla y confiable para nuestros clientes.',
                              style: TextStyle(fontSize: 12, color: Color(0xFF1F2937), height: 1.4),
                            ),
                          ],
                        ),
                      )
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 20),

              // 4. Call & WhatsApp Action Buttons
              Row(
                children: [
                  Expanded(
                    child: SizedBox(
                      height: 52,
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: NovaStyles.primary,
                          foregroundColor: Colors.white,
                          elevation: 0,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                        icon: const Icon(Icons.phone_in_talk, size: 20),
                        label: const Text('Llamar', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                        onPressed: () async {
                          final phoneUrl = Uri.parse('tel:+573005722844');
                          if (await canLaunchUrl(phoneUrl)) {
                            await launchUrl(phoneUrl);
                          } else {
                            if (context.mounted) {
                              _showError(context, 'No se pudo iniciar la llamada.');
                            }
                          }
                        },
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: SizedBox(
                      height: 52,
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF25D366),
                          foregroundColor: Colors.white,
                          elevation: 0,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                        icon: const Icon(Icons.chat_bubble, size: 20),
                        label: const Text('WhatsApp', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                        onPressed: () async {
                          const number = '573005722844';
                          const msg = 'Hola, Nova Droguería. Quisiera consultar sobre un product.';
                          final waUrl = Uri.parse('https://wa.me/$number?text=${Uri.encodeComponent(msg)}');
                          
                          if (await canLaunchUrl(waUrl)) {
                            await launchUrl(waUrl, mode: LaunchMode.externalApplication);
                          } else {
                            if (context.mounted) {
                              _showError(context, 'No pudimos abrir WhatsApp. Puedes contactarnos al +57 300 572 2844.');
                            }
                          }
                        },
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 12),

              // 5. Maps visual button (Coordenadas Reales)
              SizedBox(
                height: 52,
                child: OutlinedButton.icon(
                  style: OutlinedButton.styleFrom(
                    side: const BorderSide(color: NovaStyles.primary, width: 1.5),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                  icon: const Icon(Icons.map, color: NovaStyles.primary, size: 20),
                  label: const Text('Cómo llegar', style: TextStyle(color: NovaStyles.primary, fontWeight: FontWeight.bold, fontSize: 14)),
                  onPressed: () async {
                    final url = Uri.parse('https://www.google.com/maps/dir/?api=1&destination=10.4122807,-75.458924');
                    if (await canLaunchUrl(url)) {
                      await launchUrl(url, mode: LaunchMode.externalApplication);
                    } else {
                      if (context.mounted) {
                        _showError(context, 'No pudimos abrir Google Maps. Intenta nuevamente.');
                      }
                    }
                  },
                ),
              ),

              const SizedBox(height: 36),

              // 6. Restrictive discrete admin login panel trigger
              const Divider(color: Color(0xFFE2E8F0)),
              const SizedBox(height: 8),
              Center(
                child: TextButton.icon(
                  style: TextButton.styleFrom(
                    foregroundColor: NovaStyles.textMuted,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                  icon: const Icon(Icons.lock_outline, size: 16),
                  label: const Text(
                    'Acceso Administrador',
                    style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, decoration: TextDecoration.underline),
                  ),
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => const AdminLoginView()),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, color: NovaStyles.primary, size: 22),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: const TextStyle(fontSize: 12, color: NovaStyles.textMuted, fontWeight: FontWeight.bold)),
              const SizedBox(height: 2),
              Text(value, style: const TextStyle(fontSize: 14, color: NovaStyles.textDark, fontWeight: FontWeight.w600, height: 1.3)),
            ],
          ),
        ),
      ],
    );
  }
}

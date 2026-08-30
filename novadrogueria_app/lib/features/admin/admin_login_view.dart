import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_state.dart';
import '../../core/theme/nova_styles.dart';
import '../../shared/widgets/shared_widgets.dart';
import 'admin_dashboard_view.dart';

class AdminLoginView extends StatefulWidget {
  const AdminLoginView({Key? key}) : super(key: key);

  @override
  State<AdminLoginView> createState() => _AdminLoginViewState();
}

class _AdminLoginViewState extends State<AdminLoginView> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  
  bool isLogging = false;
  bool _obscurePassword = true;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  void _login() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      isLogging = true;
    });

    try {
      final state = context.read<NovaAppState>();
      await state.performAdminLogin(
        _usernameController.text.trim(),
        _passwordController.text,
      );

      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => const AdminDashboardView()),
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
          isLogging = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: NovaStyles.background,
      appBar: AppBar(
        title: const Text('Acceso Administrativo'),
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24.0),
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.admin_panel_settings, size: 64, color: NovaStyles.primary),
                const SizedBox(height: 16),
                const Text(
                  'Panel de Gestión Móvil',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: NovaStyles.textDark),
                ),
                const Text(
                  'Accede con tus credenciales de Nova Droguería',
                  style: TextStyle(fontSize: 12, color: NovaStyles.textMuted),
                ),
                
                const SizedBox(height: 32),

                // Credentials inputs
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
                        TextFormField(
                          controller: _usernameController,
                          decoration: const InputDecoration(
                            labelText: 'Usuario / Email',
                            prefixIcon: Icon(Icons.person, color: NovaStyles.primary),
                            border: OutlineInputBorder(),
                          ),
                          validator: (val) {
                            if (val == null || val.trim().isEmpty) return 'Ingresa tu usuario.';
                            return null;
                          },
                        ),
                        const SizedBox(height: 16),
                        TextFormField(
                          controller: _passwordController,
                          obscureText: _obscurePassword,
                          decoration: InputDecoration(
                            labelText: 'Contraseña',
                            prefixIcon: const Icon(Icons.lock, color: NovaStyles.primary),
                            border: const OutlineInputBorder(),
                            suffixIcon: IconButton(
                              icon: Icon(_obscurePassword ? Icons.visibility_off : Icons.visibility, color: NovaStyles.textMuted),
                              onPressed: () {
                                setState(() {
                                  _obscurePassword = !_obscurePassword;
                                });
                              },
                            ),
                          ),
                          validator: (val) {
                            if (val == null || val.trim().isEmpty) return 'Ingresa tu contraseña.';
                            return null;
                          },
                        ),
                        const SizedBox(height: 20),
                        NovaPrimaryButton(
                          label: 'Iniciar Sesión',
                          isLoading: isLogging,
                          onPressed: _login,
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

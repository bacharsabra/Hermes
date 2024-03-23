import 'package:flutter/material.dart';
import 'package:hermesclient/pages/sign_in_page.dart';
import 'package:hermesclient/styles/styles.dart';
import 'package:hermesclient/util/glassmorphism_widget.dart';
import 'package:hermesclient/auth_service.dart';
import 'package:hermesclient/pages/home_page.dart';
import 'package:hermesclient/util/form_validations.dart';

class SignUpPage extends StatefulWidget {
  const SignUpPage({super.key});

  @override
  _SignUpPageState createState() => _SignUpPageState();
}

class _SignUpPageState extends State<SignUpPage> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController usernameController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();
  bool passwordVisible = false;
  String signUpError = '';
  bool _submitted = false;

  void _trySubmitForm() async {
    setState(() {
      _submitted = true;
      signUpError = '';
    });

    if (!_formKey.currentState!.validate()) {
      return;
    }

    final String username = usernameController.text.trim();
    final String password = passwordController.text;

    try {
      bool success = await AuthService().signUp(username, password);
      if (!success) {
        setState(() {
          signUpError = 'This user already exists.';
        });
      } else {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => HomePage(
              username: username,
              password: password,
              isTokenAuth: false,
            ),
          ),
        );
      }
    } catch (e) {
      setState(() {
        signUpError = 'An error occurred while signing up.';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: gradientBackground,
        child: Center(
          child: SingleChildScrollView(
            child: GlassmorphismWidget(
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Form(
                  key: _formKey,
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      TextFormField(
                        controller: usernameController,
                        decoration: const InputDecoration(
                          hintText: "Username",
                        ),
                        validator: (value) {
                          if (value == null || value.isEmpty) {
                            return 'Username is required';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 10),
                      TextFormField(
                        controller: passwordController,
                        decoration: InputDecoration(
                          hintText: "Password",
                          suffixIcon: IconButton(
                            icon: Icon(passwordVisible ? Icons.visibility : Icons.visibility_off),
                            onPressed: () {
                              setState(() {
                                passwordVisible = !passwordVisible;
                              });
                            },
                          ),
                        ),
                        obscureText: !passwordVisible,
                        validator: FormValidations.validatePassword,
                      ),
                      const SizedBox(height: 20),
                      if (signUpError.isNotEmpty)
                        Text(
                          signUpError,
                          style: const TextStyle(color: Colors.red, fontSize: 14),
                        ),
                      const SizedBox(height: 20),
                      ElevatedButton(
                        onPressed: _trySubmitForm,
                        child: const Text("Sign Up"),
                      ),
                      TextButton(
                        onPressed: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => const SignInPage()),
                          );
                        },
                        child: const Text("Already Registered? Sign In"),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    usernameController.dispose();
    passwordController.dispose();
    super.dispose();
  }
}

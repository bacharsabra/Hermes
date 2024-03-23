import 'package:flutter/material.dart';
import 'package:hermesclient/pages/home_page.dart';
import 'package:hermesclient/pages/sign_up_page.dart';
import 'package:hermesclient/styles/styles.dart';
import 'package:hermesclient/util/glassmorphism_widget.dart';
import 'package:hermesclient/auth_service.dart';

class SignInPage extends StatefulWidget {
  const SignInPage({super.key});

  @override
  _SignInPageState createState() => _SignInPageState();
}

class _SignInPageState extends State<SignInPage> {
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final TextEditingController _tokenController = TextEditingController();
  bool _passwordVisible = false;
  String _errorMessage = '';

  Future<void> _trySignIn() async {
    final bool success = await AuthService().signIn(
      _usernameController.text,
      _passwordController.text,
    );

    if (!success) {
      setState(() {
        _errorMessage = 'Incorrect Credentials.';
      });
    } else {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (context) => HomePage(
            username: _usernameController.text,
            password: _passwordController.text,
            isTokenAuth: false,
          ),
        ),
      );
    }
  }

  Future<void> _signInWithToken() async {
    final bool success = await AuthService().signInWithToken(
      _tokenController.text,
    );

    if (!success) {
      setState(() {
        _errorMessage = 'Invalid Token.';
      });
    } else {
      final String username = await AuthService().getUsernameFromToken(_tokenController.text);

      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (context) => HomePage(
            username: username,
            password: '',
            isTokenAuth: true,
            token: _tokenController.text,
          ),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: gradientBackground,
        child: Center(
          child: GlassmorphismWidget(
            child: Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: _usernameController,
                    decoration: const InputDecoration(hintText: "Username"),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: _passwordController,
                    decoration: InputDecoration(
                      hintText: "Password",
                      suffixIcon: IconButton(
                        icon: Icon(
                          _passwordVisible ? Icons.visibility : Icons.visibility_off,
                        ),
                        onPressed: () {
                          setState(() {
                            _passwordVisible = !_passwordVisible;
                          });
                        },
                      ),
                    ),
                    obscureText: !_passwordVisible,
                  ),
                  const SizedBox(height: 20),
                  ElevatedButton(
                    onPressed: _trySignIn,
                    child: const Text("Sign In"),
                  ),
                  if (_errorMessage.isNotEmpty)
                    Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: Text(
                        _errorMessage,
                        style: const TextStyle(color: Colors.red, fontSize: 14),
                      ),
                    ),
                  TextButton(
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (context) => const SignUpPage()),
                      );
                    },
                    child: const Text("Not Registered Yet? Sign Up"),
                  ),
                  Container(
                    child: Center(
                          child: Column(
                            children: [
                              const SizedBox(height: 10),
                              TextField(
                                controller: _tokenController,
                                decoration: const InputDecoration(hintText: "Token"),
                              ),
                              const SizedBox(height: 20),
                              ElevatedButton(
                                onPressed: _signInWithToken,
                                child: const Text("Sign In With Token"),
                              ),
                              if (_errorMessage.isNotEmpty)
                                Padding(
                                  padding: const EdgeInsets.only(top: 8),
                                  child: Text(
                                    _errorMessage,
                                    style: const TextStyle(color: Colors.red, fontSize: 14),
                                  ),
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
      ),
    );
  }

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    _tokenController.dispose();
    super.dispose();
  }
}

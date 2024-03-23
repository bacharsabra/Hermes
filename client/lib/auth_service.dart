import 'dart:convert';
import 'package:http/http.dart' as http;

class AuthService {
  final String _baseUrl = "http://10.0.2.2:8080/basic-auth/api";


  final String usernameAdmin = 'admin';
  final String passwordAdmin = 'admin';

  Future<bool> signIn(String username, String password) async {
    String basicAuth = 'Basic ${base64Encode(utf8.encode('$username:$password'))}';
    try {
      final response = await http.get(
        Uri.parse('$_baseUrl/users/$username'),
        headers: <String, String>{
          'Authorization': basicAuth,
        },
      );

      if (response.statusCode == 200) {
        print('Signed in');
        return true;
      } else {
        print('Failed to sign in');
        return false;
      }
    } catch (e) {
      print('Error during sign in: $e');
      return false;
    }
  }

  Future<bool> signUp(String username, String password) async {
    final String adminCredentials = base64Encode(utf8.encode('$usernameAdmin:$passwordAdmin'));
    final response = await http.post(
      Uri.parse('$_baseUrl/users'),
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Basic $adminCredentials',
      },
      body: jsonEncode(<String, dynamic>{
        'username': username,
        'password': password,
        "isToken" : false,
      }),
    );

    if (response.statusCode == 200 || response.statusCode == 201) {
      print('Signed up');
      return true;
    } else if (response.statusCode == 400) {
      print('User already exists');
      return false;
    } else {
      print('Failed to sign up');
      return false;
    }
  }

  Future<bool> signInWithToken(String token) async {
    final String adminCredentials = base64Encode(utf8.encode('$usernameAdmin:$passwordAdmin'));
    final uri = Uri.parse('http://10.0.2.2:8080/basic-auth/api/users/$token');
    try {
      final response = await http.get(uri, headers: {
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Basic $adminCredentials',
      });

      if (response.statusCode == 200) {
        return true;
      }
    } catch (e) {
      print(e);
    }
    return false;
  }

  Future<String> getUsernameFromToken(String token) async {
    final uri = Uri.parse('http://10.0.2.2:8080/basic-auth/api/users/$token');
    try {
      final response = await http.get(uri, headers: {
        'Authorization': 'Bearer $token',
      });

      if (response.statusCode == 200) {
        final Map<String, dynamic> data = json.decode(response.body);
        final String username = data['username'];
        return username;
      }
    } catch (e) {
      print(e);
    }
    return '';
  }
}

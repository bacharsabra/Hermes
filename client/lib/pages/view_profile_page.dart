import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:hermesclient/pages/sign_in_page.dart';
import 'package:hermesclient/styles/styles.dart';
import 'package:http/http.dart' as http;
import 'package:hermesclient/classes/user.dart';
import 'package:hermesclient/pages/edit_profile_page.dart';

class ViewProfilePage extends StatefulWidget {
  final String username;
  final String password;
  const ViewProfilePage(
      {super.key, required this.username, required this.password});

  @override
  _ViewProfilePageState createState() => _ViewProfilePageState();
}

class _ViewProfilePageState extends State<ViewProfilePage> {
  late Future<User> futureUser;

  @override
  void initState() {
    super.initState();
    futureUser = fetchUserProfile();
  }

  Future<User> fetchUserProfile() async {
    final uri = Uri.parse(
        'http://10.0.2.2:8080/basic-auth/api/users/${widget.username}');
    final headers = {
      'Authorization':
          'Basic ${base64Encode(utf8.encode('${widget.username}:${widget.password}'))}',
    };
    final response = await http.get(uri, headers: headers);

    if (response.statusCode == 200) {
      return User.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to load user profile');
    }
  }

  void signout() {
    Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (context) => const SignInPage()));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile', style: TextStyle(color: Colors.white)),
        backgroundColor: primaryBlue,
        iconTheme: const IconThemeData(
          color: Colors.white,
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                    builder: (_) => EditProfilePage(
                        username: widget.username, password: widget.password)),
              );
            },
          ),
        ],
      ),
      body: FutureBuilder<User>(
        future: futureUser,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          } else if (snapshot.hasData) {
            return Container(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 20),
                  const Text('Username:',
                      style: TextStyle(fontWeight: FontWeight.bold)),
                  TextField(
                    controller:
                        TextEditingController(text: snapshot.data!.username),
                    readOnly: true,
                    decoration: const InputDecoration(
                      border: InputBorder.none,
                    ),
                  ),
                  const SizedBox(height: 20),
                  Align(
                    alignment: Alignment.centerLeft,
                    child: TextButton.icon(
                      onPressed: signout,
                      icon: const Icon(Icons.logout, color: Colors.blueGrey),
                      label: const Text('Sign Out',
                          style: TextStyle(
                            color: Colors.blueGrey,
                            fontSize: 16,
                            fontWeight: FontWeight.bold
                          )
                      ),
                    ),
                  ),
                ],
              ),
            );
          } else {
            return const Center(child: Text('User not found'));
          }
        },
      ),
    );
  }
}

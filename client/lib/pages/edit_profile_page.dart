import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:hermesclient/styles/styles.dart';
import 'package:hermesclient/util/form_validations.dart';
import 'package:http/http.dart' as http;

class EditProfilePage extends StatefulWidget {
  final String username;
  final String password;

  const EditProfilePage(
      {super.key, required this.username, required this.password});

  @override
  _EditProfilePageState createState() => _EditProfilePageState();
}

class _EditProfilePageState extends State<EditProfilePage> {
  final usernameController = TextEditingController();
  final currentPasswordController = TextEditingController();
  final newPasswordController = TextEditingController();
  final confirmNewPasswordController = TextEditingController();
  bool _submitted = false;
  bool _showConfirmPasswordField = false;

  Future<bool> updatePassword(
      String currentPassword, String newPassword) async {
    final uri = Uri.parse(
        'http://10.0.2.2:8080/basic-auth/api/${widget.username}/change-password');
    final headers = {
      'Authorization':
          'Basic ${base64Encode(utf8.encode('${widget.username}:${widget.password}'))}',
    };
    final response = await http.patch(
      uri,
      headers: headers,
      body: json.encode({
        'currentPassword': currentPassword,
        'newPassword': newPassword,
      }),
    );
    return response.statusCode == 200;
  }

  void _updateProfile() async {
    setState(() => _submitted = true);
    bool passwordUpdated = false;

    if (currentPasswordController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Current password is required'),
        backgroundColor: Colors.red,
      ));
      return;
    }

    if (newPasswordController.text.isNotEmpty &&
        newPasswordController.text == confirmNewPasswordController.text &&
        newPasswordController.text != widget.password) {
      passwordUpdated = await updatePassword(
          currentPasswordController.text, newPasswordController.text);
    }

    if (passwordUpdated) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Profile updated successfully!'),
        backgroundColor: primaryGreen,
      ));
    } else {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Failed to update profile. Please try again.'),
        backgroundColor: Colors.red,
      ));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title:
            const Text("Edit Profile", style: TextStyle(color: Colors.white)),
        iconTheme: const IconThemeData(color: Colors.white),
        backgroundColor: primaryBlue,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 8.0),
              child: Row(
                children: [
                  const Icon(Icons.person, color: primaryBlue),
                  const SizedBox(width: 10),
                  Text(widget.username),
                ],
              ),
            ),
            const SizedBox(height: 20),
            TextFormField(
              controller: currentPasswordController,
              decoration: InputDecoration(
                labelText: 'Current Password',
                errorText: _submitted
                    ? FormValidations.validateCurrentPassword(
                        currentPasswordController.text, widget.password)
                    : null,
                border: const OutlineInputBorder(),
                prefixIcon: const Icon(Icons.lock_outline, color: primaryBlue),
              ),
              obscureText: true,
            ),
            const SizedBox(height: 20),
            TextFormField(
              controller: newPasswordController,
              decoration: InputDecoration(
                labelText: 'New Password',
                errorText: _submitted
                    ? FormValidations.validateNewPassword(
                        newPasswordController.text)
                    : null,
                border: const OutlineInputBorder(),
                prefixIcon: const Icon(Icons.lock, color: primaryBlue),
              ),
              obscureText: true,
              onChanged: (value) {
                if (!_showConfirmPasswordField && value.isNotEmpty) {
                  setState(() => _showConfirmPasswordField = true);
                } else if (_showConfirmPasswordField && value.isEmpty) {
                  setState(() => _showConfirmPasswordField = false);
                }
              },
            ),
            const SizedBox(height: 20),
            if (_showConfirmPasswordField)
              TextFormField(
                controller: confirmNewPasswordController,
                decoration: InputDecoration(
                  labelText: 'Confirm New Password',
                  errorText: _submitted
                      ? FormValidations.validateConfirmNewPassword(
                          confirmNewPasswordController.text,
                          newPasswordController.text)
                      : null,
                  border: const OutlineInputBorder(),
                  prefixIcon: const Icon(Icons.lock, color: primaryBlue),
                ),
                obscureText: true,
              ),
            const SizedBox(height: 20),
            Align(
              alignment: Alignment.centerRight,
              child: ElevatedButton(
                onPressed: _updateProfile,
                style: ElevatedButton.styleFrom(
                  foregroundColor: Colors.white,
                  backgroundColor: primaryGreen,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(18.0),
                  ),
                ),
                child: const Text('Save'),
              ),
            )
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    usernameController.dispose();
    currentPasswordController.dispose();
    newPasswordController.dispose();
    confirmNewPasswordController.dispose();
    super.dispose();
  }
}

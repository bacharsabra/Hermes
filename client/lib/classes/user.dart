import 'package:hermesclient/classes/place.dart';

class User {
  final String username;
  final bool isToken;
  final DateTime? tokenExpiryDate;
  final String href;
  final Place? place;

  User({
      required this.username,
      required this.isToken,
      this.tokenExpiryDate,
      required this.href,
      this.place
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      username: json['username'] as String,
      isToken: json['isToken'] as bool,
      tokenExpiryDate: json['tokenExpiryDate'] != null ? DateTime.parse(json['tokenExpiryDate']) : null,
      href: json['href'] as String,
      place: json['place'] != null ? Place.fromJson(json['place']) : null,
    );
  }
}
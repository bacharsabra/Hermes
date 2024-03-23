import 'package:hermesclient/classes/user_access.dart';

class Place {
  final String id;
  String name;
  String description;
  double latitude;
  double longitude;
  final String creator;
  final DateTime updateTimestamp;
  final String href;
  final List<UserAccess>? userAccess;

  Place({
    required this.id,
    required this.name,
    required this.description,
    required this.latitude,
    required this.longitude,
    required this.creator,
    required this.updateTimestamp,
    required this.href,
    this.userAccess,
  });

  factory Place.fromJson(Map<String, dynamic> json) {
    return Place(
      id: json['id'] as String,
      name: json['name'] as String,
      description: json['description'] as String,
      latitude: json['latitude'] as double,
      longitude: json['longitude'] as double,
      creator: json['creator'] as String,
      updateTimestamp: DateTime.parse(json['updateTimestamp']),
      href: json['href'] as String,
      userAccess: (json['userAccess'] as List).map((e) => UserAccess.fromJson(e)).toList(),
    );
  }
}

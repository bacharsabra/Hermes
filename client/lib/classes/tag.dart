import 'package:hermesclient/classes/user_access.dart';

class Tag {
  final String tagName;
  final List<UserAccess> userAccess;
  final String creator;
  final String updateTimestamp;
  final String href;

  Tag({
    required this.tagName,
    required this.userAccess,
    required this.creator,
    required this.updateTimestamp,
    required this.href,
  });

  factory Tag.fromJson(Map<String, dynamic> json) {
    return Tag(
      tagName: json['tagName'] as String,
      userAccess: (json['userAccess'] as List).map((e) => UserAccess.fromJson(e)).toList(),
      creator: json['creator'] as String,
      updateTimestamp: json['updateTimestamp'] as String,
      href: json['href'] as String,
    );
  }
}

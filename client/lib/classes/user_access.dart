class UserAccess {
  final String username;
  final bool canEdit;

  UserAccess({required this.username, required this.canEdit});

  factory UserAccess.fromJson(Map<String, dynamic> json) {
    return UserAccess(
      username: json['username'],
      canEdit: json['canEdit'],
    );
  }
  
  Map<String, dynamic> toJson() {
    return {
      'username': username,
      'canEdit': canEdit,
    };
  }
}

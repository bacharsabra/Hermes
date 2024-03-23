class Collection {
  final String name;

  Collection({required this.name});

  factory Collection.fromJson(Map<String, dynamic> json) {
    return Collection(
      name: json['name'] as String,
    );
  }
}

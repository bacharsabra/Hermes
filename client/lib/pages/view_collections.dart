import 'package:flutter/material.dart';
import 'package:hermesclient/classes/tag.dart';
import 'dart:convert';
import 'package:http/http.dart' as http;

class CollectionsPage extends StatefulWidget {
  final String username;
  final String password;
  final bool isTokenAuth;
  final String token;

  const CollectionsPage({Key? key, required this.username, required this.password, required this.isTokenAuth, this.token = ""}) : super(key: key);

  @override
  _CollectionsPageState createState() => _CollectionsPageState();
}

class _CollectionsPageState extends State<CollectionsPage> {
  late Future<List<Tag>> futureTags;

  @override
  void initState() {
    super.initState();
    futureTags = fetchTags();
  }

  Future<List<Tag>> fetchTags() async {
    bool isTokenAuth = widget.isTokenAuth;

    final uriString = isTokenAuth
        ? 'http://10.0.2.2:8080/token/api/tags'
        : 'http://10.0.2.2:8080/basic-auth/api/tags';
    final uri = Uri.parse(uriString);

    final headers = isTokenAuth
        ? {'Authorization': 'Bearer ${widget.token}'}
        : {'Authorization': 'Basic ${base64Encode(utf8.encode('${widget.username}:${widget.password}'))}'};

    final response = await http.get(uri, headers: headers);

    if (response.statusCode == 200) {
      List jsonResponse = json.decode(response.body);
      return jsonResponse.map((tag) => Tag.fromJson(tag)).toList();
    } else {
      throw Exception('Failed to load Tags from API');
    }
  }

  bool currentUserCanEditTag(Tag tag, String currentUser) {
    for (var access in tag.userAccess) {
      if (access.username == currentUser) {
        return access.canEdit;
      }
    }
    return tag.creator == currentUser || false;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Collections'),
        leading: IconButton(
          icon: Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: FutureBuilder<List<Tag>>(
        future: futureTags,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Text("Error: ${snapshot.error}");
          } else if (snapshot.hasData && snapshot.data!.isNotEmpty) {
            return ListView.builder(
              itemCount: snapshot.data!.length,
              itemBuilder: (context, index) {
                Tag currentTag = snapshot.data![index];
                bool canEdit = currentUserCanEditTag(currentTag, widget.username);
                return ListTile(
                  title: Text(currentTag.tagName),
                  subtitle: Text("Creator: ${currentTag.creator}"),
                  trailing: canEdit || !widget.isTokenAuth
                      ? IconButton(
                          icon: const Icon(Icons.share),
                          onPressed: () => _showShareTagDialog(currentTag),
                        )
                      : null,
                );
              },
            );
          } else {
            return const Center(
              child: Text('No collection found.'),
            );
          }
        },
      ),
    );
  }


  void _showShareTagDialog(Tag tag) {
    final TextEditingController _usernameController = TextEditingController();
    bool _canEdit = false;

    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Share Tag'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: _usernameController,
                decoration: const InputDecoration(hintText: "Username"),
              ),
              Row(
                children: [
                  const Text('Can modify:'),
                  Checkbox(
                    value: _canEdit,
                    onChanged: (bool? value) {
                      setState(() {
                        _canEdit = value!;
                      });
                    },
                  ),
                ],
              ),
            ],
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Cancel'),
              onPressed: () => Navigator.of(context).pop(),
            ),
            TextButton(
              child: const Text('Share'),
              onPressed: () {
                _shareTag(tag, _usernameController.text, _canEdit);
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  Future<void> _shareTag(Tag tag, String username, bool canEdit) async {
    final uri = Uri.parse('http://10.0.2.2:8080/basic-auth/api/tags/${tag.tagName}');
    final credentials = base64Encode(utf8.encode('${widget.username}:${widget.password}'));
    final uriCheckUser = Uri.parse('http://10.0.2.2:8080/basic-auth/api/users/$username');

    final checkUserResponse = await http.get(uriCheckUser, headers: {
      'Authorization': 'Basic ${base64Encode(utf8.encode('admin:admin'))}',
    });

    if (checkUserResponse.statusCode != 200) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('erreur lors de la récupération des informations du user')));
      return;
    }

    final currentTagResponse = await http.get(uri, headers: {
      'Authorization': 'Basic $credentials',
    });

    if (currentTagResponse.statusCode != 200) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Erreur lors de la récupération des informations de la Tag')));
      return;
    }

    final currentTagData = jsonDecode(currentTagResponse.body);
    List<dynamic> currentUserAccess = currentTagData['userAccess'] as List<dynamic>;

    currentUserAccess.add({
      "username": username,
      "canEdit": canEdit
    });

    final response = await http.put(
      uri,
      headers: {
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Basic $credentials',
      },
      body: jsonEncode({
        "userAccess": currentUserAccess
      }),
    );

    if (response.statusCode == 200) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Tag partagé avec succès !')));
    } else {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Erreur lors du partage du tag')));
    }
  }
}

import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_osm_plugin/flutter_osm_plugin.dart';
import 'package:hermesclient/classes/collection.dart';
import 'package:hermesclient/pages/map.dart';
import 'package:hermesclient/pages/sign_in_page.dart';
import 'package:hermesclient/pages/view_profile_page.dart';
import 'package:hermesclient/util/form_validations.dart';
import 'package:http/http.dart' as http;
import 'dart:math';
import 'package:hermesclient/classes/place.dart';
import 'package:hermesclient/pages/place_details_page.dart';
import 'package:hermesclient/pages/view_collections.dart';
import 'package:hermesclient/styles/styles.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';

class HomePage extends StatefulWidget {
  final String username;
  final String password;
  final String token;
  final bool isTokenAuth;

  const HomePage({super.key, required this.username, required this.password, required this.isTokenAuth, this.token = ""});

  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  late Future<List<Place>> futurePlaces;
  List<Collection> collections = [];
  bool isMapView = false;
  final TextEditingController searchController = TextEditingController();
  XFile? _selectedImage;

  Future<void> fetchCollections() async {
    bool isTokenAuth = widget.isTokenAuth;

    final uriString = isTokenAuth
        ? 'http://10.0.2.2:8080/token/api/collections'
        : 'http://10.0.2.2:8080/basic-auth/api/collections';
    final uri = Uri.parse(uriString);

    final headers = isTokenAuth
        ? {'Authorization': 'Bearer ${widget.token}'}
        : {'Authorization': 'Basic ${base64Encode(utf8.encode('${widget.username}:${widget.password}'))}'};

    final response = await http.get(uri, headers: headers);

    if (response.statusCode == 200) {
      final List<dynamic> collectionJson = json.decode(response.body);
      setState(() {
        collections = collectionJson
            .map((json) => Collection(name: json.toString()))
            .toList();
      });
    } else {
      throw Exception('Failed to load collections from API');
    }
  }

  @override
  void initState() {
    super.initState();
    futurePlaces = fetchPlaces();
    fetchCollections();
  }

  Future<List<Place>> fetchPlaces() async {
    bool isTokenAuth = widget.isTokenAuth;

    String searchString = searchController.text;
    List<String> tagsList = [];
    String tagsListString = tagsList.join(',');

    Map<String, String> queryParams = {
      'searchString': searchString,
      'tagsList': tagsListString,
    };

    var uri = isTokenAuth
        ? Uri.http('10.0.2.2:8080', '/token/api/places', queryParams)
        : Uri.http('10.0.2.2:8080', '/basic-auth/api/places', queryParams);

    final headers = isTokenAuth
        ? {'Authorization': 'Bearer ${widget.token}'}
        : {'Authorization': 'Basic ${base64Encode(utf8.encode('${widget.username}:${widget.password}'))}'};

    final response = await http.get(uri, headers: headers);

    if (response.statusCode == 200) {
      List jsonResponse = json.decode(response.body);
      return jsonResponse.map<Place>((place) => Place.fromJson(place)).toList();
    } else {
      throw Exception('Failed to load places from API');
    }
  }

  Future<void> fetchPlacesByTag(String tagName) async {
    bool isTokenAuth = widget.isTokenAuth;
    final uriString = isTokenAuth
        ? 'http://10.0.2.2:8080/token/api/collections/$tagName'
        : 'http://10.0.2.2:8080/basic-auth/api/collections/$tagName';
    final uri = Uri.parse(uriString);

    final headers = isTokenAuth
        ? {'Authorization': 'Bearer ${widget.token}'}
        : {'Authorization': 'Basic ${base64Encode(utf8.encode('${widget.username}:${widget.password}'))}'};

    final response = await http.get(uri, headers: headers);

    if (response.statusCode == 200) {
      final Map<String, dynamic> jsonResponse = json.decode(response.body);
      final List<dynamic> placesJson = jsonResponse['places'];

      setState(() {
        futurePlaces = Future.value(
            placesJson.map((place) => Place.fromJson(place)).toList());
      });
    } else {
      throw Exception('Failed to load places from API');
    }
  }

  @override
  Widget build(BuildContext context) {
    const double tagsHeight = 50;
    return Scaffold(
      appBar: AppBar(
        title: const Center(
          child: Text(
            'Hermes',
            style: TextStyle(color: Colors.white),
          ),
        ),
        backgroundColor: primaryBlue,
        iconTheme: const IconThemeData(
          color: Colors.white,
        ),
        actions: [
          IconButton(
            icon: Icon(isMapView ? Icons.list : Icons.map),
            onPressed: () {
              setState(() {
                isMapView = !isMapView;
              });
            },
          ),
        ],
      ),
      drawer: Drawer(
        child: Column(
          children: <Widget>[
            Expanded(
              child: ListView(
                padding: EdgeInsets.zero,
                children: const <Widget>[
                  DrawerHeader(
                    decoration: gradientBackground,
                    child: Text(
                      'Menu',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 24,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const Divider(),
            ListTile(
              leading: const Icon(
                Icons.collections_bookmark,
                color: primaryGreen,
              ),
              title: const Text(
                'My Collections',
                style:
                    TextStyle(color: primaryBlue, fontWeight: FontWeight.bold),
              ),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(
                      builder: (context) => CollectionsPage(
                            username: widget.username,
                            password: widget.password,
                            isTokenAuth: widget.isTokenAuth,
                            token: widget.token,
                          )),
                );
              },
            ),
            if (!widget.isTokenAuth)
            ListTile(
              leading: const Icon(
                Icons.add_link_rounded,
                color: primaryGreen,
              ),
              title: const Text(
                'Create Token',
                style:
                    TextStyle(color: primaryBlue, fontWeight: FontWeight.bold),
              ),
              onTap: () {
                _createToken(context);
              },
            ),
            ListTile(
              leading: const Icon(
                Icons.person,
                color: primaryGreen,
              ),
              title: const Text(
                'Profile',
                style:
                    TextStyle(color: primaryBlue, fontWeight: FontWeight.bold),
              ),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(
                      builder: (context) => ViewProfilePage(
                            username: widget.username,
                            password: widget.password,
                          )),
                );
              },
            ),
            ListTile(
              leading: const Icon(
                Icons.logout,
                color: Colors.blueGrey,
              ),
              title: const Text(
                'Sign Out',
                style: TextStyle(
                  color: Colors.blueGrey,
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
              ),
              onTap: () {
                Navigator.of(context).pushReplacement(MaterialPageRoute(
                    builder: (context) => const SignInPage()));
              },
            ),
          ],
        ),
      ),
      body: Column(
        children: [
          const SizedBox(height: 20),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 8.0),
              child: TextField(
                controller: searchController,
                decoration: const InputDecoration(
                  labelText: 'Search Places',
                  hintText: 'Enter a place name or description',
                  prefixIcon: Icon(Icons.search),
                  contentPadding: EdgeInsets.symmetric(vertical: 10.0),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(25.0)),
                  ),
                ),
                onSubmitted: (String value) {
                  setState(() {
                    futurePlaces = fetchPlaces();
                  });
                },
              ),
            ),
          ),
          SizedBox(
            height: tagsHeight,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: collections.length,
              itemBuilder: (context, index) {
                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 8.0),
                  child: Theme(
                    data: Theme.of(context).copyWith(
                      chipTheme: Theme.of(context).chipTheme.copyWith(
                            backgroundColor: Colors.grey[300],
                            side: const BorderSide(color: Colors.transparent),
                            shape: const RoundedRectangleBorder(
                              borderRadius: BorderRadius.zero,
                            ),
                            labelStyle: const TextStyle(
                              color: Colors.black,
                              fontStyle: FontStyle.italic,
                              fontSize: 12,
                            ),
                          ),
                    ),
                    child: ActionChip(
                      label: Text(collections[index].name),
                      onPressed: () {
                        fetchPlacesByTag(collections[index].name);
                      },
                    ),
                  ),
                );
              },
            ),
          ),
          Expanded(
            child: isMapView ? _buildMapView(context) : _buildListView(context),
          ),
        ],
      ),
      floatingActionButton: widget.isTokenAuth == false ? FloatingActionButton(
        onPressed: () {
          _showAddPlaceDialog(context);
        },
        backgroundColor: primaryGreen,
        child: const Icon(Icons.add),
      ) : null,
    );
  }

  Widget _buildListView(BuildContext context) {
    return FutureBuilder<List<Place>>(
      future: futurePlaces,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        } else if (snapshot.hasError) {
          return Text('Error: ${snapshot.error}');
        } else if (snapshot.hasData && snapshot.data!.isNotEmpty) {
          return ListView(
            padding: const EdgeInsets.all(16.0),
            children: snapshot.data!
                .map((place) => _buildCard(context, place))
                .toList(),
          );
        } else {
          return const Center(
            child: Text(
              'No place found, please add new places.',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 16.0,
                color: Colors.grey,
              ),
            ),
          );
        }
      },
    );
  }

  Widget _buildCard(BuildContext context, Place place) {
    final int starCount = Random().nextInt(5) + 1;
    return Card(
      elevation: 4.0,
      child: InkWell(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => CardDetailsPage(
                place: place,
                starCount: starCount,
                username: widget.username,
                password: widget.password,
                onPlaceUpdated: () {
                  setState(() {
                    futurePlaces = fetchPlaces();
                  });
              }, isTokenAuth: widget.isTokenAuth, token: widget.token,),
            ),
          ).then((_) {
            fetchPlaces();
          });
        },
        onLongPress: () {
          showDialog(
            context: context,
            builder: (BuildContext context) {
              return AlertDialog(
                title: const Text('Confirm'),
                content:
                    const Text('Are you sure you want to delete this place?'),
                actions: <Widget>[
                  TextButton(
                    onPressed: () => Navigator.of(context).pop(),
                    child: const Text('Cancel'),
                  ),
                  TextButton(
                    onPressed: () {
                      deletePlace(place.id);
                      Navigator.of(context).pop();
                    },
                    child: const Text('Delete'),
                  ),
                ],
              );
            },
          );
        },
        child: ListTile(
          title: Text(place.name),
          subtitle: Text(place.description),
        ),
      ),
    );
  }

  void _showAddPlaceDialog(BuildContext context) {
    final formKey = GlobalKey<FormState>();
    String title = '';
    String description = '';
    String latitude = '';
    String longitude = '';
    String tag = 'all';

    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text("Create a new place"),
          content: Form(
            key: formKey,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  TextFormField(
                    decoration: const InputDecoration(labelText: 'Title'),
                    validator: (value) {
                      if (value == null || value.isEmpty) {
                        return 'Please insert a title';
                      }
                      return null;
                    },
                    onSaved: (value) {
                      title = value ?? '';
                    },
                  ),
                  TextFormField(
                    decoration: const InputDecoration(labelText: 'Description'),
                    onSaved: (value) {
                      description = value ?? '';
                    },
                  ),
                  TextFormField(
                    decoration: const InputDecoration(labelText: 'Latitude'),
                    keyboardType:
                        const TextInputType.numberWithOptions(decimal: true),
                    validator: (value) =>
                        FormValidations.validateLatitude(value),
                    onSaved: (value) {
                      latitude = value!;
                    },
                  ),
                  TextFormField(
                    decoration: const InputDecoration(labelText: 'Longitude'),
                    keyboardType:
                        const TextInputType.numberWithOptions(decimal: true),
                    validator: (value) =>
                        FormValidations.validateLongitude(value),
                    onSaved: (value) {
                      longitude = value!;
                    },
                  ),
                  TextFormField(
                    decoration:
                        const InputDecoration(labelText: 'Tag (optional)'),
                    onSaved: (value) {
                      tag = value ?? 'all';
                    },
                  ),
                ],
              ),
            ),
          ),
          actions: <Widget>[
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Cancel'),
            ),
            TextButton(
              onPressed: () {
                if (formKey.currentState!.validate()) {
                  formKey.currentState!.save();
                  _createPlace(title, description, latitude, longitude, tag);
                  Navigator.of(context).pop();
                }
              },
              child: const Text('Add'),
            ),
          ],
        );
      },
    );
  }

  Future<void> _createPlace(String title, String description, String latitude,
      String longitude, String tag) async {
    final uri = Uri.parse('http://10.0.2.2:8080/basic-auth/api/places');

    final headers = {
      'Content-Type': 'application/json',
      'Authorization':
          'Basic ${base64Encode(utf8.encode('${widget.username}:${widget.password}'))}',
    };

    String toIso8601WithMillis(DateTime dateTime) {
      String date = dateTime.toIso8601String();
      int millis = dateTime.millisecond;
      return "${date.split('.')[0]}.${millis.toString().padLeft(3, '0')}Z";
    }

    final body = jsonEncode({
      "name": title,
      "description": description,
      "latitude": double.tryParse(latitude),
      "longitude": double.tryParse(longitude),
      "creator": widget.username,
      "userAccess": [
        {"username": widget.username, "canEdit": true}
      ],
      "tags": [tag],
      "updateTimestamp": toIso8601WithMillis(DateTime.now())
    });

    try {
      final response = await http.post(uri, headers: headers, body: body);
      if (response.statusCode == 200 || response.statusCode == 201) {
        final placeId = json.decode(response.body)['id'];
        if (_selectedImage != null) {
          await uploadImage(placeId);
        } else {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
              content: Text('Place created successfully without image.')));
        }
        ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Place created successfully!')));
        setState(() {
          futurePlaces =
              fetchPlaces();
          fetchCollections();
        });
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Error creating place: ${response.body}')));
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error connecting to server: $e')));
    }
  }

  Future<void> uploadImage(String id) async {
    final uri =
        Uri.parse('http://10.0.2.2:8080/basic-auth/api/places/$id/image');

    var request = http.MultipartRequest('POST', uri)
      ..headers.addAll({
        'Authorization':
            'Basic ${base64Encode(utf8.encode('${widget.username}:${widget.password}'))}',
      });

    if (_selectedImage != null) {
      request.files.add(
          await http.MultipartFile.fromPath('image', _selectedImage!.path));
    } else {
      const Text('Error: No image selected for upload');
      return;
    }

    try {
      var response = await request.send();
      final respStr = await response.stream.bytesToString();
      Text('Server responded with status code: ${response.statusCode}');
      Text('Response body: $respStr');

      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Image uploaded successfully!')));
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Failed to upload image: $respStr')));
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error during image upload: $e')));
    }
  }

  Future<void> deletePlace(String id) async {
    final uri = Uri.parse('http://10.0.2.2:8080/basic-auth/api/places/$id');
    final headers = {
      'Authorization':
          'Basic ${base64Encode(utf8.encode('${widget.username}:${widget.password}'))}',
    };

    final response = await http.delete(uri, headers: headers);

    if (response.statusCode == 200) {
      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Place deleted successfully!')));
      setState(() {
        futurePlaces = fetchPlaces();
      });
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Error while deleting a place')));
    }
  }

  Future<XFile?> pickImage() async {
    final ImagePicker _picker = ImagePicker();
    final XFile? image = await _picker.pickImage(source: ImageSource.gallery);
    return image;
  }

  Widget _buildMapView(BuildContext context) {
    return FutureBuilder<List<Place>>(
      future: futurePlaces,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        } else if (snapshot.hasError) {
          return Text('Error: ${snapshot.error}');
        } else if (snapshot.hasData) {
          List<GeoPoint> points = snapshot.data!
              .map((place) => GeoPoint(
                  latitude: place.latitude, longitude: place.longitude))
              .toList();

          return MapScreen(points: points);
        } else {
          return const Text('No place found.');
        }
      },
    );
  }

  String generateRandomUsername() {
    var now = DateTime.now();
    var random = Random().nextInt(999);
    return 'user_${now.microsecondsSinceEpoch}_$random'.toString();
  }

  String? userLink;

  Future<void> _createToken(BuildContext context) async {
    final uri = Uri.parse('http://10.0.2.2:8080/basic-auth/api/users');
    final credentials =
        base64Encode(utf8.encode('${widget.username}:${widget.password}'));
    final username = generateRandomUsername();
    final response = await http.post(
      uri,
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Basic $credentials',
      },
      body: jsonEncode(<String, dynamic>{
        "username": username,
        "password": "password",
        "isToken": true,
        "tokenExpiryDate": "2025-03-19T20:06:29.623Z",
      }),
    );

    if (response.statusCode == 200 || response.statusCode == 201) {
      final data = jsonDecode(response.body);
      final token = data['href'];

      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (BuildContext context) {
          return AlertDialog(
            title: const Text('Token created successfully'),
            content: SingleChildScrollView(
              child: ListBody(
                children: <Widget>[
                  Container(
                    padding: const EdgeInsets.all(8.0),
                    color: Colors.grey[200],
                    child: Row(
                      children: [
                        Expanded(child: Text('Token: $username')),
                        IconButton(
                          icon: const Icon(Icons.copy),
                          onPressed: () {
                            Clipboard.setData(ClipboardData(text: username));
                            ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text('Token copied')));
                          },
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 8.0),
                  Container(
                    padding: const EdgeInsets.all(8.0),
                    color: Colors.grey[200],
                    child: Row(
                      children: [
                        Expanded(child: Text('Link: $token')),
                        IconButton(
                          icon: const Icon(Icons.copy),
                          onPressed: () {
                            Clipboard.setData(ClipboardData(text: token));
                            ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text('Link copied')));
                          },
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            actions: <Widget>[
              TextButton(
                child: const Text('Close'),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              ),
            ],
          );
        },
      );
    } else {
      // Affiche une erreur si la création du token échoue
      showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: const Text('Error'),
            content: const SingleChildScrollView(
              child: ListBody(
                children: <Widget>[
                  Text('Error while creating token.'),
                ],
              ),
            ),
            actions: <Widget>[
              TextButton(
                child: const Text('Close'),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              ),
            ],
          );
        },
      );
    }
  }
}

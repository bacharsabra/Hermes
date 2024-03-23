import 'dart:async';
import 'dart:convert';
import 'package:esys_flutter_share_plus/esys_flutter_share_plus.dart';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter_osm_plugin/flutter_osm_plugin.dart';
import 'package:hermesclient/classes/place.dart';
import 'package:hermesclient/pages/map.dart';
import 'package:hermesclient/styles/styles.dart';
import 'package:http/http.dart' as http;
import 'package:url_launcher/url_launcher.dart';

class CardDetailsPage extends StatefulWidget {
  final Place place;
  final int starCount;
  final String username;
  final String password;
  final VoidCallback onPlaceUpdated;
  final bool isTokenAuth;
  final String token;

  const CardDetailsPage(
      {super.key,
      required this.place,
      required this.starCount,
      required this.username,
      required this.password,
      required this.onPlaceUpdated,
      required this.isTokenAuth,
      required this.token});

  @override
  _CardDetailsPageState createState() => _CardDetailsPageState();
}

class _CardDetailsPageState extends State<CardDetailsPage> {
  List<GeoPoint> points = [];
  String feedbackMessage = '';

  @override
  Widget build(BuildContext context) {
    bool canCurrentUserEdit = widget.place.creator == widget.username ||
        (widget.place.userAccess?.any((userAccess) =>
                userAccess.username == widget.username && userAccess.canEdit) ??
            false);
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          "Place Details",
          style: TextStyle(color: Colors.white),
        ),
        backgroundColor: primaryBlue,
        iconTheme: const IconThemeData(
          color: Colors.white,
        ),
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Card(
                clipBehavior: Clip.antiAlias,
                elevation: 4.0,
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Expanded(
                            child: Text(
                              widget.place.name,
                              style: const TextStyle(
                                  fontSize: 24.0, fontWeight: FontWeight.bold),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                          if (canCurrentUserEdit && !widget.isTokenAuth)
                            IconButton(
                              icon: const Icon(Icons.edit_location_alt_rounded,
                                  color: primaryGreen),
                              onPressed: () => _showEditPlaceDialog(context),
                            ),
                          if (canCurrentUserEdit && !widget.isTokenAuth)
                            IconButton(
                              icon:
                                  const Icon(Icons.share, color: primaryGreen),
                              onPressed: () => _showShareDialog(context),
                            ),
                          IconButton(
                            icon: const Icon(Icons.send, color: primaryGreen),
                            onPressed:
                                _sharePlace,
                          ),
                        ],
                      ),
                      Text(
                          style: const TextStyle(
                            fontStyle: FontStyle.italic,
                          ),
                          widget.place.description),
                      const SizedBox(height: 16.0),
                      Row(
                        children: [
                          const Icon(Icons.location_on, color: primaryBlue),
                          const SizedBox(width: 8.0),
                          Text("Latitude: ${widget.place.latitude}"),
                        ],
                      ),
                      const SizedBox(height: 8.0),
                      Row(
                        children: [
                          const Icon(Icons.location_on, color: primaryBlue),
                          const SizedBox(width: 8.0),
                          Text("Longitude: ${widget.place.longitude}"),
                        ],
                      ),
                      const SizedBox(height: 8.0),
                      Row(
                        children: [
                          const Icon(Icons.person, color: primaryBlue),
                          const SizedBox(width: 8.0),
                          Text("Creator: ${widget.place.creator}"),
                        ],
                      ),
                      if (feedbackMessage.isNotEmpty)
                        Padding(
                          padding: const EdgeInsets.all(8.0),
                          child: Text(feedbackMessage,
                              style: TextStyle(
                                fontSize: 16.0,
                                color: feedbackMessage.contains('successfully')
                                    ? Colors.green
                                    : Colors.red,
                              )),
                        ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16.0),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  ElevatedButton(
                    onPressed: () => openPlaceWithExternalMap(widget.place),
                    style: ElevatedButton.styleFrom(
                      foregroundColor: Colors.white,
                      backgroundColor: primaryBlue,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(18.0),
                      ),
                    ),
                    child: const Text('Open In Google App'),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => MapScreen(
                            points: [
                              GeoPoint(
                                  latitude: widget.place.latitude,
                                  longitude: widget.place.longitude),
                            ],
                          ),
                        ),
                      );
                    },
                    style: ElevatedButton.styleFrom(
                      foregroundColor: Colors.white,
                      backgroundColor: primaryGreen,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(18.0),
                      ),
                    ),
                    child: const Text('View on map'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _showShareDialog(BuildContext context) {
    final TextEditingController usernameController = TextEditingController();
    bool userCanEdit = false;

    showDialog(
      context: context,
      builder: (BuildContext context) {
        return StatefulBuilder(
          builder: (context, setState) {
            return AlertDialog(
              title: const Text('Share place with user'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: usernameController,
                    decoration: const InputDecoration(hintText: "Username"),
                  ),
                  Row(
                    children: [
                      Checkbox(
                        value: userCanEdit,
                        onChanged: (bool? value) {
                          setState(() {
                            userCanEdit = value!;
                          });
                        },
                      ),
                      const Text('Can Modify'),
                    ],
                  ),
                ],
              ),
              actions: <Widget>[
                TextButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text('Cancel'),
                ),
                TextButton(
                  onPressed: () {
                    _sharePlaceWithUser(
                        usernameController.text, userCanEdit, context);
                    Navigator.of(context).pop();
                  },
                  child: const Text('Share'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  void _sharePlace() {
    final String placeDetails =
        'Discover this place : ${widget.place.name}, located in latitude ${widget.place.latitude} and longitude ${widget.place.longitude}.';
    Share.text('Share', placeDetails, 'text/plain');
  }

  String toIso8601WithMillis(DateTime dateTime) {
    String date = dateTime.toIso8601String();
    int millis = dateTime.millisecond;
    return "${date.split('.')[0]}.${millis.toString().padLeft(3, '0')}Z";
  }

  Future<void> _sharePlaceWithUser(
      String username, bool canEdit, BuildContext context) async {
    final String credentials =
        base64Encode(utf8.encode('${widget.username}:${widget.password}'));
    final uri = Uri.parse(
        'http://10.0.2.2:8080/basic-auth/api/places/${widget.place.id}');
    final uriCheckUser =
        Uri.parse('http://10.0.2.2:8080/basic-auth/api/users/$username');

    final checkUserResponse = await http.get(uriCheckUser, headers: {
      'Authorization': 'Basic ${base64Encode(utf8.encode('admin:admin'))}',
    });

    if (checkUserResponse.statusCode != 200) {
      setState(() {
        feedbackMessage = "This user doesn't exist.";
      });
      return;
    }

    final currentPlaceResponse = await http.get(uri, headers: {
      'Authorization': 'Basic $credentials',
    });

    if (currentPlaceResponse.statusCode != 200) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
          content: Text('Error while sharing place, please try again later.')));
      return;
    }

    final currentPlaceData = jsonDecode(currentPlaceResponse.body);
    List<dynamic> currentUserAccess =
        currentPlaceData['userAccess'] as List<dynamic>;

    currentUserAccess.add({"username": username, "canEdit": canEdit});

    final response = await http.put(
      uri,
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Basic $credentials',
      },
      body: jsonEncode(<String, dynamic>{
        "name": widget.place.name,
        "description": widget.place.description,
        "latitude": widget.place.latitude,
        "longitude": widget.place.longitude,
        "userAccess": currentUserAccess,
        "tags": [],
        "updateTimestamp": toIso8601WithMillis(DateTime.now())
      }),
    );

    if (response.statusCode == 200) {
      setState(() {
        feedbackMessage = 'Place shared successfully!';
      });
    } else {
      setState(() {
        feedbackMessage = 'Error while sharing place.';
      });
    }
  }

  Future<void> openPlaceWithExternalMap(Place place) async {
    final Uri googleMapsUri = Uri.parse(
        'https://www.google.com/maps/search/?api=1&query=${place.latitude},${place.longitude}');
    final Uri wazeUri = Uri.parse(
        'https://waze.com/ul?ll=${place.latitude},${place.longitude}&navigate=yes');

    if (await canLaunchUrl(googleMapsUri)) {
      await launchUrl(googleMapsUri);
    } else if (await canLaunchUrl(wazeUri)) {
      await launchUrl(wazeUri);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text(
                'Unable to open map. Please install Google Maps or Waze.')),
      );
    }
  }

  void _showEditPlaceDialog(BuildContext context) {
    final formKey = GlobalKey<FormState>();
    final nameController = TextEditingController(text: widget.place.name);
    final descriptionController =
        TextEditingController(text: widget.place.description);
    final latitudeController =
        TextEditingController(text: widget.place.latitude.toString());
    final longitudeController =
        TextEditingController(text: widget.place.longitude.toString());

    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Edit this place'),
          content: Form(
            key: formKey,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  TextFormField(
                    controller: nameController,
                    decoration: const InputDecoration(labelText: 'Name'),
                    validator: (value) {
                      if (value == null || value.isEmpty) {
                        return 'Name is required.';
                      }
                      return null;
                    },
                  ),
                  TextFormField(
                    controller: descriptionController,
                    decoration: const InputDecoration(labelText: 'Description'),
                  ),
                  TextFormField(
                    controller: latitudeController,
                    decoration: const InputDecoration(labelText: 'Latitude'),
                    keyboardType: TextInputType.number,
                    validator: (value) {
                      if (value == null ||
                          value.isEmpty ||
                          double.tryParse(value) == null) {
                        return 'A valide latitude is required.';
                      }
                      return null;
                    },
                  ),
                  TextFormField(
                    controller: longitudeController,
                    decoration: const InputDecoration(labelText: 'Longitude'),
                    keyboardType: TextInputType.number,
                    validator: (value) {
                      if (value == null ||
                          value.isEmpty ||
                          double.tryParse(value) == null) {
                        return 'A valide longitude is required.';
                      }
                      return null;
                    },
                  ),
                ],
              ),
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Cancel'),
              onPressed: () => Navigator.of(context).pop(),
            ),
            TextButton(
              child: const Text('Save'),
              onPressed: () {
                if (formKey.currentState!.validate()) {
                  updatePlace(
                      widget.place.id,
                      nameController.text,
                      descriptionController.text,
                      double.parse(latitudeController.text),
                      double.parse(longitudeController.text));
                  Navigator.of(context).pop();
                }
              },
            ),
          ],
        );
      },
    );
  }

  Future<void> updatePlace(String id, String name, String description,
      double latitude, double longitude) async {
    final credentials =
        base64Encode(utf8.encode('${widget.username}:${widget.password}'));
    final uri = Uri.parse('http://10.0.2.2:8080/basic-auth/api/places/$id');
    List<Map<String, dynamic>> userAccessJson = widget.place.userAccess!
        .map((userAccess) => userAccess.toJson())
        .toList();
    final response = await http.put(
      uri,
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Basic $credentials',
      },
      body: jsonEncode({
        "name": name,
        "description": description,
        "latitude": latitude,
        "longitude": longitude,
        "tags": [],
        "userAccess": userAccessJson,
        "updateTimestamp": DateTime.now().toIso8601String()
      }),
    );

    if (response.statusCode == 200) {
      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Place updated successfully.')));
      widget.onPlaceUpdated();
      setState(() {
        widget.place.name = name;
        widget.place.description = description;
        widget.place.latitude = latitude;
        widget.place.longitude = longitude;
      });
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Error while updating place.')));
    }
  }

  Future<Uint8List> fetchImageData(String id) async {
    final uri =
        Uri.parse('http://10.0.2.2:8080/basic-auth/api/places/$id/image');
    final response = await http.get(uri, headers: {
      'Authorization':
          'Basic ${base64Encode(utf8.encode('${widget.username}:${widget.password}'))}',
    });

    if (response.statusCode == 200) {
      return response.bodyBytes;
    } else {
      throw Exception('Failed to load image data');
    }
  }
}

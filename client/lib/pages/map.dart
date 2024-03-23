import 'package:flutter/material.dart';
import 'package:flutter_osm_plugin/flutter_osm_plugin.dart';

class MapPage extends StatelessWidget {
  final List<GeoPoint> points;

  const MapPage({super.key, required this.points});

  @override
  Widget build(BuildContext context) {
    return MapScreen(points: points);
  }
}

class MapScreen extends StatefulWidget {
  final List<GeoPoint> points;

  const MapScreen({super.key, required this.points});

  @override
  _MapScreenState createState() => _MapScreenState();
}
class _MapScreenState extends State<MapScreen> {
  late MapController mapController;

  @override
  void initState() {
    super.initState();
    final centerPoint = calculateCenter(widget.points);
    mapController = MapController.customLayer(
      initPosition: GeoPoint(
        latitude: centerPoint.latitude,
        longitude: centerPoint.longitude,
      ),
      customTile: CustomTile(
        sourceName: "openstreetmap",
        tileExtension: ".png",
        minZoomLevel: 2,
        maxZoomLevel: 14,
        urlsServers: [TileURLs(url: "https://{s}.tile.openstreetmap.org/", subdomains: ['a', 'b', 'c'])],
        tileSize: 256,
      ),
    );

    Future.delayed(const Duration(seconds: 1), () async {
      for (var point in widget.points) {
        await mapController.addMarker(point, markerIcon: const MarkerIcon(icon: Icon(Icons.location_on)));
      }
      await mapController.setZoom(zoomLevel: 2);
    });
  }

  @override
  void dispose() {
    mapController.dispose();
    super.dispose();
  }

  Future<void> zoomIn() async {
    await mapController.zoomIn();
  }

  Future<void> zoomOut() async {
    await mapController.zoomOut();
  }

  Future<void> updateLocation() async {
    for (var point in widget.points) {
      GeoPoint newLocation = GeoPoint(latitude: point.latitude, longitude: point.longitude);
      await mapController.changeLocation(newLocation);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Map"),
      ),
      body: OSMFlutter(
        controller: mapController,
        osmOption: const OSMOption(
          userTrackingOption: UserTrackingOption(
            enableTracking: true,
            unFollowUser: false,
          ),
          zoomOption: ZoomOption(
            initZoom: 8.0,
            minZoomLevel: 2,
            maxZoomLevel: 19,
            stepZoom: 1.0,
          ),
        ),
      ),
      floatingActionButton: Column(
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          FloatingActionButton(
            onPressed: zoomIn,
            heroTag: "zoomIn",
            child: const Icon(Icons.zoom_in),
          ),
          const SizedBox(height: 10),
          FloatingActionButton(
            onPressed: zoomOut,
            heroTag: "zoomOut",
            child: const Icon(Icons.zoom_out),
          ),
          const SizedBox(height: 10),
          FloatingActionButton(
            onPressed: updateLocation,
            heroTag: "updateLocation",
            child: const Icon(Icons.location_on),
          ),
        ],
      ),
    );
  }

  GeoPoint calculateCenter(List<GeoPoint> points) {
    double averageLat = 0.0;
    double averageLon = 0.0;
    for (var point in points) {
      averageLat += point.latitude;
      averageLon += point.longitude;
    }
    averageLat /= points.length;
    averageLon /= points.length;
    return GeoPoint(latitude: averageLat, longitude: averageLon);
  }
}

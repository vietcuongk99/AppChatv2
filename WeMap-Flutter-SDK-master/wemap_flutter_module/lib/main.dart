

import 'package:flutter/material.dart';
import 'package:location/location.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:wemapgl/wemapgl.dart';
import 'full_map.dart';
import 'map_ui.dart';
import 'ePage.dart';
import 'simpleDirection.dart';
import 'package:wemapgl/wemapgl.dart' as WEMAP;

class FullMapPage extends ePage {
  FullMapPage() : super(const Icon(Icons.map), 'Full screen map');

  @override
  Widget build(BuildContext context) {
    return const FullMap();
  }
}

class FullMap extends StatefulWidget {
  const FullMap();

  @override
  State createState() => FullMapState();
}

class FullMapState extends State<FullMap> {
  WeMapController mapController;
  int searchType = 1; //Type of search bar
  String searchInfoPlace = "Tìm kiếm ở đây"; //Hint text for InfoBar
  String searchPlaceName;
  LatLng myLatLng = LatLng(21.038282, 105.782885);
  bool reverse = true;
  WeMapPlace place;
  String _sharedString = "";
  double latitude = 0;
  double longitude = 0;

  void _onMapCreated(WeMapController controller) {
    mapController = controller;
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
        debugShowCheckedModeBanner: false,
        home: Scaffold(
          body: Stack(
            children: <Widget>[
              WeMap(
                  trackCameraPosition: true,
                  myLocationEnabled: true,
                  onMapClick: (point, latlng, _place) async {
                    place = await _place;
                  },
                  onPlaceCardClose: () {
                    // print("Place Card closed");
                  },
                  reverse: true,
                  onMapCreated: _onMapCreated,
                  initialCameraPosition: const CameraPosition(
                    target: LatLng(21.036029, 105.782950),
                    // target: LatLng(latitude, longitude),
                    zoom: 15.0,
                  ),
                  destinationIcon: "assets/symbols/destination.png",
                  onStyleLoadedCallback: onStyleLoadedCallback
              ),
              WeMapSearchBar(
                location: myLatLng,
                onSelected: (_place) {
                  setState(() {
                    place = _place;
                  });
                  mapController.moveCamera(
                    CameraUpdate.newCameraPosition(
                      CameraPosition(
                        target: place.location,
                        zoom: 18.0,
                      ),
                    ),
                  );
                  mapController.showPlaceCard(place);
                },
                onClearInput: () {
                  setState(() {
                    place = null;
                    mapController.showPlaceCard(place);
                  });
                },
              ),
            ],
          ),
          floatingActionButton: FloatingActionButton(
            onPressed: () {
              // Add your onPressed code here!
              mapController.moveCamera(
                  CameraUpdate.newCameraPosition(
                    CameraPosition(
                      target: LatLng(latitude, longitude),
                      zoom: 15.0,
                    ),
                  ));
            },
            child: const Icon(Icons.navigation),
            backgroundColor: Colors.green,
          ),
        )

    );
  }

  void initState() {
    super.initState();
  }

  void onStyleLoadedCallback() async {
    _sharedString = (await SharedPreferences.getInstance()).getString("test");
    List arr = _sharedString.split(",");

    setState(() {
      latitude = double.parse(arr[0]);
      longitude = double.parse(arr[1]);
    });

    await mapController.addCircle(CircleOptions(
        geometry: LatLng(latitude, longitude),
        circleRadius: 8.0,
        circleColor: '#d3d3d3',
        circleStrokeWidth: 1.5,
        circleStrokeColor: '#0071bc'));
    await mapController.moveCamera(
      CameraUpdate.newCameraPosition(
        CameraPosition(
          target: LatLng(latitude, longitude),
          zoom: 15.0,
        ),
      ),
    );
  }
}

void main() {
  WEMAP.Configuration.setWeMapKey('GqfwrZUEfxbwbnQUhtBMFivEysYIxelQ');
  runApp(FullMap());
}

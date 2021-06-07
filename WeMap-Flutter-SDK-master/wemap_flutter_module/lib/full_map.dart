import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:wemapgl/wemapgl.dart';
import 'ePage.dart';

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

  void _onMapCreated(WeMapController controller) {
    mapController = controller;
  }

  @override
  Widget build(BuildContext context) {
    Map<String, dynamic> destinationPoint = jsonDecode(_sharedString);
    LatLng location = new LatLng(destinationPoint['latitude'], destinationPoint['longitude']);
    return new Scaffold(
      body: Stack(
        children: <Widget>[
          Text(
            "\nDestination point is "+_sharedString + " $location"
          ),
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
            initialCameraPosition: CameraPosition(
              target: LatLng(21.036029, 105.782950),
              //target: LatLng(location.latitude, location.longitude),
              zoom: 14.0,
            ),
            destinationIcon: "assets/symbols/destination.png",
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
                    zoom: 14.0,
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
    );
  }

  void initState() {
    super.initState();
  }

  void onStyleLoadCallback() async {
    _sharedString = (await SharedPreferences.getInstance()).getString("test");
    // destinationPoint = jsonDecode(_sharedString);
}
}

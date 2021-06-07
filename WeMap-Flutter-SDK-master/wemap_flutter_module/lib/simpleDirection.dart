import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:wemapgl/wemapgl.dart';

import 'ePage.dart';

class SimpleDirectionPage extends ePage {
  SimpleDirectionPage() : super(const Icon(Icons.directions), 'Simple Multiple Direction');

  @override
  Widget build(BuildContext context) {
    return const DirectionAPI();
  }
}

class DirectionAPI extends StatefulWidget {
  const DirectionAPI();

  @override
  State createState() => DirectionAPIState();
}

class DirectionAPIState extends State<DirectionAPI> {
  WeMapDirections directionAPI = WeMapDirections();

  int _tripDistance = 0;
  int _tripTime = 0;


  String _sharedString = "";

  WeMapController mapController;

  @override
  Widget build(BuildContext context) {
    List arr = _sharedString.split(",");
    double latitude = double.parse(arr[0]);
    double longitude = double.parse(arr[1]);
    return MaterialApp(
        title: "Direction example",
        debugShowCheckedModeBanner: false,
        home: Scaffold(
            appBar: AppBar(
              title: Text("Direction example"),
            ),
            body: Column(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: <Widget>[
                Padding(
                  padding: EdgeInsets.only(bottom: 12.0),
                  child: Text(
                      "Trip distance: $_tripDistance m. Trip time estimate: $_tripTime ms" + "\nDestination point is "+_sharedString
                  + "\nLatitude: $latitude"),
                ),
                Center(
                  child: SizedBox(
                    width: 300.0,
                    height: 200.0,
                    child: WeMap(
                      onMapCreated: _onMapCreated,
                      onStyleLoadedCallback: onStyleLoadedCallback,
                      initialCameraPosition: const CameraPosition(
                        target: LatLng(21.017273, 105.798138),
                        zoom: 11.0,
                      ),
                    ),
                  ),
                ),  
              ],
            )));
  }

  void _onMapCreated(WeMapController mapController) {
    this.mapController = mapController;
  }

  @override
  void dispose() {
    super.dispose();
  }

  void onStyleLoadedCallback() async {
    List<LatLng> points = [];

    points.add(LatLng(21.036751,105.782013)); //origin Point
    points.add(LatLng(21.024412, 105.798115)); //way Point
    points.add(LatLng(21.004880,105.817432)); //destination Point

    final json = await directionAPI.getResponseMultiRoute(0, points); //0 = car, 1 = bike, 2 = foot
    _sharedString = (await SharedPreferences.getInstance()).getString("test");
    Map<String, dynamic> destinationPoint = jsonDecode(_sharedString);

    List<LatLng> _route = directionAPI.getRoute(json);
    List<LatLng> _waypoins = directionAPI.getWayPoints(json);

    setState(() {
      _tripDistance = directionAPI.getDistance(json);
      _tripTime = directionAPI.getTime(json);
    });

    if (_route != null) {
      await mapController.addLine(
        LineOptions(
          geometry: _route,
          lineColor: "#0071bc",
          lineWidth: 5.0,
          lineOpacity: 1,
        ),
      );
      await mapController.addCircle(CircleOptions(
          geometry: _waypoins[0],
          circleRadius: 8.0,
          circleColor: '#d3d3d3',
          circleStrokeWidth: 1.5,
          circleStrokeColor: '#0071bc'));
      for(int i=1; i<_waypoins.length; i++){
        await mapController.addCircle(CircleOptions(
            geometry: _waypoins[i],
            circleRadius: 8.0,
            circleColor: '#ffffff',
            circleStrokeWidth: 1.5,
            circleStrokeColor: '#0071bc'));
      }
    }
  }
}

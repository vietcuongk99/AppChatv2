

import 'package:flutter/material.dart';
import 'package:location/location.dart';
import 'full_map.dart';
import 'map_ui.dart';
import 'ePage.dart';
import 'simpleDirection.dart';
import 'package:wemapgl/wemapgl.dart' as WEMAP;

final List<ePage> _allPages = <ePage>[
  MapUiPage(),
  SimpleDirectionPage(),
  FullMapPage(),
];

class MapsDemo extends StatelessWidget {
  void _pushPage(BuildContext context, ePage page) async {
    final location = Location();
    final hasPermissions = await location.hasPermission();
    if (hasPermissions != PermissionStatus.GRANTED) {
      await location.requestPermission();
    }

    Navigator.of(context).push(MaterialPageRoute<void>(
        builder: (_) => Scaffold(
          body: page,
        )));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('WeMap examples')),
      body: ListView.builder(
        itemCount: _allPages.length,
        itemBuilder: (_, int index) => ListTile(
          leading: _allPages[index].leading,
          title: Text(_allPages[index].title),
          onTap: () => _pushPage(context, _allPages[index]),
        ),
      ),
    );
  }
}

void main() {
  WEMAP.Configuration.setWeMapKey('GqfwrZUEfxbwbnQUhtBMFivEysYIxelQ');
  runApp(MaterialApp(
      home: MapsDemo(),
      initialRoute: '/',
      routes: {
        '/map_ui': (context) => MapUiPage(),
        '/full_map': (context) => FullMap(),
        '/direction': (context) => SimpleDirectionPage(),
      }));

}

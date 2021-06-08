

import 'package:flutter/material.dart';

abstract class ePage extends StatelessWidget {
  const ePage(this.leading, this.title);

  final Widget leading;
  final String title;
}

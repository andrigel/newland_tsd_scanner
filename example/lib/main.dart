import 'package:flutter/material.dart';
import 'package:newland_tsd_scanner/newland_tsd_scanner.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _scanResults = 'Unknown';

  @override
  void initState() {
    NewlandTsdScanner.startListerning(onDetect: (code) {
      setState(() {
        _scanResults = code.barcode;
      });
    });
    super.initState();
  }

  @override
  void dispose() {
    NewlandTsdScanner.stopListerning();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Text('$_scanResults\n'),
            ],
          ),
        ),
      ),
    );
  }
}

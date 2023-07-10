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

  TextEditingController? c;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: TsdScannerWrapper(
          onDetect: (barcode) => setState(() {
            _scanResults = barcode;
          }),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              Center(
                child: Text('$_scanResults\n'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

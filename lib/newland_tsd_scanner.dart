import 'dart:async';
import 'newland_tsd_scanner_platform_interface.dart';

class NewlandTsdScanner {
  static Future<void> startListerning(
      {Function(TsdBarcode code)? onDetect}) async {
    await NewlandTsdScannerPlatform.instance
        .startListerning(onDetect: onDetect);
  }

  static Future<void> stopListerning() async {
    await NewlandTsdScannerPlatform.instance.stopListerning();
  }
}

class TsdBarcode {
  final String barcodeTypeName;
  final String barcode;
  final TsdBarcodeScanState state;

  TsdBarcode({
    required this.barcodeTypeName,
    required this.barcode,
    required this.state,
  });

  TsdBarcode.fromJson(Map<String, dynamic> data)
      : barcodeTypeName = data['SCAN_BARCODE_TYPE_NAME'],
        barcode = data['SCAN_BARCODE1'] ?? '',
        state = data['SCAN_STATE'] == 'ok'
            ? TsdBarcodeScanState.ok
            : TsdBarcodeScanState.fail;
}

enum TsdBarcodeScanState {
  ok,
  fail,
}

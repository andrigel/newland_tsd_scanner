import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'newland_tsd_scanner.dart';
import 'newland_tsd_scanner_method_channel.dart';

abstract class NewlandTsdScannerPlatform extends PlatformInterface {
  NewlandTsdScannerPlatform() : super(token: _token);

  static final Object _token = Object();

  static NewlandTsdScannerPlatform _instance = MethodChannelNewlandTsdScanner();

  static NewlandTsdScannerPlatform get instance => _instance;

  static set instance(NewlandTsdScannerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> startListerning({Function(TsdBarcode code)? onDetect}) {
    throw UnimplementedError('startListerning() has not been implemented.');
  }

  Future<void> stopListerning() {
    throw UnimplementedError('stopListerning() has not been implemented.');
  }
}

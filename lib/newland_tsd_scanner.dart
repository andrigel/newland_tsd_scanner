import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'newland_tsd_scanner_platform_interface.dart';

class NewlandTsdScanner {
  static Future<void> startScan() async {
    await NewlandTsdScannerPlatform.instance.startScan();
  }

  static Future<String?> stopScan() async {
    return NewlandTsdScannerPlatform.instance.stopScan();
  }
}

class TsdScannerWrapper extends StatefulWidget {
  final Widget child;
  final Function(String barcode) onDetect;

  const TsdScannerWrapper({
    super.key,
    required this.child,
    required this.onDetect,
  });

  @override
  State<TsdScannerWrapper> createState() => _TsdScannerWrapperState();
}

class _TsdScannerWrapperState extends State<TsdScannerWrapper> {
  final TextEditingController c = TextEditingController();
  final TextfieldFocusNode textfieldFocusNode = TextfieldFocusNode();

  @override
  void initState() {
    c.addListener(() {
      if (c.text.isNotEmpty) {
        widget.onDetect.call(c.text);
        c.clear();
      }
    });
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        widget.child,
        SizedBox.shrink(
          child: TextFieldWithNoKeyboard(
            controller: c,
            focus: textfieldFocusNode,
          ),
        )
      ],
    );
  }
}

class TextFieldWithNoKeyboard extends EditableText {
  TextFieldWithNoKeyboard({
    super.key,
    required TextEditingController controller,
    required TextfieldFocusNode focus,
    TextStyle style = const TextStyle(),
    Color cursorColor = Colors.transparent,
  }) : super(
          controller: controller,
          focusNode: focus,
          style: style,
          cursorColor: cursorColor,
          autofocus: true,
          backgroundCursorColor: Colors.black,
        );

  @override
  EditableTextState createState() {
    return TextFieldEditableState();
  }
}

class TextFieldEditableState extends EditableTextState {
  @override
  void requestKeyboard() {
    super.requestKeyboard();
    SystemChannels.textInput.invokeMethod('TextInput.hide');
  }
}

class TextfieldFocusNode extends FocusNode {
  @override
  bool consumeKeyboardToken() {
    return false;
  }
}

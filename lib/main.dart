import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:developer' as developer;

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'FlipIt',
      theme: ThemeData(
        primarySwatch: Colors.green,
      ),
      home: MyHomePage(title: 'FlipIt'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  File _image;
  static const platform = const MethodChannel('flutter.native/helper');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text(widget.title),
        ),
        body: _bodyContent(),
        floatingActionButton: FloatingActionButton(
          onPressed: getImage,
          tooltip: 'Add Image',
          child: Icon(Icons.add),
        ),
        bottomNavigationBar: BottomAppBar(
          child: new Row(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: <Widget>[
              IconButton(
                icon: Icon(Icons.swap_horiz),
                onPressed: () {
                  flip(true, false);
                },
                tooltip: "flip horizontal",
              ),
              IconButton(
                icon: Icon(Icons.swap_vert),
                tooltip: "flip vertical",
                onPressed: () {
                  flip(false, true);
                },
              ),
            ],
          ),
        ));
  }

  Future<void> flip(bool flipHorizontally, bool flipVertically) async {
    if (_image.path == "") {
      return;
    }
    Directory tempDir = await getTemporaryDirectory();
    String outputImagePath = tempDir.path + basename(_image.path);

    String response = "";
    try {
      final String result =
      await platform.invokeMethod('flip', <String, dynamic>{
        'inputFile': _image.path,
        'outputFile': outputImagePath,
        'flipHorizontally': flipHorizontally,
        'flipVertically': flipVertically,
      });
      response = result;
      setState(() {
        _image = new File(outputImagePath);
      });
    } on PlatformException catch (e) {
      response = "Failed to Invoke: '${e.message}'.";
    }

    developer.log(response, name: 'flipResponse');
  }

  Future getImage() async {
    var image = await ImagePicker.pickImage(source: ImageSource.gallery);

    setState(() {
      _image = image;
    });
  }

  Widget _bodyContent() {
    return Center(
      child: _image == null ? Text('No image selected.') : Image.file(_image),
    );
  }
}

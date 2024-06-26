import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Contacts Demo',
      home: ContactsPage(),
    );
  }
}

class ContactsPage extends StatefulWidget {
  @override
  _ContactsPageState createState() => _ContactsPageState();
}

class _ContactsPageState extends State<ContactsPage> {
  static const platform = MethodChannel('contactsChannel');

  List<dynamic> _contacts = [];

  @override
  void initState() {
    super.initState();
    _requestContactsPermission();
  }

  Future<void> _requestContactsPermission() async {
    if (await Permission.contacts.request().isGranted) {
      // Permission is granted, fetch contacts
      _fetchContacts();
    } else {
      // Permission is not granted, handle accordingly
      // Optionally show a message or disable functionality
    }
  }

  Future<void> _fetchContacts() async {
    List<dynamic> contacts;
    try {
      final List<dynamic> result = await platform.invokeMethod('getContacts');
      contacts = result;
    } on PlatformException catch (e) {
      print("Failed to fetch contacts: '${e.message}'.");
      contacts = [];
    }

    setState(() {
      _contacts = contacts;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Contacts Demo'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            ElevatedButton(
              onPressed: _requestContactsPermission,
              child: Text('Fetch Contacts'),
            ),
            Expanded(
              child: ListView.builder(
                itemCount: _contacts.length,
                itemBuilder: (context, index) {
                  return ListTile(
                    title: Text(_contacts[index]['name'] ?? ''),
                    subtitle: Text(_contacts[index]['phone'] ?? ''),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

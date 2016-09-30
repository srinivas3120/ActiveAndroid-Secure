# ActiveAndroid-Secure

ActiveAndroid-Secure is meant to be a working example of using ActiveAndroid with SQLCipher v3.1.0

For more info on ActiveAndroid please click [here](https://github.com/pardom/ActiveAndroid)

In order to use the library you should first set a password in the ```Cache.java``` file.

```java
public static synchronized SQLiteDatabase openDatabase() {
	return sDatabaseHelper.getWritableDatabase("PASSWORD-GOES-HERE");
}
```

then type ```ant``` to build your .jar file

## Documentation

* [Getting started](http://github.com/pardom/ActiveAndroid/wiki/Getting-started)
* [Creating your database model](http://github.com/pardom/ActiveAndroid/wiki/Creating-your-database-model)
* [Saving to the database](http://github.com/pardom/ActiveAndroid/wiki/Saving-to-the-database)
* [Querying the database](http://github.com/pardom/ActiveAndroid/wiki/Querying-the-database)
* [Type serializers](http://github.com/pardom/ActiveAndroid/wiki/Type-serializers)
* [Using the content provider](http://github.com/pardom/ActiveAndroid/wiki/Using-the-content-provider)
* [Schema migrations](http://github.com/pardom/ActiveAndroid/wiki/Schema-migrations)
* [Pre-populated-databases](http://github.com/pardom/ActiveAndroid/wiki/Pre-populated-databases)

## License

[Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

    Copyright (C) 2010 Michael Pardo

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## Contributing

Please fork this repository and contribute back using [pull requests](http://github.com/pardom/ActiveAndroid/pulls).

Any contributions, large or small, major features, bug fixes, unit tests are welcomed and appreciated but will be thoroughly reviewed and discussed.

## Author

Michael Pardo | www.michaelpardo.com | www.activeandroid.com

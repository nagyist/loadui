About
-----

> **NOTE:** Want to download LoadUI? Please visit [LoadUI.org](http://loadui.org).

LoadUI is a modern load-testing software built in Java 7. JavaFX is used to create the GUI and a Groovy DSL is used for extending LoadUI.


Building
---------

LoadUI is built by running `mvn clean install` from the root directory. This will compile and run all regular unit tests. To start LoadUI, use the `loadui.bat` (or Linux/Mac equivalent) file in `loadui-installers\loadui-controller-installer\target\main`.

Testing
-------

To run GUI unit tests, use the Maven profile `gui-tests`; to run GUI integration tests, use the profile `int-tests`.


Contributing
------------

We love contributions! Even though LoadUI is an open source project, for legal reasons you have to sign the [Contributor's Agreement](http://www.soapui.org/Developers-Corner/contribute-to-soapui.html) before we can incorporate any of your code in LoadUI.


Additional resources
--------------------
* [Technical Overview](http://www.loadui.org/Developers-Corner/technical-overview.html)
* [Creating Custom Components](http://www.loadui.org/Developers-Corner/custom-component-reference-new.html)

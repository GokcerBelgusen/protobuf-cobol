# Overview #

Protobuf-cobol is an implementation of protocol buffers for the COBOL language. For a complete description of protocol buffers refer to [Google's documentation](http://code.google.com/p/protobuf/).

Protobuf-cobol is primarily targeted at the IBM z/OS variant of the COBOL language.

COBOL has severe limitations as compared to modern languages. That makes it quite challenging to support all of protocol buffers features, so don't expect a 100% coverage.

We believe it is possible to support most of the important constructs though, and propose some workarounds for issues such as:
  * COBOL String are fixed size byte arrays
  * COBOL arrays must have a maximum number of items defined

This is early code, delivered in the hope that it will raise some interest. Please feel free to open issues on http://code.google.com/p/protobuf-cobol/issues/list, your comments are welcome.

This project is related to the [LegStar](http://code.google.com/p/legstar/) open source project and uses some of its artifacts.

The project also uses the [protobuf-java](http://protobuf.googlecode.com/svn/trunk/java/README.txt) implementation developed by Google.

# Usage #

## Pre requisites ##

The first pre-requisite is to have protocol buffers installed on your machine. To check that it is installed, type the following command in a command window:
```
protoc -h
```
If you get the protocol buffers help, you are fine.

The second pre-requisite is that you need a java JDK (not a JRE). The version must be 1.6 or above.

## Generate COBOL artifacts ##

To get started, download the latest zip file from http://code.google.com/p/protobuf-cobol/downloads/list and unzip to a location of your choice. This should create a folder called protobuf-cobol-${version}. We will refer to that folder as PROTOCOB\_HOME for the rest of this document.

Open a command window and change directory to PROTOCOB\_HOME.

There are run.bat and run.sh files in that folder. On unix, you might have to run
```
chmod +x run.sh
```

Executing this command on Windows:
```
run -h
```

or this one on Unix:
```
./run.sh -h
```

should display the commands available.

The PROTOCOB\_HOME/proto sub-folder contains the same addressbook.proto sample proto file Google uses in their documentation. This proto file describes a message called AddressBook which contains repeated occurrences of the Person message.

In order to generate the COBOL artifacts for that proto file, type this command on Windows:
```
run -i proto/addressbook.proto
```

or this one on Unix:
```
./run.sh -i proto/addressbook.proto
```

You should now have a sub folder called PROTOCOB\_HOME/cobol with the following content:

  * ADDRBOOP.cbl is a COBOL subprogram that parses the protocol buffer wire format for message AddressBook
  * ADDRBOOW.cbl is a COBOL subprogram that writes protocol buffer wire format for message AddressBook
  * AddressBookC.cpy is a COBOL copybook that matches the AddressBook message from the proto file

You also get the same artifacts for the inner message Person.

## Using the COBOL artifacts ##

If you look at the generated COBOL sub programs you will notice they call a library that provides basic conversion routines.

The library is part of this open source project and can be found as source code here: [PB4CBLIB](http://code.google.com/p/protobuf-cobol/source/browse/trunk/src/main/zos/COBOL/PB4CBLIB).

There is a sample compilation JCL that uses IBM z/OS Entreprise COBOL here: [COBPBCL](http://code.google.com/p/protobuf-cobol/source/browse/trunk/src/main/zos/CNTL/COBPBCL).

Once the library is compiled, you can in turn compile the generated artifacts (no special requirements there).

Finally, you can find an example of a simple COBOL program using the generated Parser and Writer sub programs here: [PBCTTUTO](http://code.google.com/p/protobuf-cobol/source/browse/trunk/src/it/zos/COBOL/PBCTTUTO).

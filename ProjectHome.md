# Objective #
An implementation of [Google's Protocol Buffers](http://code.google.com/p/protobuf/) for COBOL.

The idea is to provide a code generator that takes a protocol buffers [proto file](http://code.google.com/p/protobuf-cobol/source/browse/trunk/src/test/resources/proto/addressbook.proto) and produces a [COBOL parser](http://code.google.com/p/protobuf-cobol/source/browse/trunk/src/test/resources/reference/ProtoCobolTest/testAddressBookProtos/ADDRBOOP.cbl.txt) and a [COBOL writer](http://code.google.com/p/protobuf-cobol/source/browse/trunk/src/test/resources/reference/ProtoCobolTest/testAddressBookProtos/ADDRBOOW.cbl.txt) that would be able to convert the protocol buffers wire format to the content of a [COBOL structure](http://code.google.com/p/protobuf-cobol/source/browse/trunk/src/test/resources/reference/ProtoCobolTest/testAddressBookProtos/PersonC.cpy.txt).

Your own COBOL programs can then use the COBOL parser/writer to interoperate with all the [languages supporting protocol buffers](http://code.google.com/p/protobuf/wiki/ThirdPartyAddOns).

This project does not cover the "service" and "rpc" features of protocol buffers. For now the focus is on messaging and data serialization.
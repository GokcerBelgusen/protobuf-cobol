#! /bin/sh
##   ---------------------------------------------------------------------------
##   Translates Protocol buffers proto files to COBOL
##   ------------------------------------------------
##   type run -h to get help on available options
##   protoc.exe must be on the path (see http://code.google.com/p/protobuf/)
##   set JAVA_HOME to point to a JDK (not a JRE)
##   ---------------------------------------------------------------------------

if [ -z "$JAVA_HOME" ]; then
  echo "JAVA_HOME must be set to point to a JDK (not a JRE)"
  exit 1
fi

##   Use the following to set your own JVM arguments
JVM_ARGS=

##   Update the log4j configuration to set debug mode
JVM_ARGS="$JVM_ARGS -Dlog4j.configuration=file:conf/log4j.properties"

java $JVM_ARGS -cp "$JAVA_HOME"/lib/tools.jar:protocob.jar com.legstar.protobuf.cobol.ProtoCobolMain "$@"

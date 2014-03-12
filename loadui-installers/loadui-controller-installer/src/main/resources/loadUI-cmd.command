#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  LoadUI Command Line Runner for Mac                                      ##
##                                                                          ##
### ====================================================================== ###

### $Id$ ###

DIRNAME=`dirname $0`

# Setup LOADUI_HOME
if [ "x$LOADUI_HOME" = "x" ]
then
    # get the full path (without any relative bits)
    LOADUI_HOME=`cd $DIRNAME/; pwd`
fi
export LOADUI_HOME

LOADUI_CLASSPATH="$LOADUI_HOME:$LOADUI_HOME/lib/*:$LOADUI_HOME/../../PlugIns/jre.bundle/Contents/Home/jre/lib/*"
JAVA="$LOADUI_HOME/../../PlugIns/jre.bundle/Contents/Home/jre/bin/java"

if [ ! -f "$JAVA" ]; 
then
  JAVA="java"
  echo "Using system Java"
else
  echo "Using bundled Java" 
fi

JAVA_OPTS="-Xms128m -Xmx768m -XX:MaxPermSize=128m"

"$JAVA" $JAVA_OPTS -cp "$LOADUI_CLASSPATH" com.javafx.main.Main --cmd=true --nofx=true -nofx -Dlog4j.configuration=log4j_headless.xml "$@"

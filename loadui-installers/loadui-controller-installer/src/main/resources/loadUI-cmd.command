#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  loadUI Agent Bootstrap Script                                           ##
##                                                                          ##
### ====================================================================== ###

### $Id$ ###

DIRNAME=`dirname $0`

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# Setup LOADUI_HOME
if [ "x$LOADUI_AGENT_HOME" = "x" ]
then
    # get the full path (without any relative bits)
    LOADUI_AGENT_HOME=`cd $DIRNAME/; pwd`
fi
export LOADUI_AGENT_HOME

LOADUI_AGENT_CLASSPATH="$LOADUI_AGENT_HOME:$LOADUI_AGENT_HOME/lib/*:$LOADUI_AGENT_HOME/../../PlugIns/jre.bundle/Contents/Home/jre/lib/*"

# For Cygwin, switch paths to Windows format before running java
if $cygwin
then
    LOADUI_AGENT_HOME=`cygpath --path -w "$LOADUI_AGENT_HOME"`
    LOADUI_AGENT_CLASSPATH=`cygpath --path -w "$LOADUI_AGENT_CLASSPATH"`
fi

JAVA = "../../PlugIns/jre.bundle/Contents/Home/jre/bin/java"

if [ ! -d "$JAVA" ]; then
  JAVA="java"
fi

JAVA_OPTS="-Xms128m -Xmx768m -XX:MaxPermSize=128m"

$JAVA $JAVA_OPTS -cp "$LOADUI_AGENT_CLASSPATH" com.eviware.loadui.launcher.LoadUICommandLineLauncher "$@"

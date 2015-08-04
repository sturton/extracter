#!/bin/sh
#
# extract.sh - Unix start script
# $Id$
#
# Normally, editing this script should not be required.
#
PRG=$0

#
# Resolve the location of this installation
# resolve symlinks (idea taken from Netbean's runide.sh)
#
while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
	PRG="$link"
    else
	PRG="`dirname "$PRG"`/$link"
    fi
done

pldir=`dirname "$PRG"`

# absolutize pldir
oldpwd=`pwd` ; cd "${pldir}"; pldir=`pwd`; cd "${oldpwd}"; unset oldpwd

# Set classpath
cp="${CLASSPATH}:${pldir}:${pldir}/target/${project.build.finalName}-jar-with-dependencies.jar"

if [ -z "$ORACLE_HOME" ]
then
  echo ERROR: Environment variable ORACLE_HOME not set. 1>&2
  exit 1
else
  #Normal ORACLE_HOME 
  cp="${cp}:$ORACLE_HOME/jdbc/lib/ojdbc7.jar:$ORACLE_HOME/jdbc/lib/ojdbc6.jar:$ORACLE_HOME/jdbc/lib/ojdbc5.jar:$ORACLE_HOME/jdbc/lib/ojdbc14.jar:$ORACLE_HOME/jdbc/lib/classes12.jar"
  #InstantClient ORACLE_HOME 
  cp="${cp}:$ORACLE_HOME/ojdbc7.jar:$ORACLE_HOME/ojdbc6.jar:$ORACLE_HOME/ojdbc5.jar:$ORACLE_HOME/ojdbc14.jar:$ORACLE_HOME/classes12.jar"
fi

#
# Call Extracter
#java -jar ${pldir}/target/${project.build.finalName}-jar-with-dependencies.jar "$@"
java -server -cp "$cp" ${exec.mainClass} "$@" 

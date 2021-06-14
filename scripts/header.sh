#!/bin/sh
# Combine with a jar file to produce an executable
PROG=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && PROG="./$0"
exec java -jar $PROG
exit 1 

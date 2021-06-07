all: compile

compile: 
	javac -cp ".:jgroups-3.0.0.Final.jar:flatlaf-demo-1.2.jar" *.java

client: compile
	java -cp ".:jgroups-3.0.0.Final.jar:flatlaf-demo-1.2.jar" Channel

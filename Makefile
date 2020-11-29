all: compile

compile: 
	javac -cp ".:jgroups-3.0.0.Final.jar" Channel.java

client: compile
	java -cp ".:jgroups-3.0.0.Final.jar" Channel

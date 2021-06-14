all: compile

compile: 
	javac -cp ".:resources/*" src/*.java

client: compile
	java -cp "src:resources/*" Client

clean:
	$(RM) src/*.class

all: compile

compile: 
	javac -cp ".:resources/*" dialogue/*.java

client: compile
	java -cp ".:resources/*" dialogue.Client

Dialogue.jar: compile
	jar -cvmf resources/MANIFEST.MF $@ dialogue/*.class
	
binary: Dialogue.jar
	cat scripts/header.sh Dialogue.jar > Dialogue && chmod +x Dialogue

clean:
	$(RM) src/*.class
	$(RM) Dialogue Dialogue.jar

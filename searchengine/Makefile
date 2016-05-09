SRC = ir/*.java
LIB = lib/json-simple.jar

all:
	javac $(SRC) -Xlint:none -encoding latin1 -cp $(LIB)

run:
	java -cp $(LIB):. ir.SearchGUI -d ./myFiles

clean:
	rm -f ir/*.class

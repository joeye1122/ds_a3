JAVAC = javac
JAVA_FLAGS = -source 1.8 -target 1.8

SOURCES = LogServer.java PaxosBroadcastServer.java PaxosParticipant.java PaxosSimulation.java Proposal.java Utils.java

CLASSES = $(SOURCES:.java=.class)

all: $(CLASSES)

%.class: %.java
	$(JAVAC) $(JAVA_FLAGS) $<

clean:
	rm -f *.class

.PHONY: clean

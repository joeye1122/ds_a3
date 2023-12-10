# Distributed System Paxos Voting Readme

# Introduction

The purpose of this distributed system is to simulate the decision-making process of the Adelaide Suburban Council Chairman election. We have designed an efficient and robust framework to handle the decision-making process of the Adelaide Suburban Council Chairman election. In this Paxos system designed for the election of the Adelaide Suburban Council Chairman, through a series of collaborative components, the system provides a robust decision-making framework. Centralized log management ensures transparency and traceability of the entire process, and makes testing very simple. The efficient message propagation mechanism (broadcast mechanism) ensures fast and accurate exchange of information among participants. The flexibility of system configuration allows the system to be adjusted according to different runtime environments, simulating different results under different delays.

# System Architecture

The main structure of this distributed system includes the following files:

1. **LogServer.java**

    It records the running logs of the algorithm, provides debugging information, and possible runtime errors. It may act as a centralized log server for all Paxos events, with a separate address and can handle logs through socket centralized logging by calling util. When running the log server independently in a terminal, the log information not only appears in the terminal but is also saved as a file.

2. **PaxosBroadcastServer.java**

    The purpose of the PaxosBroadcastServer is to implement the broadcast service, used to propagate messages among all participants in the Paxos algorithm. This service is responsible for transmitting proposals and decisions to all Paxos participating nodes. The broadcast server also maintains the overall number of participants in the system, as well as global information such as the port addresses of each person.

3. **PaxosParticipant.java**

    Represents a participant node in the Paxos algorithm, which may implement the behavior of Proposer, Acceptor, and Learner. This class is the core of the algorithm logic, handling message reception, decision generation, and learning process.

4. **Proposal.java**

    Represents a Paxos proposal, which includes a proposal number and a proposal value for passing proposal information among Paxos participants.

5. **Utils.java**

    Contains various supporting methods, such as network communication helpers, serialization and deserialization tools, and other utility functions that may be used multiple times throughout the project.

These classes work together to simulate the Paxos algorithm. The **`PaxosParticipant`** class is the center of interaction for all other classes, handling the logic of the algorithm. The **`PaxosBroadcastServer`** and **`LogServer`** provide support for network communication and log recording, while **`Utils`** provides configuration and utility tools. Finally, the **`Proposal`** class is the data model representing the proposals passed in the algorithm.

# How to Run the System

The system provides a series of ways to run, including manually starting each server and client, or starting based on the configuration information in a config file, as well as various ways to run and test the system, including multi-threaded mode or independent terminal mode.

### Manually Start the System

If you want to start each part of the system separately, first compile the program using the makefile, and then start the log server and broadcast server in order.

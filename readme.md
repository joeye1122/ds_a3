# Distributed System Paxos Voting Readme

# introduction

The purpose of this distributed system is to simulate the decision-making process of the Adelaide Suburban Council Chairman election. We have designed an efficient and robust framework to handle the decision-making process of the Adelaide Suburban Council Chairman election. In this Paxos system designed for the election, through a series of collaborative components, the system provides a robust decision-making framework. Centralized log management ensures transparency and traceability of the entire process, and makes testing simple. The efficient message dissemination mechanism (broadcast mechanism) ensures fast and accurate exchange of information among participants. The system configuration is flexible and allows the system to be adjusted according to different runtime environments, simulating different results without delay.

# system architecture

The main structure of this distributed system includes the following files:

1. **LogServer.java**

    Records the running log of the algorithm, provides debugging information, and possible runtime errors. It may act as a centralized log server for all Paxos events, with a separate address and can handle logs through socket centralized logging using utility calls. When running the log server independently in a terminal, the log information not only appears in the terminal but is also saved as a file.

2. **PaxosBroadcastServer.java**

    The purpose of **PaxosBroadcastServer** is to implement the broadcast service, used to propagate messages among all participants in the Paxos algorithm. This service is responsible for transmitting proposals and decisions to all Paxos participant nodes. The broadcast server also maintains the overall number of participants in the system, as well as global information such as the port address of each participant.

3. **PaxosParticipant.java**

    Represents a participant node in the Paxos algorithm, which is a member of the council (m1-m9). It may implement the behavior of Proposer, Acceptor, and Learner. This class is the core of the algorithm logic, handling message reception, decision generation, and learning process.

4. **Proposal.java**

    Represents a Paxos proposal, which includes a proposal number and a proposal value used to pass proposal information among Paxos participants.

5. **Utils.java**

    Contains various utility methods, such as network communication helpers, serialization and deserialization tools, and other utility functions that may be used multiple times throughout the project.

These classes work together to simulate the Paxos algorithm. The **`PaxosParticipant`** class is the center of interaction for all other classes, handling the logic of the algorithm. **`PaxosBroadcastServer`** and **`LogServer`** provide support for network communication and log recording, while **`Utils`** provides configuration and utility tools. Finally, the **`Proposal`** class is the data model representing the proposals passed in the algorithm.

# How to Run the System

The system provides several ways to run, including manually starting each server and client, or starting them all at once based on the configuration information provided in a config file. There are also multiple modes available for running and testing the system, such as multi-threaded mode or independent terminal mode.

### Manually Starting the System

If you want to start each part of the system separately, first compile the program using the makefile, and then start the log server and broadcast server in order.

```jsx
make
java LogServer
java PaxosBroadcastServer
```

you need to start the corresponding number of members based on your requirements. The "id" should be the name of the participant, for example, if it's "m1," you should fill in "1." The "port" is the listening address for that participant, e.g., 9090, 9000. Finally, the "delay" is the delay time in milliseconds. If no delay is needed, you can choose to enter 0.

> The log server and broadcast server use ports 10000 and 10001 respectively. When creating participants, avoid these two special ports.

> Any m1, m2, or m3 will automatically become a proposal.

```jsx
java PaxosParticipant [id] [portnumber] [delay]
```

### Running Automated Testing with Paxos Simulation

Paxos simulation allows you to initiate the system's built-in integration tests. Simply compile and run the Paxos simulation, and it will run in multi-threaded mode. The results will be displayed in the terminal after the test is completed.

```bash
java PaxosSimulation
```

### Paxos Simulation with Custom Configuration File

```bash
java PaxosSimulation [config address]
```
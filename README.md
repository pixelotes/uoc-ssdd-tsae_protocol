# UOC SSDD - TSAE PROTOCOL EXERCISE

This is the solution for the TSAE exercise that was proposed back when I was getting a Technical Engineering Systems degree in the UOC. It consisted on implementing several classes from the TSAE protocol, so a set of nodes would be able to exchange information and keep it synced, even though the implementation simulates random failures in the communications.

## The TSAE protocol
The exercise consists in completing a Java implementation of Golding's Timestamped Anti-Entropy Protocol (TSAE), a weak-consistency replication protocol designed for distributed internet applications, such as distributed databases, that predates other similar protocols like Bayou.

For further insight on this protocol, you can read the original Golding's thesis ["Weak-consistency group communication and membership"](http://www.cs.cornell.edu/courses/cs734/2000FA/cached%20papers/golding-thesis.PDF)


## Phases
* Phase 1 (up to a grade of D): Implementation and testing of Log and TimestampVector data structures
* Phase 2 (up to a grade of C-): Implementation of a reduced version of the application and TSAE protocol:
only add operation; no purge of log
* Phase 3 (up to a grade of C+): Extension of phase 2 to purge log with unsynchronized clocks
* Phase 4 (up to a grade of B): Evaluation of TSAE protocol and implementation of Remove recipe operation
* Phase 4.1 (up to a grade of A): Extend application adding the remove recipe operation
* Phase 4.2 (up to a grade of A with honors): Evaluation of TSAE protocol

## Setup


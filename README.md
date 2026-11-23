# Hospital ER Simulation

A Java console simulation of a hospital emergency room, built for the CS241/SCS241 Operating Systems course at Cairo University. It demonstrates the producer-consumer pattern with a bounded waiting room, a custom counting semaphore, and concurrent doctor and patient threads.

## How it works

- The program asks for the waiting room capacity, the number of doctors and the number of patients.
- Each patient is a thread. A patient waits for a free seat in the bounded waiting room, then enters it.
- Each doctor is a thread that takes the next patient from the waiting room.
- Before treating a patient, a doctor must acquire one of the two shared treatment rooms. If no room becomes free within 3 seconds, the doctor returns the patient to the front of the waiting room and takes another one.
- Treatment takes a random 2 to 4 seconds. Afterwards the doctor releases the room and the patient leaves the hospital.
- When every patient has been treated, the waiting room closes and the doctor threads finish.

## Concepts demonstrated

- Producer-consumer problem with a bounded buffer
- A custom counting semaphore with blocking acquire and timed acquire
- Mutual exclusion with synchronized, wait() and notifyAll()
- Avoiding race conditions on shared state (waiting room and treatment rooms)

## Project structure

- src/Hospital.java - entry point: reads input, creates and joins the threads
- src/WaitingRoom.java - bounded waiting room shared by patients and doctors
- src/TreatmentRoomManager.java - manages the shared treatment rooms
- src/Semaphore.java - custom counting semaphore
- src/Doctor.java - doctor thread logic
- src/Patient.java - patient thread logic
- src/Output.java - console logging

## Run

Requires a JDK.

    javac -d out src/*.java
    java -cp out Hospital

Then enter the waiting room capacity, the number of doctors and the number of patients when prompted.

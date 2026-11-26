# Hospital ER Simulation

A Java simulation of a hospital emergency room, built for the CS241/SCS241 Operating Systems course at Cairo University. It demonstrates the producer-consumer pattern with a bounded waiting room, a custom counting semaphore, and concurrent doctor and patient threads.

The project runs as a console application and as a JavaFX GUI ("City General ER - Night Shift") that animates the same simulation live: patients walk in, take seats, doctors treat them in the shared treatment rooms, and every event appears in a color-coded log with real-time statistics.

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

- pom.xml - Maven build for the JavaFX GUI
- src/Hospital.java - console entry point: reads input, creates and joins the threads
- src/WaitingRoom.java - bounded waiting room shared by patients and doctors
- src/TreatmentRoomManager.java - manages the shared treatment rooms
- src/Semaphore.java - custom counting semaphore
- src/Doctor.java - doctor thread logic
- src/Patient.java - patient thread logic
- src/Output.java - console logging
- src/gui/ - JavaFX GUI (runs the original classes unchanged and visualizes their events)
- src/main/resources/style.css - GUI theme

The GUI is built on top of the original seven classes without modifying them: it captures the simulation's log output and turns each event into animation, so the concurrency logic, timing and behavior stay exactly the same in both versions.

## The GUI

- Setup screen: choose waiting room seats (1-12), doctors (1-6) and patients (1-30).
- Animated ER floor: entrance and outside queue, waiting room seats, doctor status cards and the two treatment rooms with a live pulse while treating.
- Patients that hit the 3-second room timeout walk back to the front of the waiting room.
- Live statistics: treated patients, occupied seats, busy rooms, send-backs, timeouts and elapsed time.
- Pause/Resume button (or the Space key) freezes the whole run mid-shift and resumes it exactly where it stopped.
- Color-coded event log mirroring the console output.
- "All patients treated" summary when the simulation finishes, with a New Simulation reset.

## Run

Requires a JDK.

### Console

    javac -d out src/*.java
    java -cp out Hospital

Then enter the waiting room capacity, the number of doctors and the number of patients when prompted.

### GUI (Maven)

The GUI needs the pom's Java and JavaFX versions (Java 27, JavaFX 27.0.1).

    mvn javafx:run

Or in IntelliJ IDEA: open the project as a Maven project, then run Maven Plugins > javafx > javafx:run.

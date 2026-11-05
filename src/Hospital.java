import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Hospital {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter waiting room capacity: ");
        int capacity = scanner.nextInt();

        System.out.print("Enter number of doctors: ");
        int numberOfDoctors = scanner.nextInt();

        System.out.print("Enter number of patients: ");
        int numberOfPatients = scanner.nextInt();

        if (capacity <= 0 || numberOfDoctors <= 0 || numberOfPatients < 0) {
            System.out.println("Invalid input.");
            return;
        }
        Output.printHeader(capacity, numberOfDoctors, numberOfPatients);
        WaitingRoom waitingRoom = new WaitingRoom(capacity);

        TreatmentRoomManager treatmentRoomManager =
                new TreatmentRoomManager(2);

        CountDownLatch completionLatch =
                new CountDownLatch(numberOfPatients);

        Doctor[] doctors = new Doctor[numberOfDoctors];

        for (int i = 0; i < numberOfDoctors; i++) {
            doctors[i] = new Doctor(
                    i + 1,
                    waitingRoom,
                    treatmentRoomManager,
                    completionLatch
            );
        }

        Thread[] patients = new Thread[numberOfPatients];

        for (int i = 0; i < numberOfPatients; i++) {
            Patient patient = new Patient(i + 1, waitingRoom);
            patients[i] = new Thread(patient);
        }

        for (Doctor doctor : doctors) {
            doctor.start();
        }
        for (Thread patient : patients) {
            patient.start();
        }

        try {
            completionLatch.await();

            for (Thread patient : patients) {
                patient.join();
            }
            waitingRoom.close(numberOfDoctors);

            for (Doctor doctor : doctors) {
                doctor.join();
            }
            Output.printFooter();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Output.log("Hospital simulation was interrupted.");
        }

        scanner.close();
    }
}
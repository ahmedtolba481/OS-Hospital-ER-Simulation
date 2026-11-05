public class TestSemaphore {

    public static void main(String[] args) {

        Semaphore semaphore = new Semaphore(2);

        System.out.println("Initial permits: " + semaphore.get_value());

        try {

            semaphore.acquire();

            System.out.println("First acquire successful");
            System.out.println("Available permits: " + semaphore.get_value());

            semaphore.acquire();

            System.out.println("Second acquire successful");
            System.out.println("Available permits: " + semaphore.get_value());

            boolean acquired = semaphore.acq_timeout(2000);

            System.out.println("Third acquire successful? " + acquired);

            semaphore.release();

            System.out.println("Released one permit");

            acquired = semaphore.acq_timeout(2000);

            System.out.println("Can acquire now? " + acquired);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            System.out.println("Test interrupted");
        }
    }
}
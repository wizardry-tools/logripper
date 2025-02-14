package com.wizardry.tools.logripper.jsamples;

public class SynchronizedCounting {
    static class Counter {
        private int count = 0;

        public synchronized void increment() {
            count++;
        }

        public synchronized int getCount() {
            return count;
        }
    }

    static class IncrementThread extends Thread {
        private final Counter counter;

        public IncrementThread(Counter counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            for (int i = 0; i < 1000; i++) {
                counter.increment();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();

        Thread t1 = new IncrementThread(counter);
        Thread t2 = new IncrementThread(counter);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Final count is: " + counter.getCount());
    }
}

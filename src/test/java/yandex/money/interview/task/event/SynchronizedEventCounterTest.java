package yandex.money.interview.task.event;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SynchronizedEventCounterTest {
    private SynchronizedEventCounter counter;

    @Before
    public void setup() {
        counter = new SynchronizedEventCounter();
    }

    @Test
    public void countAndExpectTheSameNumber() {
        int expectedCount = 100;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < expectedCount; i++) {
            counter.count();
        }
        long countForLastDay = counter.getCountForLastDay();
        long countForLastHour = counter.getCountForLastHour();
        long countForLastMinute = counter.getCountForLastMinute();
        long countSinceStart = counter.getCountSince(startTime);
        assertEquals(countForLastDay, expectedCount);
        assertEquals(countForLastHour, expectedCount);
        assertEquals(countForLastMinute, expectedCount);
        assertEquals(countSinceStart, expectedCount);
    }

    @Test
    public void countWithOneSecondDelay() throws InterruptedException {
        int expectedCountForLastSecond = 100;
        int expectedCountForAllTime = 200;
        for (int i = 0; i < 100; i++) {
            counter.count();
        }
        Thread.sleep(1000);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            counter.count();
        }
        long countSinceLastSecond = counter.getCountSince(startTime);
        long countForAllTime = counter.getCountForLastDay();
        assertEquals(countSinceLastSecond, expectedCountForLastSecond);
        assertEquals(countForAllTime, expectedCountForAllTime);
    }
}

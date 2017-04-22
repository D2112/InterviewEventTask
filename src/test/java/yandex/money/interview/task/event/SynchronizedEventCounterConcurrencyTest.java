package yandex.money.interview.task.event;

import org.junit.*;
import org.junit.rules.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class SynchronizedEventCounterConcurrencyTest {
    private static final int OPERATIONS_TO_PERFORM = 10000;
    private static final int COUNT_PER_ACTION = 1000;
    private static final int ACTIONS_PER_THREAD = 1;
    private static final int OPERATIONS_PER_THREAD = ACTIONS_PER_THREAD * COUNT_PER_ACTION; //1000
    private static final int MAX_THREADS = OPERATIONS_TO_PERFORM / OPERATIONS_PER_THREAD; //10
    private static final int ACTIONS_TO_PERFORM_AT_ONCE = MAX_THREADS * ACTIONS_PER_THREAD; //10
    @Rule
    public Timeout globalTimeout = new Timeout(20, TimeUnit.SECONDS);
    private SynchronizedEventCounter counter;
    private Callable<Void> countAction;
    private ExecutorService executorService;
    private CompletionService<Void> completionService;

    @Before
    public void setup() {
        counter = new SynchronizedEventCounter();
        executorService = Executors.newFixedThreadPool(MAX_THREADS);
        completionService = new ExecutorCompletionService<>(executorService);
        countAction = () -> {
            for (int i = 0; i < COUNT_PER_ACTION; i++) {
                counter.count();
            }
            return null;
        };
    }

    @After
    public void shutdown() {
        executorService.shutdown();
    }

    @Test
    public void performCountFromDifferentThreadsOnce() throws InterruptedException {
        int actionsToPerform = ACTIONS_TO_PERFORM_AT_ONCE;
        int countToExpect = actionsToPerform * COUNT_PER_ACTION;
        performCounterActions(actionsToPerform);
        long eventsCounted = counter.getCountForLastMinute();
        assertEquals(countToExpect, eventsCounted);
    }

    @Test
    public void performCountFromDifferentThreadsMultipleTimes() throws InterruptedException {
        int actionsToPerformAtOnce = ACTIONS_TO_PERFORM_AT_ONCE;
        int numOfPerforms = 3;
        int countToExpect = actionsToPerformAtOnce * numOfPerforms * COUNT_PER_ACTION;
        //Несколько раз запускаем потоки с операциями через каждую секунду
        performWithDelay(() -> {
            performCounterActions(actionsToPerformAtOnce);
            return null;
        }, numOfPerforms, 1000);
        //Ждем, пока сервис завершит все задачи
        for (int i = 0; i < numOfPerforms; i++) {
            completionService.take();
        }
        long eventsCounted = counter.getCountForLastMinute();
        assertEquals(eventsCounted, countToExpect);
    }

    private void performWithDelay(Callable<Void> callable, int amountOfTimes, long delayMillis) throws InterruptedException {
        for (int i = 0; i < amountOfTimes; i++) {
            completionService.submit(callable);
            Thread.sleep(delayMillis);
        }
    }

    private List<Future<Void>> performCounterActions(int amountOfActions) throws InterruptedException {
        List<Callable<Void>> actions = getCounterActions(amountOfActions);
        return executorService.invokeAll(actions);
    }

    private List<Callable<Void>> getCounterActions(int amountOfActions) throws InterruptedException {
        List<Callable<Void>> actions = new ArrayList<>();
        for (int i = 0; i < amountOfActions; i++) {
            actions.add(countAction);
        }
        return actions;
    }
}

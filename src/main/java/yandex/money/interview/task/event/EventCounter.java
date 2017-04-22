package yandex.money.interview.task.event;

public interface EventCounter {

    void count();

    long getCountForLastMinute();

    long getCountForLastHour();

    long getCountForLastDay();

    long getCountSince(long sinceTime);

}

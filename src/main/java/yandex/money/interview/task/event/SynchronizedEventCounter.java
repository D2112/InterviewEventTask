package yandex.money.interview.task.event;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedEventCounter implements EventCounter {
    private List<Long> eventTimestamps = new LinkedList<>();
    private ReentrantLock lock = new ReentrantLock();

    @Override
    public void count() {
        long eventTime = System.currentTimeMillis();
        lock.lock();
        try {
            eventTimestamps.add(eventTime);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long getCountForLastMinute() {
        return getCountSince(getCurrentTimeMinus(1, TimeUnit.MINUTES));
    }

    @Override
    public long getCountForLastHour() {
        return getCountSince(getCurrentTimeMinus(1, TimeUnit.HOURS));
    }

    @Override
    public long getCountForLastDay() {
        return getCountSince(getCurrentTimeMinus(24, TimeUnit.HOURS));
    }

    @Override
    public long getCountSince(long sinceTime) {
        lock.lock();
        try {
            int index = ceilingIndexOf(eventTimestamps, sinceTime);
            return tailList(eventTimestamps, index).size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * ���� ������ �������� � ��������� ��������� � ��������������� �� ����������� ������
     * @param list ��������������� �� ����������� ������
     * @param value ������� ��������
     * @return ���������� ������ �������� � ��������� ���������.
     * ���� ������� � ����� ��������� �����������, �� ����� ������ ������� ��������,
     * ������� ��������� ��������� ��������
     */
    private int ceilingIndexOf(List<Long> list, long value) {
        int resultIndex;
        int indexOf = list.indexOf(value);
        if (indexOf >= 0) resultIndex = indexOf;
        else {
            //���� ���������� �������� ���,
            //�� �������� ����� ����� �������������� ������ �������� � ���� �������������� �����
            int binarySearchIndex = Collections.binarySearch(list, value);
            resultIndex = -binarySearchIndex - 1; //������ ���������� ��������
        }
        return resultIndex;
    }

    private <T> List<T> tailList(List<T> list, int fromIndex) {
        return list.subList(fromIndex, list.size());
    }

    private long getCurrentTimeMinus(long minusTime, TimeUnit minusTimeUnit) {
        long currentTime = System.currentTimeMillis();
        return currentTime - minusTimeUnit.toMillis(minusTime);
    }

}

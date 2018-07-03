package com.abcexample.myassistant;

/**
 * Created by divya on 09-03-2018.
 */

public class Schedule {

    private Integer Hour;
    private Integer Minute;
    private String Task;

    public Schedule() {
    }

    public Schedule(Integer hour, Integer minute, String task) {
        Hour = hour;
        Minute = minute;
        Task = task;
    }

    public Integer getHour() {
        return Hour;
    }

    public void setHour(Integer hour) {
        Hour = hour;
    }

    public Integer getMinute() {
        return Minute;
    }

    public void setMinute(Integer minute) {
        Minute = minute;
    }

    public String getTask() {
        return Task;
    }

    public void setTask(String task) {
        Task = task;
    }
}

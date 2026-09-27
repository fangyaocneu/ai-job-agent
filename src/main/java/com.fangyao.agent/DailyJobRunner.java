package com.fangyao.agent;

public class DailyJobRunner {

    public static void main(String[] args) {

        DailyJobWorkflow workflow =
                new DailyJobWorkflow();

        workflow.run();
    }
}
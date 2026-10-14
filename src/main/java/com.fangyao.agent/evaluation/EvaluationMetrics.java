package com.fangyao.agent.evaluation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.fangyao.agent.AgentIntent;

public class EvaluationMetrics {

    public static double accuracy(
            List<EvaluationResult> results
    ) {

        if (results == null || results.isEmpty()) {
            return 0.0;
        }

        long correct =
                results.stream()
                        .filter(EvaluationResult::isCorrect)
                        .count();

        return (double) correct
                / results.size();
    }

    public static double averageLatencyMs(
            List<EvaluationResult> results
    ) {

        if (results == null || results.isEmpty()) {
            return 0.0;
        }

        return results.stream()
                .mapToLong(EvaluationResult::getLatencyMs)
                .average()
                .orElse(0.0);
    }

    public static double precision(
            List<EvaluationResult> results,
            AgentIntent intent
    ) {

        long truePositive = 0;
        long falsePositive = 0;

        for (EvaluationResult result : results) {

            if (result.getActual() == intent) {

                if (result.getExpected() == intent) {
                    truePositive++;
                } else {
                    falsePositive++;
                }
            }
        }

        long denominator =
                truePositive + falsePositive;

        if (denominator == 0) {
            return 0.0;
        }

        return (double) truePositive
                / denominator;
    }

    public static double recall(
            List<EvaluationResult> results,
            AgentIntent intent
    ) {

        long truePositive = 0;
        long falseNegative = 0;

        for (EvaluationResult result : results) {

            if (result.getExpected() == intent) {

                if (result.getActual() == intent) {
                    truePositive++;
                } else {
                    falseNegative++;
                }
            }
        }

        long denominator =
                truePositive + falseNegative;

        if (denominator == 0) {
            return 0.0;
        }

        return (double) truePositive
                / denominator;
    }

    public static double f1(
            List<EvaluationResult> results,
            AgentIntent intent
    ) {

        double precision =
                precision(
                        results,
                        intent
                );

        double recall =
                recall(
                        results,
                        intent
                );

        if (precision + recall == 0) {
            return 0.0;
        }

        return 2
                * precision
                * recall
                / (precision + recall);
    }

    public static double macroF1(
            List<EvaluationResult> results
    ) {

        double total = 0.0;

        AgentIntent[] intents =
                AgentIntent.values();

        for (AgentIntent intent : intents) {

            total +=
                    f1(
                            results,
                            intent
                    );
        }

        return total
                / intents.length;
    }

    public static Map<
            AgentIntent,
            Map<AgentIntent, Integer>
            > confusionMatrix(
            List<EvaluationResult> results
    ) {

        Map<
                AgentIntent,
                Map<AgentIntent, Integer>
                > matrix =
                new EnumMap<>(
                        AgentIntent.class
                );

        for (AgentIntent expected :
                AgentIntent.values()) {

            Map<AgentIntent, Integer> row =
                    new EnumMap<>(
                            AgentIntent.class
                    );

            for (AgentIntent actual :
                    AgentIntent.values()) {

                row.put(
                        actual,
                        0
                );
            }

            matrix.put(
                    expected,
                    row
            );
        }

        for (EvaluationResult result : results) {

            AgentIntent expected =
                    result.getExpected();

            AgentIntent actual =
                    result.getActual();

            Map<AgentIntent, Integer> row =
                    matrix.get(expected);

            row.put(
                    actual,
                    row.get(actual) + 1
            );
        }

        return matrix;
    }
    
    // =========================
    // Percentile Latency
    // =========================
    public static double percentileLatencyMs(
            List<EvaluationResult> results,
            double percentile
    ) {

        if (results == null || results.isEmpty()) {
            return 0.0;
        }

        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException(
                    "Percentile must be between 0 and 100."
            );
        }

        List<Long> latencies = results.stream()
                .map(EvaluationResult::getLatencyMs)
                .sorted()
                .toList();

        int index = (int) Math.ceil(
                (percentile / 100.0) * latencies.size()
        ) - 1;

        index = Math.max(0, index);

        return latencies.get(index);
    }

}
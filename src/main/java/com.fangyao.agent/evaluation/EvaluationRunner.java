package com.fangyao.agent.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fangyao.agent.AgentIntent;
import com.fangyao.agent.JevIntentRouter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EvaluationRunner {

    public static void main(String[] args) {

        JevIntentRouter router
                = new JevIntentRouter();

        List<EvaluationCase> cases
                = loadCases();

        List<EvaluationResult> results
                = new ArrayList<>();

        for (EvaluationCase testCase : cases) {

            long start
                    = System.currentTimeMillis();

            AgentIntent actual
                    = router.route(
                            testCase.getInput()
                    );

            long latency
                    = System.currentTimeMillis()
                    - start;

            EvaluationResult result
                    = new EvaluationResult(
                            testCase.getInput(),
                            testCase.getExpectedIntent(),
                            actual,
                            latency
                    );

            results.add(result);

            System.out.println(
                    (result.isCorrect()
                    ? "[PASS] "
                    : "[FAIL] ")
                    + testCase.getInput()
            );

            System.out.println(
                    "Expected: "
                    + result.getExpected()
                    + " | Actual: "
                    + result.getActual()
                    + " | Latency: "
                    + latency
                    + " ms"
            );

            System.out.println();
        }

        printSummary(results);
    }

    private static List<EvaluationCase> loadCases() {

        try {

            ObjectMapper mapper
                    = new ObjectMapper();

            File file
                    = new File(
                            "evaluation/intent_cases.json"
                    );

            List<EvaluationCase> cases
                    = mapper.readValue(
                            file,
                            new TypeReference<
                                List<EvaluationCase>>() {
                    }
                    );

            System.out.println(
                    "[EVALUATION] Loaded "
                    + cases.size()
                    + " cases from "
                    + file.getPath()
            );

            System.out.println();

            return cases;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load evaluation cases",
                    e
            );
        }
    }

    private static void printSummary(
            List<EvaluationResult> results
    ) {

        double accuracy
                = EvaluationMetrics.accuracy(
                        results
                );

        double averageLatency
                = EvaluationMetrics.averageLatencyMs(
                        results
                );

        double macroF1
                = EvaluationMetrics.macroF1(
                        results
                );

        long passed
                = results.stream()
                        .filter(
                                EvaluationResult::isCorrect
                        )
                        .count();

        System.out.println(
                "===== EVALUATION REPORT ====="
        );

        System.out.println(
                "Total Cases: "
                + results.size()
        );

        System.out.println(
                "Passed: "
                + passed
        );

        System.out.printf(
                "Accuracy: %.2f%%%n",
                accuracy * 100
        );

        System.out.println();

        System.out.println(
                "===== PER-INTENT METRICS ====="
        );

        for (AgentIntent intent
                : AgentIntent.values()) {

            double precision
                    = EvaluationMetrics.precision(
                            results,
                            intent
                    );

            double recall
                    = EvaluationMetrics.recall(
                            results,
                            intent
                    );

            double f1
                    = EvaluationMetrics.f1(
                            results,
                            intent
                    );

            System.out.println(
                    intent
            );

            System.out.printf(
                    "  Precision: %.2f%%%n",
                    precision * 100
            );

            System.out.printf(
                    "  Recall:    %.2f%%%n",
                    recall * 100
            );

            System.out.printf(
                    "  F1:        %.2f%%%n",
                    f1 * 100
            );

            System.out.println();
        }

        System.out.printf(
                "Macro F1: %.2f%%%n",
                macroF1 * 100
        );

        System.out.printf(
                "Average Latency: %.2f ms%n",
                averageLatency
        );

        System.out.println();

        printConfusionMatrix(
                results
        );

        System.out.println(
                "============================="
        );
    }

    private static void printConfusionMatrix(
            List<EvaluationResult> results
    ) {

        var matrix
                = EvaluationMetrics.confusionMatrix(
                        results
                );

        AgentIntent[] intents
                = AgentIntent.values();

        System.out.println(
                "===== CONFUSION MATRIX ====="
        );

        System.out.printf(
                "%-15s",
                "Expected\\Actual"
        );

        for (AgentIntent intent
                : intents) {

            System.out.printf(
                    "%-15s",
                    intent
            );
        }

        System.out.println();

        for (AgentIntent expected
                : intents) {

            System.out.printf(
                    "%-15s",
                    expected
            );

            for (AgentIntent actual
                    : intents) {

                System.out.printf(
                        "%-15d",
                        matrix
                                .get(expected)
                                .get(actual)
                );
            }

            System.out.println();
        }
    }
}

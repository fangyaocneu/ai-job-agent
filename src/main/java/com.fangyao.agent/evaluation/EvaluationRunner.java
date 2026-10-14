package com.fangyao.agent.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fangyao.agent.AgentIntent;
import com.fangyao.agent.JevIntentRouter;
import com.fangyao.agent.RoutingDecision;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EvaluationRunner {

    public static void main(String[] args) {

        JevIntentRouter router = new JevIntentRouter();

        String dataset = args.length > 0
                ? args[0]
                : "baseline";

        List<EvaluationCase> cases = loadCases(dataset);

        if (cases.isEmpty()) {
            throw new IllegalStateException(
                    "Evaluation dataset must not be empty."
            );
        }

        List<EvaluationResult> results = new ArrayList<>();

        for (EvaluationCase testCase : cases) {

            // RoutingDecision contains:
            // Intent, Source, Confidence, Latency, FallbackReason
            RoutingDecision decision
                    = router.routeWithDetails(testCase.getInput());

            EvaluationResult result
                    = new EvaluationResult(
                            testCase.getInput(),
                            testCase.getExpectedIntent(),
                            decision
                    );

            results.add(result);

            printCaseResult(result);
        }

        printSummary(results);
    }

    // =========================
    // Load Golden Dataset
    // =========================
    private static List<EvaluationCase> loadCases(String dataset) {

        String fileName = switch (dataset.toLowerCase()) {
            case "baseline" ->
                "intent_cases.json";
            case "local" ->
                "intent_local_cases.json";
            case "live" ->
                "intent_live_cases.json";
            default ->
                throw new IllegalArgumentException(
                        "Unknown dataset: " + dataset
                );
        };

        try {
            ObjectMapper mapper = new ObjectMapper();

            File file = new File("evaluation", fileName);

            List<EvaluationCase> cases = mapper.readValue(
                    file,
                    new TypeReference<List<EvaluationCase>>() {
            }
            );

            System.out.println(
                    "[EVALUATION] Dataset: " + dataset
                    + " | Loaded: " + cases.size()
                    + " cases"
            );

            return cases;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load dataset: " + dataset,
                    e
            );
        }
    }

    // =========================
    // Print Individual Results
    // =========================
    private static void printCaseResult(
            EvaluationResult result
    ) {

        System.out.println(
                (result.isCorrect() ? "[PASS] " : "[FAIL] ")
                + result.getInput()
        );

        System.out.println(
                "Expected: " + result.getExpected()
                + " | Actual: " + result.getActual()
                + " | Source: " + result.getSource()
                + " | Confidence: " + result.getConfidence()
                + " | Latency: " + result.getLatencyMs()
                + " ms"
        );

        if (result.getFallbackReason() != null) {

            System.out.println(
                    "Fallback Reason: "
                    + result.getFallbackReason()
            );
        }

        System.out.println();
    }

    // =========================
    // Evaluation Summary
    // =========================
    private static void printSummary(
            List<EvaluationResult> results
    ) {

        double accuracy
                = EvaluationMetrics.accuracy(results);

        double averageLatency
                = EvaluationMetrics.averageLatencyMs(results);

        double macroF1
                = EvaluationMetrics.macroF1(results);

        long passed = results.stream()
                .filter(EvaluationResult::isCorrect)
                .count();

        System.out.println(
                "===== EVALUATION REPORT ====="
        );

        System.out.println(
                "Total Cases: " + results.size()
        );

        System.out.println(
                "Passed: " + passed
        );

        System.out.println(
                "Failed: " + (results.size() - passed)
        );

        System.out.printf(
                "Accuracy: %.2f%%%n",
                accuracy * 100
        );

        System.out.println();

        printPerIntentMetrics(results);

        System.out.printf(
                "Macro F1: %.2f%%%n",
                macroF1 * 100
        );

        System.out.printf(
                "Average Latency: %.2f ms%n",
                averageLatency
        );

        System.out.printf(
                "P50 Latency: %.2f ms%n",
                EvaluationMetrics.percentileLatencyMs(results, 50)
        );

        System.out.printf(
                "P95 Latency: %.2f ms%n",
                EvaluationMetrics.percentileLatencyMs(results, 95)
        );

        System.out.println();

        printRoutingMetrics(results);

        printConfusionMatrix(results);

        System.out.println(
                "============================="
        );
    }

    // =========================
    // Per-Intent Metrics
    // =========================
    private static void printPerIntentMetrics(
            List<EvaluationResult> results
    ) {

        System.out.println(
                "===== PER-INTENT METRICS ====="
        );

        for (AgentIntent intent : AgentIntent.values()) {

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

            System.out.println(intent);

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
    }

    // =========================
    // Routing Observability Metrics
    // =========================
    private static void printRoutingMetrics(
            List<EvaluationResult> results
    ) {

        System.out.println(
                "===== ROUTING METRICS ====="
        );

        int total = results.size();

        for (RoutingDecision.RoutingSource source
                : RoutingDecision.RoutingSource.values()) {

            long count = results.stream()
                    .filter(result
                            -> result.getSource() == source
                    )
                    .count();

            long correct = results.stream()
                    .filter(result
                            -> result.getSource() == source
                            && result.isCorrect()
                    )
                    .count();

            double hitRate = total == 0
                    ? 0.0
                    : (double) count / total;

            double sourceAccuracy = count == 0
                    ? 0.0
                    : (double) correct / count;

            System.out.printf(
                    "%s%n",
                    source
            );

            System.out.printf(
                    "  Count: %d%n",
                    count
            );

            System.out.printf(
                    "  Hit Rate: %.2f%%%n",
                    hitRate * 100
            );

            if (count > 0) {
                System.out.printf(
                        "  Accuracy: %.2f%%%n",
                        sourceAccuracy * 100
                );
            } else {
                System.out.println(
                        "  Accuracy: N/A (no cases)"
                );
            }

            System.out.println();
        }
    }

    // =========================
    // Confusion Matrix
    // =========================
    private static void printConfusionMatrix(
            List<EvaluationResult> results
    ) {

        var matrix
                = EvaluationMetrics.confusionMatrix(results);

        AgentIntent[] intents
                = AgentIntent.values();

        System.out.println(
                "===== CONFUSION MATRIX ====="
        );

        int width = 19;

        System.out.printf(
                "%-" + width + "s",
                "Expected\\Actual"
        );

        for (AgentIntent intent : intents) {

            System.out.printf(
                    "%-" + width + "s",
                    intent
            );
        }

        System.out.println();

        for (AgentIntent expected : intents) {

            System.out.printf(
                    "%-" + width + "s",
                    expected
            );

            for (AgentIntent actual : intents) {

                System.out.printf(
                        "%-" + width + "d",
                        matrix.get(expected).get(actual)
                );
            }

            System.out.println();
        }
    }
}

package com.fangyao.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;

public class Main {

    public static void main(String[] args) {

        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        Scanner scanner = new Scanner(System.in);

        String previousResponseId = null;

        System.out.println("AI Agent started.");
        System.out.println("Type 'exit' to quit.");
        System.out.println();

        // ==========================
        // Conversation Loop
        // ==========================
        while (true) {

            System.out.print("You: ");

            String userInput = scanner.nextLine();

            // Exit command
            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Agent: Goodbye!");
                break;
            }

            // Ignore empty input
            if (userInput.isBlank()) {
                continue;
            }

            ResponseCreateParams.Builder requestBuilder =
                    ResponseCreateParams.builder()
                            .model("gpt-5.6-luna")
                            .addTool(GetWeather.class)
                            .addTool(GetCurrentTime.class)
                            .addTool(SearchJobs.class)
                            .input(userInput);

            // If we already have conversation history,
            // connect this request to the previous response
            if (previousResponseId != null) {
                requestBuilder.previousResponseId(previousResponseId);
            }

            Response response =
                    client.responses().create(
                            requestBuilder.build()
                    );

            // ==========================
            // Agent Loop
            // ==========================
            while (true) {

                List<ResponseInputItem> toolOutputs =
                        new ArrayList<>();

                // Look through everything returned by the model
                for (var item : response.output()) {

                    if (item.isFunctionCall()) {

                        ResponseFunctionToolCall functionCall =
                                item.asFunctionCall();

                        System.out.println(
                                "[Tool Call] "
                                        + functionCall.name()
                        );

                        String result = 
                                ToolExecutor.execute(functionCall);

                        System.out.println(
                                "[Tool Result] " + result
                        );

                        // Save tool output
                        toolOutputs.add(
                                ResponseInputItem.ofFunctionCallOutput(
                                        ResponseInputItem
                                                .FunctionCallOutput
                                                .builder()
                                                .callId(
                                                        functionCall
                                                                .callId()
                                                )
                                                .output(result)
                                                .build()
                                )
                        );
                    }
                }

                // ==========================
                // No more tool calls
                // → final answer
                // ==========================
                if (toolOutputs.isEmpty()) {

                    response.output().stream()
                            .flatMap(
                                    item ->
                                            item.message().stream()
                            )
                            .flatMap(
                                    message ->
                                            message.content().stream()
                            )
                            .flatMap(
                                    content ->
                                            content.outputText().stream()
                            )
                            .forEach(
                                    output ->
                                            System.out.println(
                                                    "Agent: "
                                                            + output.text()
                                            )
                            );

                    // Remember this response
                    // for the next conversation turn
                    previousResponseId =
                            response.id();

                    System.out.println();

                    break;
                }

                // ==========================
                // Send tool results back
                // ==========================
                ResponseCreateParams nextRequest =
                        ResponseCreateParams.builder()
                                .model("gpt-5.6-luna")
                                .addTool(GetWeather.class)
                                .addTool(GetCurrentTime.class)
                                .addTool(SearchJobs.class)
                                .previousResponseId(
                                        response.id()
                                )
                                .inputOfResponse(
                                        toolOutputs
                                )
                                .build();

                response =
                        client.responses().create(
                                nextRequest
                        );
            }
        }

        scanner.close();
    }
}
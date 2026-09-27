package com.fangyao.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.openai.models.responses.ResponseFunctionToolCall;

public class ToolExecutor {

    private static final Map<
            String,
            Function<ResponseFunctionToolCall, String>
            > TOOL_REGISTRY = new HashMap<>();

    static {

        TOOL_REGISTRY.put(
                "GetWeather",
                functionCall -> {

                    GetWeather weather =
                            functionCall.arguments(
                                    GetWeather.class
                            );

                    return weather.execute();
                }
        );

        TOOL_REGISTRY.put(
                "GetCurrentTime",
                functionCall -> {

                    GetCurrentTime time =
                            functionCall.arguments(
                                    GetCurrentTime.class
                            );

                    return time.execute();
                }
        );
    }

    public static String execute(
            ResponseFunctionToolCall functionCall
    ) {

        String toolName =
                functionCall.name();

        Function<ResponseFunctionToolCall, String> tool =
                TOOL_REGISTRY.get(toolName);

        if (tool == null) {
            return "Unknown tool: " + toolName;
        }

        return tool.apply(functionCall);
    }
}
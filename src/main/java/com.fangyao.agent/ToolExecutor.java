package com.fangyao.agent;

import java.util.HashMap;
import java.util.List;
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

        TOOL_REGISTRY.put(
                "SearchJobs",
                functionCall -> {

                    SearchJobsArgs args =
                            functionCall.arguments(
                                    SearchJobsArgs.class
                            );

                    if (args.keyword == null ||
                            args.keyword.isBlank()) {

                        return "Missing job search keyword.";
                    }

                    SearchJobs searchJobs =
                            new SearchJobs();

                    List<Job> jobs =
                            searchJobs.search(args.keyword);

                    if (jobs.isEmpty()) {
                        return "No new jobs found for keyword: "
                                + args.keyword;
                    }

                    StringBuilder result =
                            new StringBuilder();

                    result.append(
                            "New jobs found for keyword: "
                                    + args.keyword
                                    + "\n\n"
                    );

                    for (Job job : jobs) {

                        result.append("Title: ")
                                .append(job.getTitle())
                                .append("\n");

                        result.append("Company: ")
                                .append(job.getCompany())
                                .append("\n");

                        result.append("Location: ")
                                .append(job.getLocation())
                                .append("\n");

                        result.append("URL: ")
                                .append(job.getUrl())
                                .append("\n\n");
                    }

                    return result.toString();
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

    public static class SearchJobsArgs {

        public String keyword;

        public SearchJobsArgs() {
        }
    }
}

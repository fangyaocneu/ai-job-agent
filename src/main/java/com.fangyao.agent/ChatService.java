package com.fangyao.agent;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final OpenAIClient client;
    private final CandidateProfileRepository profileRepository;
    private final ChatToolService chatToolService;
    private final ChatMessageRepository chatMessageRepository;

    // =========================
    // Constructor
    // =========================
    public ChatService() {

        this.client
                = OpenAIOkHttpClient.fromEnv();

        this.profileRepository
                = new CandidateProfileRepository();

        this.chatToolService
                = new ChatToolService();

        this.chatMessageRepository
                = new ChatMessageRepository();
    }

    // =========================
    // Tool 1: Get Top Matches
    // =========================
    @JsonClassDescription(
            """
            Gets the candidate's current top job matches
            from the application's real job database.

            Use this tool when the user asks which jobs
            they should apply to, their best opportunities,
            strongest matches, recommended jobs,
            or what they should focus on applying to.
            """
    )
    public static class GetTopMatches {

        @JsonPropertyDescription(
                """
                Maximum number of job matches requested by the user.
                If the user does not specify a number, use 5.
                """
        )
        public Integer limit;

        public GetTopMatches() {
        }
    }

    // =========================
    // Tool 2: Get Job Insights
    // =========================
    @JsonClassDescription(
            """
            Gets detailed match information for one specific job
            from the application's real job database.

            Use this tool when the user asks why a specific job
            received a certain match score, asks about the fit
            of a specific job, or wants to understand skill fit,
            experience fit, role fit, preference fit, strengths,
            weaknesses, or skill gaps for a specific job ID.
            """
    )
    public static class GetJobInsights {

        @JsonPropertyDescription(
                """
                The numeric job ID of the job the user is asking about.
                """
        )
        public Long jobId;

        public GetJobInsights() {
        }
    }

    // =========================
    // Tool 3: Get Follow-Ups
    // =========================
    @JsonClassDescription(
            """
            Gets applications that currently have follow-ups due
            from the application's real database.

            Use this tool when the user asks which applications
            need attention, whether any follow-ups are due or overdue,
            what they should follow up on next,
            or similar application follow-up questions.
            """
    )
    public static class GetFollowUps {

        @JsonPropertyDescription(
                """
                Optional scope for the follow-up request.
                Use "due" when the user is asking about follow-ups
                that currently need attention.
                """
        )
        public String scope;

        public GetFollowUps() {
        }
    }

    // =========================
    // Tool 4: Get Applications
    // =========================
    @JsonClassDescription(
            """
            Gets the candidate's currently tracked job applications
            from the application's real database.

            Use this tool when the user asks what jobs they have applied to,
            which applications they are tracking,
            their current application pipeline,
            application stages,
            or the status of their applications.
            """
    )
    public static class GetApplications {

        @JsonPropertyDescription(
                """
                Optional application stage requested by the user.

                Examples:
                APPLIED
                OA
                PHONE_SCREEN
                INTERVIEW
                FINAL_ROUND
                OFFER
                REJECTED

                If no specific stage is requested,
                use "ALL".
                """
        )
        public String stage;

        public GetApplications() {
        }
    }

    // =========================
    // Tool 5: Candidate Profile
    // =========================
    @JsonClassDescription(
            """
            Gets the candidate's current stored profile
            from the application's database.

            Use this tool when the user asks about their skills,
            target roles, preferred locations, work preferences,
            experience, projects, technologies,
            or asks to summarize their candidate profile.
            """
    )
    public static class GetCandidateProfile {

        @JsonPropertyDescription(
                """
                Optional section of the profile requested by the user.
                Examples: skills, roles, locations, experience,
                projects, technologies, or all.
                """
        )
        public String section;

        public GetCandidateProfile() {
        }
    }

    // =========================
    // Main Chat
    // =========================
    public String chat(
            long conversationId,
            String message
    ) {

        if (message == null
                || message.isBlank()) {

            throw new IllegalArgumentException(
                    "Chat message cannot be empty."
            );
        }

        // =========================
        // Load Candidate Profile
        // =========================
        CandidateProfile profile
                = profileRepository.loadProfile();

        String profileContext
                = profile == null
                        ? "No candidate profile is available."
                        : profile.getProfileSummary();

        // =========================
        // Load Persistent Memory
        // =========================
        List<ChatMessage> recentMessages
                = chatMessageRepository.getRecentMessages(
                        conversationId,
                        10
                );

        StringBuilder historyBuilder
                = new StringBuilder();

        if (recentMessages.isEmpty()) {

            historyBuilder.append(
                    "No previous conversation."
            );

        } else {

            for (ChatMessage chatMessage
                    : recentMessages) {

                historyBuilder
                        .append(
                                chatMessage.getRole()
                        )
                        .append(": ")
                        .append(
                                chatMessage.getContent()
                        )
                        .append("\n");
            }
        }

        String historyContext
                = historyBuilder.toString();

        // =========================
        // Initial Prompt
        // =========================
        String prompt = """
                You are an AI career assistant
                inside a job-search application.

                You help the candidate:

                - understand job opportunities
                - evaluate job fit
                - make application decisions
                - understand their candidate profile
                - understand AI match scores
                - manage application follow-ups
                - track active job applications
                - improve job-search strategy

                Candidate Profile:

                %s

                Recent Conversation:

                %s

                Current User Message:

                %s

                Important Rules:

                - Use the recent conversation to understand
                  references such as "the second one",
                  "that job", "it", or "the previous role".

                - Use tools whenever current application
                  or database information is required.

                - Do not invent jobs, companies,
                  scores, applications, follow-ups,
                  stages, or database data.

                - If a relevant tool is available,
                  use it instead of saying that you
                  do not have access to the information.

                - When discussing one specific job ID,
                  use the job insights tool whenever
                  its stored scoring information is relevant.

                - When recommending current jobs,
                  use the top matches tool.

                - When the user asks about applications
                  that need attention, overdue follow-ups,
                  follow-ups due today, or what to follow up on next,
                  use the follow-ups tool.

                - When the user asks which jobs they have applied to,
                  what applications they are tracking,
                  application stages,
                  or their application pipeline,
                  use the applications tool.

                - When the user asks about their skills,
                  target roles, preferred locations,
                  work preferences, experience, projects,
                  or technologies,
                  use the candidate profile tool.

                - Use candidate profile information
                  when relevant.

                - Do not invent candidate experience.

                - Clearly distinguish stored system data
                  from your interpretation of that data.

                - Answer clearly and concisely.
                """
                .formatted(
                        profileContext,
                        historyContext,
                        message
                );

        // =========================
        // Conversation Input
        // =========================
        List<ResponseInputItem> inputs
                = new ArrayList<>();

        inputs.add(
                ResponseInputItem.ofMessage(
                        ResponseInputItem.Message
                                .builder()
                                .role(
                                        ResponseInputItem.Message.Role.USER
                                )
                                .addInputTextContent(
                                        prompt
                                )
                                .build()
                )
        );

        // =========================
        // Register Tools
        // =========================
        ResponseCreateParams.Builder paramsBuilder
                = ResponseCreateParams
                        .builder()
                        .model(
                                ChatModel.GPT_5_2
                        )
                        .addTool(
                                GetTopMatches.class
                        )
                        .addTool(
                                GetJobInsights.class
                        )
                        .addTool(
                                GetFollowUps.class
                        )
                        .addTool(
                                GetApplications.class
                        )
                        .addTool(
                                GetCandidateProfile.class
                        )
                        .input(
                                ResponseCreateParams.Input
                                        .ofResponse(
                                                inputs
                                        )
                        );

        // =========================
        // Multi-Tool Reasoning Loop
        // =========================
        int maxToolRounds
                = 5;

        for (int round = 1;
                round <= maxToolRounds;
                round++) {

            System.out.println(
                    "[AI REASONING ROUND] "
                    + round
            );

            paramsBuilder.input(
                    ResponseCreateParams.Input
                            .ofResponse(
                                    inputs
                            )
            );

            Response response
                    = client.responses()
                            .create(
                                    paramsBuilder.build()
                            );

            boolean toolWasCalledThisRound
                    = false;

            // =========================
            // Execute Tool Calls
            // =========================
            for (var item
                    : response.output()) {

                if (!item.isFunctionCall()) {

                    continue;
                }

                toolWasCalledThisRound
                        = true;

                ResponseFunctionToolCall functionCall
                        = item.asFunctionCall();

                System.out.println(
                        "[AI TOOL CALL] "
                        + functionCall.name()
                );

                System.out.println(
                        "[AI TOOL RAW ARGUMENTS] "
                        + functionCall.arguments()
                );

                // Keep tool call in reasoning context
                inputs.add(
                        ResponseInputItem
                                .ofFunctionCall(
                                        functionCall
                                )
                );

                Object toolResult
                        = executeTool(
                                functionCall
                        );

                System.out.println(
                        "[AI TOOL RESULT]"
                );

                System.out.println(
                        toolResult
                );

                // Return result to model
                inputs.add(
                        ResponseInputItem
                                .ofFunctionCallOutput(
                                        ResponseInputItem.FunctionCallOutput
                                                .builder()
                                                .callId(
                                                        functionCall.callId()
                                                )
                                                .outputAsJson(
                                                        toolResult
                                                )
                                                .build()
                                )
                );
            }

            // =========================
            // Final Answer
            // =========================
            if (!toolWasCalledThisRound) {

                System.out.println(
                        "[AI REASONING COMPLETE] "
                        + round
                        + " round(s)"
                );

                String finalAnswer
                        = extractOutputText(
                                response
                        );

                // =========================
                // Save Persistent Memory
                // =========================
                chatMessageRepository.save(
                        conversationId,
                        "user",
                        message
                );

                chatMessageRepository.save(
                        conversationId,
                        "assistant",
                        finalAnswer
                );

                return finalAnswer;
            }
        }

        // =========================
        // Safety Limit
        // =========================
        throw new IllegalStateException(
                "AI exceeded maximum tool reasoning rounds."
        );
    }

    // =========================
    // Tool Executor
    // =========================
    private Object executeTool(
            ResponseFunctionToolCall functionCall
    ) {

        return switch (functionCall.name()) {

            // =========================
            // Tool 1: Top Matches
            // =========================
            case "GetTopMatches" -> {

                GetTopMatches arguments
                        = functionCall.arguments(
                                GetTopMatches.class
                        );

                int requestedLimit
                        = arguments.limit == null
                                ? 5
                                : arguments.limit;

                System.out.println(
                        "[AI TOOL EXECUTOR] Running getTopMatches()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] limit = "
                        + requestedLimit
                );

                yield chatToolService
                .getTopMatches(
                requestedLimit
                );
            }

            // =========================
            // Tool 2: Job Insights
            // =========================
            case "GetJobInsights" -> {

                GetJobInsights arguments
                        = functionCall.arguments(
                                GetJobInsights.class
                        );

                if (arguments.jobId == null) {

                    throw new IllegalArgumentException(
                            "GetJobInsights requires jobId."
                    );
                }

                System.out.println(
                        "[AI TOOL EXECUTOR] Running getJobInsights()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] jobId = "
                        + arguments.jobId
                );

                yield chatToolService
                .getJobInsights(
                arguments.jobId
                .intValue()
                );
            }

            // =========================
            // Tool 3: Follow-Ups
            // =========================
            case "GetFollowUps" -> {

                GetFollowUps arguments
                        = functionCall.arguments(
                                GetFollowUps.class
                        );

                String scope
                        = arguments.scope == null
                                ? "due"
                                : arguments.scope;

                System.out.println(
                        "[AI TOOL EXECUTOR] Running getFollowUps()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] scope = "
                        + scope
                );

                yield chatToolService
                .getFollowUps();
            }

            // =========================
            // Tool 4: Applications
            // =========================
            case "GetApplications" -> {

                GetApplications arguments
                        = functionCall.arguments(
                                GetApplications.class
                        );

                String stage
                        = arguments.stage == null
                                ? "ALL"
                                : arguments.stage;

                System.out.println(
                        "[AI TOOL EXECUTOR] Running getApplications()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] stage = "
                        + stage
                );

                yield chatToolService
                .getApplications();
            }

            // =========================
            // Tool 5: Candidate Profile
            // =========================
            case "GetCandidateProfile" -> {

                GetCandidateProfile arguments
                        = functionCall.arguments(
                                GetCandidateProfile.class
                        );

                String section
                        = arguments.section == null
                                ? "all"
                                : arguments.section;

                System.out.println(
                        "[AI TOOL EXECUTOR] Running getCandidateProfile()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] section = "
                        + section
                );

                yield chatToolService
                .getCandidateProfile();
            }

            // =========================
            // Unknown Tool
            // =========================
            default ->
                throw new IllegalArgumentException(
                        "Unknown tool: "
                        + functionCall.name()
                );
        };
    }

    // =========================
    // Extract Final Text
    // =========================
    private String extractOutputText(
            Response response
    ) {

        StringBuilder output
                = new StringBuilder();

        response.output()
                .stream()
                .flatMap(
                        item
                        -> item.message()
                                .stream()
                )
                .flatMap(
                        message
                        -> message.content()
                                .stream()
                )
                .flatMap(
                        content
                        -> content.outputText()
                                .stream()
                )
                .forEach(
                        text
                        -> output.append(
                                text.text()
                        )
                );

        if (output.isEmpty()) {

            throw new IllegalStateException(
                    "Could not find text in OpenAI response."
            );
        }

        return output
                .toString()
                .trim();
    }
}

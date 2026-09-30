package com.fangyao.agent;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;

import java.util.ArrayList;
import java.util.List;

public class ResumeGapService {

    private final ResumeRagService resumeRagService;
    private final OpenAIClient client;

    public ResumeGapService() {

        this.resumeRagService =
                new ResumeRagService();

        this.client =
                OpenAIOkHttpClient.fromEnv();
    }

    // =========================
    // Analyze Job Description
    // =========================

    public GapAnalysis analyze(
            String jobDescription
    ) {

        if (jobDescription == null
                || jobDescription.isBlank()) {

            throw new IllegalArgumentException(
                    "Job description cannot be empty."
            );
        }

        // =========================
        // Step 1:
        // Retrieve Resume Evidence
        // =========================

        List<ResumeChunkMatch> resumeChunks =
                resumeRagService
                        .retrieveRelevantChunks(
                                jobDescription,
                                3
                        );

        if (resumeChunks.isEmpty()) {

            throw new IllegalStateException(
                    "No relevant resume evidence was found."
            );
        }

        // =========================
        // Step 2:
        // Build Evidence Context
        // =========================

        String resumeEvidence =
                buildResumeEvidence(
                        resumeChunks
                );

        // =========================
        // Step 3:
        // Build Gap Analysis Prompt
        // =========================

        String prompt =
                """
                You are analyzing a job description against a candidate's resume.

                Your analysis MUST be grounded only in the provided resume evidence.

                Do not invent candidate experience.

                A skill may be classified as MATCHED only when the resume evidence
                explicitly demonstrates that skill, technology, or experience.

                Do not treat related technologies as equivalent.

                Examples:
                - AWS ECS does NOT prove Kubernetes experience.
                - Spring Boot services do NOT prove microservices experience.
                - GitHub Actions alone does NOT prove end-to-end CI/CD experience.
                - Using a database does NOT automatically prove database optimization experience.

                If the evidence is indirect, adjacent, implied, or incomplete,
                classify the requirement as PARTIAL.

                If there is no meaningful evidence,
                classify the requirement as MISSING.

                When uncertain, prefer PARTIAL or MISSING over MATCHED.

                For every MATCHED skill,
                cite specific resume evidence that explicitly supports it.

                JOB DESCRIPTION:

                %s


                RESUME EVIDENCE:

                %s


                Return the analysis in exactly this structure:

                MATCHED SKILLS:
                - ...

                PARTIAL MATCHES:
                - ...

                MISSING SKILLS:
                - ...

                RESUME EVIDENCE:
                - ...

                GAP SEVERITY:
                LOW, MEDIUM, or HIGH

                RECOMMENDED IMPROVEMENTS:
                - ...

                Keep the analysis concise and practical.
                """
                .formatted(
                        jobDescription,
                        resumeEvidence
                );

        // =========================
        // Step 4:
        // Call LLM
        // =========================

        List<ResponseInputItem> inputs =
                new ArrayList<>();

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

        ResponseCreateParams params =
                ResponseCreateParams
                        .builder()
                        .model(
                                ChatModel.GPT_5_2
                        )
                        .input(
                                ResponseCreateParams.Input
                                        .ofResponse(
                                                inputs
                                        )
                        )
                        .build();

        Response response =
                client.responses()
                        .create(
                                params
                        );

        // =========================
        // Step 5:
        // Extract Analysis
        // =========================

        String analysisText =
                extractOutputText(
                        response
                );

        return new GapAnalysis(
                jobDescription,
                resumeChunks,
                analysisText
        );
    }

    // =========================
    // Build Resume Evidence
    // =========================

    private String buildResumeEvidence(
            List<ResumeChunkMatch> chunks
    ) {

        StringBuilder builder =
                new StringBuilder();

        for (
                int i = 0;
                i < chunks.size();
                i++
        ) {

            ResumeChunkMatch chunk =
                    chunks.get(i);

            builder.append(
                    "--- Resume Chunk "
                            + (i + 1)
                            + " ---\n"
            );

            builder.append(
                    "Resume ID: "
                            + chunk.getResumeId()
                            + "\n"
            );

            builder.append(
                    "Chunk Index: "
                            + chunk.getChunkIndex()
                            + "\n"
            );

            builder.append(
                    "Similarity: "
                            + String.format(
                                    "%.4f",
                                    chunk.getSimilarity()
                            )
                            + "\n\n"
            );

            builder.append(
                    chunk.getContent()
            );

            builder.append(
                    "\n\n"
            );
        }

        return builder.toString();
    }

    // =========================
    // Extract OpenAI Text
    // =========================

    private String extractOutputText(
            Response response
    ) {

        StringBuilder output =
                new StringBuilder();

        response.output()
                .stream()
                .flatMap(
                        item ->
                                item.message()
                                        .stream()
                )
                .flatMap(
                        message ->
                                message.content()
                                        .stream()
                )
                .flatMap(
                        content ->
                                content.outputText()
                                        .stream()
                )
                .forEach(
                        text ->
                                output.append(
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

    // =========================
    // Gap Analysis Result
    // =========================

    public static class GapAnalysis {

        private final String jobDescription;

        private final List<ResumeChunkMatch>
                resumeChunks;

        private final String analysis;

        public GapAnalysis(
                String jobDescription,
                List<ResumeChunkMatch> resumeChunks,
                String analysis
        ) {

            this.jobDescription =
                    jobDescription;

            this.resumeChunks =
                    resumeChunks;

            this.analysis =
                    analysis;
        }

        public String getJobDescription() {

            return jobDescription;
        }

        public List<ResumeChunkMatch>
        getResumeChunks() {

            return resumeChunks;
        }

        public String getAnalysis() {

            return analysis;
        }
    }
}
package com.fangyao.agent;

public class JobDescriptionService {

    private static final int MAX_JOB_DESCRIPTION_LENGTH = 12_000;

    public String extractCoreJobDescription(String pageText) {

        if (pageText == null || pageText.isBlank()) {

            throw new IllegalArgumentException(
                    "Job page text cannot be empty."
            );
        }

        String originalText =
                pageText.trim();

        String text =
                originalText;

        /*
         * Find where the actual job description begins.
         */
        int startIndex =
                findFirstIndex(
                        text,
                        0,
                        "Responsibilities",
                        "About the role",
                        "About the Role",
                        "Job Description",
                        "What you'll do",
                        "What You’ll Do"
                );

        if (startIndex >= 0) {

            /*
             * Keep a small amount of text before Responsibilities
             * so title/location information is preserved.
             */
            int safeStart =
                    Math.max(
                            0,
                            startIndex - 300
                    );

            text =
                    text.substring(
                            safeStart
                    );
        }

        /*
         * IMPORTANT:
         *
         * Do not search for footer markers from index 0,
         * because navigation menus may contain strings such as
         * "About TikTok".
         *
         * First find the qualifications section. Footer removal
         * should only happen AFTER that section.
         */
        int qualificationIndex =
                findFirstIndex(
                        text,
                        0,
                        "Preferred Qualifications",
                        "Minimum Qualifications",
                        "Qualifications"
                );

        int footerSearchStart;

        if (qualificationIndex >= 0) {

            footerSearchStart =
                    qualificationIndex + 100;

        } else {

            /*
             * If no qualification heading exists,
             * avoid removing content too early.
             */
            footerSearchStart =
                    Math.min(
                            1000,
                            text.length()
                    );
        }

        int endIndex =
                findFirstIndex(
                        text,
                        footerSearchStart,
                        "Job Information",
                        "For Pay Transparency",
                        "Compensation Description",
                        "About TikTok",
                        "Why Join Us",
                        "Diversity & Inclusion",
                        "Equal Opportunity",
                        "Candidate Privacy Policy",
                        "Privacy Policy",
                        "Terms of Service"
                );

        if (endIndex > 0) {

            text =
                    text.substring(
                            0,
                            endIndex
                    );
        }

        /*
         * Normalize whitespace.
         */
        text =
                text
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();

        /*
         * Safety fallback:
         *
         * If cleaning produced something suspiciously short,
         * return the original text instead of destroying the JD.
         */
        if (text.length() < 500) {

            System.out.println(
                    "[JD CLEANER] Clean result too short. "
                    + "Falling back to original page text."
            );

            text =
                    originalText
                            .replaceAll(
                                    "\\s+",
                                    " "
                            )
                            .trim();
        }

        /*
         * Hard context limit.
         */
        if (text.length()
                > MAX_JOB_DESCRIPTION_LENGTH) {

            text =
                    text.substring(
                            0,
                            MAX_JOB_DESCRIPTION_LENGTH
                    );
        }

        return text;
    }

    private int findFirstIndex(
            String text,
            int fromIndex,
            String... markers
    ) {

        int earliestIndex =
                -1;

        for (String marker : markers) {

            int index =
                    text.indexOf(
                            marker,
                            fromIndex
                    );

            if (index >= 0
                    && (
                        earliestIndex == -1
                        || index < earliestIndex
                    )) {

                earliestIndex =
                        index;
            }
        }

        return earliestIndex;
    }
}
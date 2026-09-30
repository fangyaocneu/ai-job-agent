package com.fangyao.agent;

public class ResumeChunkMatch {

    private final long resumeId;
    private final int chunkIndex;
    private final String content;
    private final double similarity;

    public ResumeChunkMatch(
            long resumeId,
            int chunkIndex,
            String content,
            double similarity
    ) {
        this.resumeId = resumeId;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.similarity = similarity;
    }

    public long getResumeId() {
        return resumeId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public double getSimilarity() {
        return similarity;
    }
}
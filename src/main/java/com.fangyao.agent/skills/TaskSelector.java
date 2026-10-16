
package com.fangyao.agent.skills;

import java.util.Locale;

import com.fangyao.agent.AgentIntent;

public class TaskSelector {

    public AgentTask select(
            AgentIntent intent,
            String message
    ) {
        if (intent == null || message == null) {
            return AgentTask.UNSUPPORTED;
        }

        String text = message.toLowerCase(Locale.ROOT);

        if (intent == AgentIntent.JOB_MATCH) {

            // Resume comparison takes priority
            if (text.contains("my resume")
                    || text.contains("my cv")
                    || text.contains("我的履歷")
                    || text.contains("匹配")
                    || text.contains("match my")) {

                return AgentTask.JOB_MATCHING;
            }

            if (text.contains("analyze jd")
                    || text.contains("analyze this job")
                    || text.contains("job description")
                    || text.contains("解析jd")
                    || text.contains("分析jd")
                    || text.contains("分析職缺")
                    || text.contains("職缺要求")) {

                return AgentTask.JD_ANALYSIS;
            }

            // Preserve existing matching behavior
            return AgentTask.JOB_MATCHING;
        }

        if (intent == AgentIntent.RESUME) {
            return AgentTask.RESUME_ANALYSIS;
        }

        return AgentTask.UNSUPPORTED;
    }
}

package com.fangyao.agent;

public class PreFilterAgent
        implements Agent<AgentContext, AgentContext> {

    private final JobPreFilter preFilter;

    public PreFilterAgent() {
        this.preFilter = new JobPreFilter();
    }

    @Override
    public AgentContext execute(
            AgentContext context
    ) {

        Job job =
                context.getJob();

        boolean relevant =
                preFilter.shouldScore(job);

        context.setRelevant(
                relevant
        );

        if (relevant) {

            System.out.println(
                    "[PreFilterAgent] Relevant job: "
                            + job.getTitle()
            );

        } else {

            System.out.println(
                    "[PreFilterAgent] Rejected job: "
                            + job.getTitle()
            );
        }

        return context;
    }
}
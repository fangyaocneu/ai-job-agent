package com.fangyao.agent;

import java.util.List;

public interface JobSource {

    String getName();

    List<Job> fetchJobs();
}
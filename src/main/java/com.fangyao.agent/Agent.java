package com.fangyao.agent;

public interface Agent<I, O> {

    O execute(I input);
}
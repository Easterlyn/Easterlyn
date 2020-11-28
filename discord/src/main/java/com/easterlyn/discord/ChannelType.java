package com.easterlyn.discord;

public enum ChannelType {
  LOG(30000),
  MAIN(200),
  REPORT(10000),
  WELCOME(0);

  private final String path;
  private final long aggregateTime;

  ChannelType(long aggregate) {
    path = name().toLowerCase();
    aggregateTime = aggregate;
  }

  public String getPath() {
    return path;
  }

  public long getAggregateTime() {
    return aggregateTime;
  }
}

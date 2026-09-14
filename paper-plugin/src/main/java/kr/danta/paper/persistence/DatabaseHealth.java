package kr.danta.paper.persistence;

public record DatabaseHealth(
        DatabaseStatus status,
        String target,
        String lastError
) {}

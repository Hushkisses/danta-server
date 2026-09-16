package kr.danta.core.snapshot;
import java.util.Objects;
public record VassalRelationSnapshot(String vassalNationId,String overlordNationId,long vassalizedAtRuntimeMillis){public VassalRelationSnapshot{Objects.requireNonNull(vassalNationId);Objects.requireNonNull(overlordNationId);if(vassalizedAtRuntimeMillis<0)throw new IllegalArgumentException("negative runtime");}}

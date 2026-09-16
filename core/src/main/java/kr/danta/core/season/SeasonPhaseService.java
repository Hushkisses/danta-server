package kr.danta.core.season;
import java.util.Objects;import java.util.function.LongSupplier;
public final class SeasonPhaseService {
 private final LongSupplier runtime; public SeasonPhaseService(LongSupplier runtime){this.runtime=Objects.requireNonNull(runtime);}
 public SeasonPhase current(){return SeasonPhase.atRuntimeMillis(runtime.getAsLong());} public long runtimeMillis(){return runtime.getAsLong();}
 public long remainingMillis(){long now=runtimeMillis(),end=current().endRuntimeMillis();return end==Long.MAX_VALUE?0L:Math.max(0,end-now);}
}
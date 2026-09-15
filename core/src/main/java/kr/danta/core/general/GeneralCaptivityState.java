package kr.danta.core.general;

import java.util.Objects;

/** DEV-075 prisoner state based on server runtime, never wall-clock time. */
public record GeneralCaptivityState(
        String captorNationId,
        long capturedAtRuntimeMillis,
        long detentionEndsAtRuntimeMillis
) {
    public GeneralCaptivityState {
        captorNationId = requireId(captorNationId, "captorNationId");
        if (capturedAtRuntimeMillis < 0L) throw new IllegalArgumentException("capturedAtRuntimeMillis must be >= 0");
        if (detentionEndsAtRuntimeMillis <= capturedAtRuntimeMillis)
            throw new IllegalArgumentException("detentionEndsAtRuntimeMillis must be after capture time");
    }

    public boolean detentionExpired(long runtimeMillis) {
        return runtimeMillis >= detentionEndsAtRuntimeMillis;
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String v = value.trim();
        if (v.isEmpty()) throw new IllegalArgumentException(label + " must not be blank");
        return v;
    }
}

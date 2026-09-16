package kr.danta.core.research;

import java.util.Objects;

/** National doctrine selection. A doctrine occupies one national slot regardless of field. */
public record DoctrineSelection(String doctrineKey, ResearchField field) {
    public DoctrineSelection {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(doctrineKey, "doctrineKey");
        doctrineKey = doctrineKey.trim().toLowerCase();
        if (!doctrineKey.matches("[a-z0-9_.-]+")) throw new IllegalArgumentException("invalid doctrineKey: " + doctrineKey);
    }
}

package kr.danta.core.general;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev072ATraitAbilityTest {
    @Test void generalStartsWithoutTraitsOrAbilities() {
        GeneralState general = new GeneralState("g1", "red", GeneralGrade.C, 3);
        assertTrue(general.traits().isEmpty());
        assertTrue(general.abilities().isEmpty());
    }

    @Test void traitAndAbilityIdsAreValidatedAndDuplicateSafe() {
        GeneralState general = new GeneralState("g1", "red", GeneralGrade.C, 3);
        GeneralTrait trait = new GeneralTrait("archer_specialist");
        GeneralAbility ability = new GeneralAbility("rally");

        assertTrue(general.addTrait(trait));
        assertFalse(general.addTrait(new GeneralTrait("archer_specialist")));
        assertTrue(general.addAbility(ability));
        assertFalse(general.addAbility(new GeneralAbility("rally")));

        assertEquals(List.of(trait), general.traits());
        assertEquals(List.of(ability), general.abilities());
    }

    @Test void traitAndAbilityCanBeRemovedWithoutNumericSideEffects() {
        GeneralState general = new GeneralState("g1", "red", GeneralGrade.B, 4);
        general.addTrait(new GeneralTrait("steady"));
        general.addAbility(new GeneralAbility("rally"));

        assertTrue(general.removeTrait("steady"));
        assertTrue(general.removeAbility("rally"));
        assertTrue(general.traits().isEmpty());
        assertTrue(general.abilities().isEmpty());
        assertFalse(general.removeTrait("missing"));
        assertFalse(general.removeAbility("missing"));
    }

    @Test void identityObjectsDoNotInventEffectFields() {
        assertArrayEquals(new String[]{"traitId"},
                java.util.Arrays.stream(GeneralTrait.class.getRecordComponents())
                        .map(java.lang.reflect.RecordComponent::getName).toArray(String[]::new));
        assertArrayEquals(new String[]{"abilityId"},
                java.util.Arrays.stream(GeneralAbility.class.getRecordComponents())
                        .map(java.lang.reflect.RecordComponent::getName).toArray(String[]::new));
    }

    @Test void invalidIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new GeneralTrait("한글 특성"));
        assertThrows(IllegalArgumentException.class, () -> new GeneralAbility(""));
    }
}

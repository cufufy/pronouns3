package net.cufufy.pronouns.api.supplier;

import net.cufufy.pronouns.api.set.PronounSet;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A supplier of predefined pronoun sets.
 */
@FunctionalInterface
public interface PronounSupplier extends Supplier<Set<PronounSet>> {
}

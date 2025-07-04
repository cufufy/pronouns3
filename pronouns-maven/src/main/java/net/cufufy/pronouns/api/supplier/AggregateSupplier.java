package net.cufufy.pronouns.api.supplier;

import net.cufufy.pronouns.api.set.PronounSet;

import java.util.Set;
import java.util.stream.Collectors;

public record AggregateSupplier(Set<PronounSupplier> providers) implements PronounSupplier {

    public AggregateSupplier(PronounSupplier... suppliers) {
        this(Set.of(suppliers));
    }
    @Override
    public Set<PronounSet> get() {
        return providers.stream().flatMap(p -> p.get().stream()).collect(Collectors.toUnmodifiableSet());
    }
}

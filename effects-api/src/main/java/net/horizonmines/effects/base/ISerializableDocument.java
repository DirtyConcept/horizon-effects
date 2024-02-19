package net.horizonmines.effects.base;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

public interface ISerializableDocument {

    @NotNull Document toDocument();

    void fromDocument(@NotNull Document document);
}

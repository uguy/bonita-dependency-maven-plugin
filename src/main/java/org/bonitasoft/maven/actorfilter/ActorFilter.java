package org.bonitasoft.maven.actorfilter;

import lombok.Data;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
public class ActorFilter {

    public static final String FILTER_DEF_FOLDER = "filters-def";
    public static final String FILTER_IMPL_FOLDER = "filters-impl";
    public static final String LIB_FOLDER = "lib";

    private Set<String> includes = new HashSet<>();
    private String definitionFolder = FILTER_DEF_FOLDER;
    private String implementationFolder = FILTER_IMPL_FOLDER;
    private String libFolder = LIB_FOLDER;

    public String getDefinitionFolder() {
        return Optional.ofNullable(definitionFolder).orElse(FILTER_DEF_FOLDER);
    }

    public String getImplementationFolder() {
        return Optional.ofNullable(implementationFolder).orElse(FILTER_IMPL_FOLDER);
    }

    public String getLibFolder() {
        return Optional.ofNullable(libFolder).orElse(LIB_FOLDER);
    }
}

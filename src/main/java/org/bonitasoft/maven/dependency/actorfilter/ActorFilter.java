package org.bonitasoft.maven.dependency.actorfilter;

import lombok.Data;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * ActorFilters dependency configuration.
 */
@Data
public class ActorFilter {

    public static final String DEFAULT_FILTER_DEF_FOLDER = "filters-def";
    public static final String DEFAULT_FILTER_IMPL_FOLDER = "filters-impl";
    public static final String DEFAULT_LIB_FOLDER = "lib";

    /**
     * Includes configuration. Includes should be in the form of <code><include>groupId:artifactId</include></code>. The artifactId can be replaced by a star(*) to match
     * all artefacts with the specified groupId.
     */
    private Set<String> includes = new HashSet<>();
    /**
     * The folder where to copy actor filter definition files and properties
     */
    private String definitionFolder = DEFAULT_FILTER_DEF_FOLDER;
    /**
     * The folder where to copy actor filter implementation files
     */
    private String implementationFolder = DEFAULT_FILTER_IMPL_FOLDER;
    /**
     * The folder where to copy actor filter jar dependencies
     */
    private String libFolder = DEFAULT_LIB_FOLDER;

    public String getDefinitionFolder() {
        return Optional.ofNullable(definitionFolder).orElse(DEFAULT_FILTER_DEF_FOLDER);
    }

    public String getImplementationFolder() {
        return Optional.ofNullable(implementationFolder).orElse(DEFAULT_FILTER_IMPL_FOLDER);
    }

    public String getLibFolder() {
        return Optional.ofNullable(libFolder).orElse(DEFAULT_LIB_FOLDER);
    }
}

package org.bonitasoft.maven.dependency.connector;

import lombok.Data;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Connectors dependency configuration
 */
@Data
public class Connector {

    public static final String DEFAULT_CONNECTOR_DEF_FOLDER = "connectors-def";
    public static final String DEFAULT_CONNECTOR_IMPL_FOLDER = "connectors-impl";
    public static final String DEFAULT_LIB_FOLDER = "lib";

    /**
     * Includes configuration. Includes should be in the form of <code><include>groupId:artifactId</include></code>. The artifactId can be replaced by a star(*) to match
     * all artefacts with the specified groupId.
     */
    private Set<String> includes = new HashSet<>();
    /**
     * The folder where to copy connector definition files and properties
     */
    private String definitionFolder = DEFAULT_CONNECTOR_DEF_FOLDER;
    /**
     * The folder where to copy connector implementation files and properties
     */
    private String implementationFolder = DEFAULT_CONNECTOR_IMPL_FOLDER;
    /**
     * The folder where to copy connector jar dependencies
     */
    private String libFolder = DEFAULT_LIB_FOLDER;

    public String getDefinitionFolder() {
        return Optional.ofNullable(definitionFolder).orElse(DEFAULT_CONNECTOR_DEF_FOLDER);
    }

    public String getImplementationFolder() {
        return Optional.ofNullable(implementationFolder).orElse(DEFAULT_CONNECTOR_IMPL_FOLDER);
    }

    public String getLibFolder() {
        return Optional.ofNullable(libFolder).orElse(DEFAULT_LIB_FOLDER);
    }
}

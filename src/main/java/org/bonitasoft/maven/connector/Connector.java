package org.bonitasoft.maven.connector;

import lombok.Data;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
public class Connector {

    public static final String CONNECTOR_DEF_FOLDER = "connectors-def";
    public static final String CONNECTOR_IMPL_FOLDER = "connectors-impl";
    public static final String LIB_FOLDER = "lib";

    private Set<String> includes = new HashSet<>();
    private String definitionFolder = CONNECTOR_DEF_FOLDER;
    private String implementationFolder = CONNECTOR_IMPL_FOLDER;
    private String libFolder = LIB_FOLDER;

    public String getDefinitionFolder() {
        return Optional.ofNullable(definitionFolder).orElse(CONNECTOR_DEF_FOLDER);
    }

    public String getImplementationFolder() {
        return Optional.ofNullable(implementationFolder).orElse(CONNECTOR_IMPL_FOLDER);
    }

    public String getLibFolder() {
        return Optional.ofNullable(libFolder).orElse(LIB_FOLDER);
    }
}

package org.bonitasoft.maven.connector;

import lombok.Data;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
public class Connector {

    public static final String CONNECTOR_DEF_FOLDER = "connectors-def";
    public static final String CONNECTOR_IMPL_FOLDER = "connectors-impl";
    public static final String LIB_FOLDER = "lib";

    @Parameter(name = "connector.includes", property = "connector.includes")
    private Set<String> includes = new HashSet<>();
    @Parameter(name = "connector.def", property = "connector.def", defaultValue = CONNECTOR_DEF_FOLDER)
    private String definitionFolder = CONNECTOR_DEF_FOLDER;
    @Parameter(name = "connector.impl", property = "connector.impl", defaultValue = CONNECTOR_IMPL_FOLDER)
    private String implementationFolder = CONNECTOR_IMPL_FOLDER;
    @Parameter(name = "connector.lib", property = "connector.lib", defaultValue = LIB_FOLDER)
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

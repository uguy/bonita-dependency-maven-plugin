package org.bonitasoft.maven.dependency.exception;

/**
 * Default Bonita Mojo Exception
 */
public class BonitaMojoException extends RuntimeException {

    public BonitaMojoException(String message) {
        super(message);
    }

    public BonitaMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}

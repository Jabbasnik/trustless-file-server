package pl.jackowiak.trustlessfileserver.application.model;

/**
 * The DTO object used for hashes endpoint handling.
 */
public record Hashes(String hash, int pieces) {
}

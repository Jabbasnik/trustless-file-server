package pl.jackowiak.trustlessfileserver.application.model;

import java.util.List;

/**
 * The DTO object used for pieces endpoint handling.
 */
public record PieceData(String content, List<String> proofs) {
}

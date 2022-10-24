package pl.jackowiak.trustlessfileserver.application.model;

import java.util.List;

public record PieceData(String content, List<String> proofs) {
}

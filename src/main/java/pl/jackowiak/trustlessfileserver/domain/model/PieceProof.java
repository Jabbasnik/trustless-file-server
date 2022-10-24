package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.List;

/**
 * The piece proof.
 */
public record PieceProof(MerkleEncoded content, List<MerkleTreeElement> proofs) {
}

package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.List;

public record PieceProof(MerkleEncoded content, List<MerkleTreeElement> proofs) {
//TODO: e&h & tostr
}

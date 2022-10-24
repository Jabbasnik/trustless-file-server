package pl.jackowiak.trustlessfileserver.domain.ports.in;

import io.vavr.control.Either;
import pl.jackowiak.trustlessfileserver.domain.model.DomainFailure;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleHash;
import pl.jackowiak.trustlessfileserver.domain.model.PieceProof;

import java.util.Map;

/**
 * Handles action related to serving application endpoints.
 */
public interface ServerFiles {
    Map<MerkleHash, Integer> availableFiles();

    Either<DomainFailure, PieceProof> getProofForPiece(MerkleHash merkleTreeHash, int pieceIndex);
}

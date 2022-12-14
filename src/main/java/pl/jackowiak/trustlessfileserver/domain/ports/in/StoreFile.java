package pl.jackowiak.trustlessfileserver.domain.ports.in;

import pl.jackowiak.trustlessfileserver.domain.model.MerkleTree;
import pl.jackowiak.trustlessfileserver.domain.model.PersistPiece;
import pl.jackowiak.trustlessfileserver.domain.model.PieceHash;

import java.util.List;

/**
 * Handles action related to persisting files.
 */
public interface StoreFile {

    PieceHash persistPiece(PersistPiece piece);

    MerkleTree storeAsMerkleTree(List<PieceHash> storedPiecesHashes);
}

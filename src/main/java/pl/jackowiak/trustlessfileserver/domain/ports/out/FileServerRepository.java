package pl.jackowiak.trustlessfileserver.domain.ports.out;

import pl.jackowiak.trustlessfileserver.domain.model.MerkleEncoded;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleHash;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleTree;
import pl.jackowiak.trustlessfileserver.domain.model.PersistPiece;
import pl.jackowiak.trustlessfileserver.domain.model.PieceHash;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The repository layer used to store and retrieve file server data.
 */
public interface FileServerRepository {
    PieceHash persistPieces(PersistPiece persistPiece);

    void persistMerkleTree(MerkleTree hash, List<PieceHash> storedPiecesHashes);

    Map<MerkleHash, Integer> getAvailableMerkleTreesWithNumberOfPieces();

    Optional<MerkleTree> getMerkleTreeByHash(MerkleHash merkleTreeHash);

    Optional<List<PieceHash>> getPieceHashByMerkleHash(MerkleHash merkleRootHash);

    Optional<MerkleEncoded> getPieceContentByPieceHash(PieceHash pieceHash);
}

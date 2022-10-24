package pl.jackowiak.trustlessfileserver.infrastructure;

import pl.jackowiak.trustlessfileserver.domain.model.MerkleEncoded;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleHash;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleTree;
import pl.jackowiak.trustlessfileserver.domain.model.PersistPiece;
import pl.jackowiak.trustlessfileserver.domain.model.PieceHash;
import pl.jackowiak.trustlessfileserver.domain.ports.out.FileServerRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public class InMemoryFileServerRepository implements FileServerRepository {

    private final Map<MerkleHash, List<PieceHash>> MERKLE_DB = new ConcurrentHashMap<>();

    private final Map<MerkleHash, MerkleTree> FULL_MERKLE_DB = new ConcurrentHashMap<>();

    private final Map<MerkleHash, MerkleEncoded> PIECES_DB = new ConcurrentHashMap<>();

    @Override
    public PieceHash persistPieces(PersistPiece persistPiece) {
        PIECES_DB.put(persistPiece.getMerkleHash(), persistPiece.getMerkleEncoded());
        return new PieceHash(persistPiece.getMerkleHash());
    }

    @Override
    public void persistMerkleTree(MerkleTree merkleTree, List<PieceHash> storedPiecesHashes) {
        var hash = merkleTree.root().hash();
        MERKLE_DB.put(hash, storedPiecesHashes);
        FULL_MERKLE_DB.put(hash, merkleTree);
    }

    @Override
    public Map<MerkleHash, Integer> getAvailableMerkleTreesWithNumberOfPieces() {
        return MERKLE_DB.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
    }

    @Override
    public Optional<MerkleTree> getMerkleTreeByHash(MerkleHash merkleTreeHash) {
        return ofNullable(FULL_MERKLE_DB.get(merkleTreeHash));
    }

    @Override
    public Optional<List<PieceHash>> getPieceHashByMerkleHash(MerkleHash merkleRootHash) {
        return ofNullable(MERKLE_DB.get(merkleRootHash));
    }

    @Override
    public Optional<MerkleEncoded> getPieceContentByPieceHash(PieceHash pieceHash) {
        return ofNullable(PIECES_DB.get(pieceHash.merkleHash()));
    }

    public void testPersistMerkleTree(MerkleHash hash, List<PieceHash> storedPiecesHashes) {
        MERKLE_DB.put(hash, storedPiecesHashes);
    }

}

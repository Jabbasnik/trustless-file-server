package pl.jackowiak.trustlessfileserver.domain;

import io.vavr.control.Either;
import pl.jackowiak.trustlessfileserver.domain.model.DomainFailure;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleHash;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleTree;
import pl.jackowiak.trustlessfileserver.domain.model.PersistPiece;
import pl.jackowiak.trustlessfileserver.domain.model.PieceHash;
import pl.jackowiak.trustlessfileserver.domain.model.PieceProof;
import pl.jackowiak.trustlessfileserver.domain.ports.in.ServerFiles;
import pl.jackowiak.trustlessfileserver.domain.ports.in.StoreFile;
import pl.jackowiak.trustlessfileserver.domain.ports.out.FileServerRepository;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * The Facade which servers as an entry point to domain.
 */
public class Facade implements StoreFile, ServerFiles {

    private final FileServerRepository fileServerRepository;
    private final ProofCreator proofCreator;

    public Facade(FileServerRepository fileServerRepository) {
        this.fileServerRepository = fileServerRepository;
        this.proofCreator = new ProofCreator(fileServerRepository);
    }

    @Override
    public PieceHash persistPiece(@Nonnull PersistPiece piece) {
        return fileServerRepository.persistPieces(requireNonNull(piece));
    }

    @Override
    public MerkleTree storeAsMerkleTree(List<PieceHash> pieceHashes) {
        if (pieceHashes.isEmpty())
            throw new IllegalArgumentException("Piece hashes for merkle tree creation cannot be empty!");
        var merkleTree = new MerkleTree(pieceHashes);
        fileServerRepository.persistMerkleTree(merkleTree, pieceHashes);
        return merkleTree;
    }

    @Override
    public Map<MerkleHash, Integer> availableFiles() {
        return fileServerRepository.getAvailableMerkleTreesWithNumberOfPieces();
    }

    @Override
    public Either<DomainFailure, PieceProof> getProofForPiece(MerkleHash merkleTreeHash, int pieceIndex) {
        return proofCreator.createProofForPiece(merkleTreeHash, pieceIndex);
    }
}

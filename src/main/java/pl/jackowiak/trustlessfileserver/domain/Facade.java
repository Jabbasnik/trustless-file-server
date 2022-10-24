package pl.jackowiak.trustlessfileserver.domain;

import io.vavr.control.Either;
import pl.jackowiak.trustlessfileserver.domain.model.DomainFailure;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleEncoded;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleHash;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleLeaf;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleTree;
import pl.jackowiak.trustlessfileserver.domain.model.PersistPiece;
import pl.jackowiak.trustlessfileserver.domain.model.PieceHash;
import pl.jackowiak.trustlessfileserver.domain.model.PieceProof;
import pl.jackowiak.trustlessfileserver.domain.ports.in.ServerFiles;
import pl.jackowiak.trustlessfileserver.domain.ports.in.StoreFile;
import pl.jackowiak.trustlessfileserver.domain.ports.out.FileServerRepository;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static pl.jackowiak.trustlessfileserver.domain.model.FailureMessages.MERKLE_TREE_NOT_FOUND_REPO;
import static pl.jackowiak.trustlessfileserver.domain.model.FailureMessages.PIECE_CONTENT_NOT_FOUND_IN_REPO;
import static pl.jackowiak.trustlessfileserver.domain.model.FailureMessages.PIECE_NOT_FOUND_IN_MERKLE_TREE;
import static pl.jackowiak.trustlessfileserver.domain.model.FailureMessages.PIECE_NOT_FOUND_IN_REPO;
import static pl.jackowiak.trustlessfileserver.domain.model.MerkleProof.checkIfLeafInTree;
import static pl.jackowiak.trustlessfileserver.domain.model.MerkleProof.createProofTree;
import static pl.jackowiak.trustlessfileserver.domain.model.MerkleProof.getProofElements;

public class Facade implements StoreFile, ServerFiles {

    private final FileServerRepository fileServerRepository;

    public Facade(FileServerRepository fileServerRepository) {
        this.fileServerRepository = fileServerRepository;
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
        return fileServerRepository.getPieceHashByMerkleHash(merkleTreeHash)
                .filter(pieces -> pieces.size() >= pieceIndex + 1)
                .map(pieces -> pieces.get(pieceIndex))
                .map(Either::<DomainFailure, PieceHash>right)
                .map(pieceHash -> onPieceHashPresent(pieceHash, merkleTreeHash))
                .orElseGet(() -> Either.left(new DomainFailure(PIECE_NOT_FOUND_IN_REPO.formatted(merkleTreeHash.getHexString(), pieceIndex))));
    }

    private Either<DomainFailure, PieceProof> onPieceHashPresent(Either<DomainFailure, PieceHash> pieceHashEither, MerkleHash merkleTreeHash) {
        return fileServerRepository.getMerkleTreeByHash(merkleTreeHash)
                .map(Either::<DomainFailure, MerkleTree>right)
                .map(merkleTree -> onMerkleTreePresent(merkleTree, pieceHashEither.get()))
                .orElseGet(() -> Either.left(new DomainFailure(MERKLE_TREE_NOT_FOUND_REPO.formatted(merkleTreeHash.getHexString()))));
    }

    private Either<DomainFailure, PieceProof> onMerkleTreePresent(Either<DomainFailure, MerkleTree> merkleTreeEither, PieceHash pieceHash) {
        return merkleTreeEither.filter(merkleTree -> checkIfLeafInTree(new MerkleLeaf(pieceHash.merkleHash()), merkleTree.root()))
                .map(merkleTree -> onPieceHashPresentInMerkleTree(merkleTree, pieceHash))
                .getOrElse(() -> Either.left(new DomainFailure(PIECE_NOT_FOUND_IN_MERKLE_TREE.formatted(pieceHash.merkleHash().getHexString()))));
    }

    private Either<DomainFailure, PieceProof> onPieceHashPresentInMerkleTree(Either<DomainFailure, MerkleTree> merkleTreeEither, PieceHash pieceHash) {
        return fileServerRepository.getPieceContentByPieceHash(pieceHash)
                .map(Either::<DomainFailure, MerkleEncoded>right)
                .map(merkleEncoded -> onMerklePieceContentPresent(merkleEncoded, merkleTreeEither.get(), pieceHash))
                .orElseGet(() -> Either.left(new DomainFailure(PIECE_CONTENT_NOT_FOUND_IN_REPO.formatted(pieceHash.merkleHash().getHexString()))));
    }

    private Either<DomainFailure, PieceProof> onMerklePieceContentPresent(Either<DomainFailure, MerkleEncoded> merkleEncodedEither, MerkleTree merkleTree, PieceHash pieceHash) {
        var leafToBeChecked = new MerkleLeaf(pieceHash.merkleHash());
        var proofTree = createProofTree(merkleTree.root(), leafToBeChecked);
        var proofElements = getProofElements(proofTree, leafToBeChecked);
        Collections.reverse(proofElements);
        return Either.right(new PieceProof(merkleEncodedEither.get(), proofElements));
    }
}

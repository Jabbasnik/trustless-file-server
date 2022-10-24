package pl.jackowiak.trustlessfileserver.domain;

import io.vavr.control.Either;
import pl.jackowiak.trustlessfileserver.domain.model.DomainFailure;
import pl.jackowiak.trustlessfileserver.domain.model.FailureMessages;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleEncoded;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleHash;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleLeaf;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleProof;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleTree;
import pl.jackowiak.trustlessfileserver.domain.model.PieceHash;
import pl.jackowiak.trustlessfileserver.domain.model.PieceProof;
import pl.jackowiak.trustlessfileserver.domain.ports.out.FileServerRepository;

import java.util.Collections;

import static pl.jackowiak.trustlessfileserver.domain.model.FailureMessages.PIECE_NOT_FOUND_IN_REPO;

class ProofCreator {

    private final FileServerRepository fileServerRepository;

    ProofCreator(FileServerRepository fileServerRepository) {
        this.fileServerRepository = fileServerRepository;
    }

    Either<DomainFailure, PieceProof> createProofForPiece(MerkleHash merkleTreeHash, int pieceIndex) {
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
                .orElseGet(() -> Either.left(new DomainFailure(FailureMessages.MERKLE_TREE_NOT_FOUND_REPO.formatted(merkleTreeHash.getHexString()))));
    }

    private Either<DomainFailure, PieceProof> onMerkleTreePresent(Either<DomainFailure, MerkleTree> merkleTreeEither, PieceHash pieceHash) {
        return merkleTreeEither.filter(merkleTree -> MerkleProof.checkIfLeafInTree(new MerkleLeaf(pieceHash.merkleHash()), merkleTree.root()))
                .map(merkleTree -> onPieceHashPresentInMerkleTree(merkleTree, pieceHash))
                .getOrElse(() -> Either.left(new DomainFailure(FailureMessages.PIECE_NOT_FOUND_IN_MERKLE_TREE.formatted(pieceHash.merkleHash().getHexString()))));
    }

    private Either<DomainFailure, PieceProof> onPieceHashPresentInMerkleTree(Either<DomainFailure, MerkleTree> merkleTreeEither, PieceHash pieceHash) {
        return fileServerRepository.getPieceContentByPieceHash(pieceHash)
                .map(Either::<DomainFailure, MerkleEncoded>right)
                .map(merkleEncoded -> onMerklePieceContentPresent(merkleEncoded, merkleTreeEither.get(), pieceHash))
                .orElseGet(() -> Either.left(new DomainFailure(FailureMessages.PIECE_CONTENT_NOT_FOUND_IN_REPO.formatted(pieceHash.merkleHash().getHexString()))));
    }

    private Either<DomainFailure, PieceProof> onMerklePieceContentPresent(Either<DomainFailure, MerkleEncoded> merkleEncodedEither, MerkleTree merkleTree, PieceHash pieceHash) {
        var leafToBeChecked = new MerkleLeaf(pieceHash.merkleHash());
        var proofTree = MerkleProof.createProofTree(merkleTree.root(), leafToBeChecked);
        var proofElements = MerkleProof.getProofElements(proofTree, leafToBeChecked);
        Collections.reverse(proofElements);
        return Either.right(new PieceProof(merkleEncodedEither.get(), proofElements));
    }
}
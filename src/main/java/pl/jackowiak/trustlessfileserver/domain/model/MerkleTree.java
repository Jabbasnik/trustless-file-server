package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.primitives.Bytes.concat;
import static java.lang.Integer.highestOneBit;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static pl.jackowiak.trustlessfileserver.domain.model.MerkleUtils.logDebug;

/**
 * The Merkle tree.
 */
public final class MerkleTree {

    private final MerkleTreeElement root;

    public MerkleTree(List<PieceHash> piecesHashes) {
        logDebug("Creating Merkle tree for: <%d> piece(s)".formatted(piecesHashes.size()));
        this.root = createMerkleTree(piecesHashes);
        logDebug("Merkle tree created with root hash: <%s>".formatted(encodeHexString(root.hash.getHash())));
    }

    public MerkleTreeElement root() {
        return root;
    }

    private MerkleTreeElement createMerkleTree(List<PieceHash> piecesHashes) {
        var leafs = mapToLeaf(piecesHashes);
        var hashingAlgo = determineHashingAlgo(piecesHashes);
        var balancedLeafs = balanceWithEmptyLeafs(leafs);
        return buildMerkleTree(balancedLeafs, hashingAlgo);
    }

    private HashingAlgo determineHashingAlgo(List<PieceHash> piecesHashes) {
        return piecesHashes.stream()
                .map(PieceHash::merkleHash)
                .map(MerkleHash::getHashingAlgo)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not determine common hashing algorithm."));
    }

    private MerkleTreeElement buildMerkleTree(List<? extends MerkleTreeElement> previousLevelElements, HashingAlgo hashingAlgo) {
        if (previousLevelElements.size() == 1) {
            return previousLevelElements.get(0);
        } else {
            var newLevelElements = new ArrayList<MerkleTreeElement>();
            for (var i = 0; i <= previousLevelElements.size() - 2; i += 2) {
                var leftElement = previousLevelElements.get(i);
                var rightElement = previousLevelElements.get(i + 1);
                var concatenatedHashes = concat(leftElement.hash.getHash(), rightElement.hash.getHash());
                var merkleHash = new MerkleHash(concatenatedHashes, hashingAlgo);
                var newNode = new MerkleNode(merkleHash, leftElement, rightElement);
                newLevelElements.add(newNode);
            }
            return buildMerkleTree(newLevelElements, hashingAlgo);
        }
    }

    private List<MerkleLeaf> mapToLeaf(List<PieceHash> pieceHashes) {
        return pieceHashes.stream()
                .map(PieceHash::merkleHash)
                .map(MerkleLeaf::new)
                .collect(toCollection(ArrayList::new));
    }

    private List<? extends MerkleTreeElement> balanceWithEmptyLeafs(List<MerkleLeaf> leafs) {
        var numberOfLeafs = leafs.size();
        var nextPowerOfTwo = highestOneBit(numberOfLeafs - 1) * 2;
        var fillerLeaf = new byte[32];
        logDebug("Filling tree with <%d> element(s).".formatted(nextPowerOfTwo - leafs.size()));
        while (leafs.size() < nextPowerOfTwo) {
            leafs.add(new MerkleLeaf(MerkleHash.rawHash(fillerLeaf)));
        }
        return leafs;
    }
}


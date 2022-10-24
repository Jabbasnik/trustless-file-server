package pl.jackowiak.trustlessfileserver.domain.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

/**
 * Creates proofs for given merkle trees and checks for leaf inclusion. Creates Merkle Proof Tree which is then used to
 * determine final leaf proofs.
 */
public final class MerkleProof {

    private MerkleProof() {
        throw new AssertionError(format("The class \"%s\" is not instantiable", this.getClass()));
    }

    public static boolean checkIfLeafInTree(MerkleLeaf leaf, MerkleTreeElement rootNode) {
        if (rootNode instanceof MerkleLeaf) {
            return Arrays.equals(rootNode.hash.getHash(), leaf.hash.getHash());
        } else if (rootNode instanceof MerkleNode node) {
            if (checkIfLeafInTree(leaf, node.left())) {
                return true;
            }
            return checkIfLeafInTree(leaf, node.right());
        }
        return false;
    }

    public static MerkleTreeElement createProofTree(MerkleTreeElement rootNode, MerkleLeaf leafToBeChecked) {
        var proofTree = createProof(rootNode, leafToBeChecked);
        return proofTree.getRight();
    }

    public static List<MerkleTreeElement> getProofElements(MerkleTreeElement proofTree, MerkleLeaf leafToBeChecked) {
        var elements = new ArrayDeque<>(singletonList(proofTree));
        var proofs = new ArrayList<MerkleTreeElement>();
        while (!elements.isEmpty()) {
            var element = elements.poll();
            if (element instanceof MerkleNode node) {
                if (node.left() != null) {
                    elements.add(node.left());
                }
                if (node.right() != null) {
                    elements.add(node.right());
                }
            } else if (element instanceof MerkleLeaf leaf && !leafToBeChecked.equals(leaf)) {
                proofs.add(leaf);
            }
        }
        return proofs;
    }

    private static Pair<Boolean, MerkleTreeElement> createProof(MerkleTreeElement rootNode, MerkleLeaf leafToBeChecked) {
        if (rootNode instanceof MerkleLeaf leaf) {
            return checkForDesiredLeaf(leafToBeChecked, leaf);
        } else if (rootNode instanceof MerkleNode node) {
            return createSubTree(leafToBeChecked, node);
        }
        throw new IllegalStateException("Reached out of recursion context!");
    }

    private static Pair<Boolean, MerkleTreeElement> createSubTree(MerkleLeaf leafToBeChecked, MerkleNode node) {
        var leftNode = createProof(node.left(), leafToBeChecked);
        var rightNode = createProof(node.right(), leafToBeChecked);
        if (leftNode.getLeft() || rightNode.getLeft()) {
            var newTree = new MerkleNode(node.hash, leftNode.getRight(), rightNode.getRight());
            return Pair.of(true, newTree);
        } else {
            var newTree = new MerkleLeaf(node.hash);
            return Pair.of(false, newTree);
        }
    }

    private static Pair<Boolean, MerkleTreeElement> checkForDesiredLeaf(MerkleLeaf leafToBeChecked, MerkleLeaf leaf) {
        if (leaf.equals(leafToBeChecked)) {
            return Pair.of(true, new MerkleLeaf(leaf.hash));
        } else {
            return Pair.of(false, new MerkleLeaf(leaf.hash));
        }
    }
}

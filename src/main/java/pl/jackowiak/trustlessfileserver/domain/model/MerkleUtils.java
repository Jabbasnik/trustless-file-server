package pl.jackowiak.trustlessfileserver.domain.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

/**
 * The merkle utils.
 */
public final class MerkleUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MerkleUtils.class);

    private MerkleUtils() {
        throw new AssertionError(format("The class \"%s\" is not instantiable", this.getClass()));
    }

    public static void printTree(MerkleTreeElement root) {
        var elements = new ArrayDeque<>(singletonList(root));
        int numberOfElementsInLevel = 0;
        int level = 0;
        while (!elements.isEmpty()) {
            numberOfElementsInLevel++;
            var element = elements.poll();
            if (element instanceof MerkleNode node) {
                LOG.info("Type: NODE | Level: %d | hash: %s".formatted(level, element.hash.getHexString()));
                if (node.left() != null) {
                    elements.add(node.left());
                }
                if (node.right() != null) {
                    elements.add(node.right());
                }
            } else LOG.info("Type: LEAF | Level: %d | hash: %s".formatted(level, element.hash.getHexString()));
            if (numberOfElementsInLevel >= Math.pow(2, level)) {
                numberOfElementsInLevel = 0;
                level++;
            }
        }
    }

    public static void logDebug(String message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }
}

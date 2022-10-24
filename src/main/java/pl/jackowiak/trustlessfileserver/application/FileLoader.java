package pl.jackowiak.trustlessfileserver.application;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleTree;
import pl.jackowiak.trustlessfileserver.domain.model.PersistPiece;
import pl.jackowiak.trustlessfileserver.domain.model.PieceHash;
import pl.jackowiak.trustlessfileserver.domain.ports.in.StoreFile;

import javax.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_256;

@Component
class FileLoader {

    private static final Logger LOG = LoggerFactory.getLogger(FileLoader.class);
    private static final String DEFAULT_FILE = "/icons_rgb_circle.png";
    private static final String DEFAULT_HASHING_ALGO = SHA_256;
    private static final String DEFAULT_ENCODING_ALGO = "BASE_64";
    private static final int DEFAULT_PIECE_SIZE = 1024;
    private final Environment env;
    private final StoreFile storeFile;

    FileLoader(StoreFile storeFile, Environment env) {
        this.storeFile = storeFile;
        this.env = env;
    }

    @PostConstruct
    public void run() {
        var filePath = env.getProperty("filePath");
        LOG.info("Arguments: " + filePath);
        Try.of(() -> storeFilePieces(filePath))
                .mapTry(storeFile::storeAsMerkleTree)
                .andThen(this::reportSuccess)
                .orElseRun(this::reportFailure);
    }

    private List<PieceHash> storeFilePieces(String path) {
        try (var resourceAsStream = loadFile(path); var resourceStream = new BufferedInputStream(resourceAsStream)) {
            return storePieces(resourceStream);
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "There was an error while trying to store file. File path: <%s>. Reason: <%s>".formatted(path, exception.getMessage()), exception);
        }
    }

    private InputStream loadFile(String path) throws IOException {
        if (path == null) {
            LOG.info("Run with default file path: %s".formatted(DEFAULT_FILE));
            return ofNullable(FileLoader.class.getResourceAsStream(DEFAULT_FILE))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "There was an error reading file from path: <%s>. Reason: <File not found>".formatted(DEFAULT_FILE)));
        }
        LOG.info("Run with file path: %s".formatted(path));
        return Files.newInputStream(Paths.get(path));
    }

    private List<PieceHash> storePieces(BufferedInputStream stream) throws IOException {
        var storedPiecesHashes = new ArrayList<PieceHash>();
        byte[] piece;
        while (true) {
            piece = stream.readNBytes(DEFAULT_PIECE_SIZE);
            if (piece.length == 0) break;
            var persistPiece = new PersistPiece(piece, DEFAULT_HASHING_ALGO, DEFAULT_ENCODING_ALGO);
            var storedPieceHash = storeFile.persistPiece(persistPiece);
            storedPiecesHashes.add(storedPieceHash);
        }
        return storedPiecesHashes;
    }

    private void reportSuccess(MerkleTree merkleTree) {
        LOG.info("Merkle tree created with root hash: <%s>".formatted(merkleTree.root().hash().getHexString()));
    }

    private void reportFailure(Throwable throwable) {
        LOG.info("Merkle tree could not be created. Reason: %s".formatted(throwable.getMessage()));
    }
}

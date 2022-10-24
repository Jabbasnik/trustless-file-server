package pl.jackowiak.trustlessfileserver.domain;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.jackowiak.trustlessfileserver.domain.model.DomainFailure;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleEncoded;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleHash;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleLeaf;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleNode;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleTreeElement;
import pl.jackowiak.trustlessfileserver.domain.model.PersistPiece;
import pl.jackowiak.trustlessfileserver.domain.model.PieceHash;
import pl.jackowiak.trustlessfileserver.domain.model.PieceProof;
import pl.jackowiak.trustlessfileserver.infrastructure.InMemoryFileServerRepository;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.data.Index.atIndex;
import static org.junit.jupiter.params.provider.Arguments.of;
import static pl.jackowiak.trustlessfileserver.domain.model.EncodingAlgo.BASE_64;
import static pl.jackowiak.trustlessfileserver.domain.model.FailureMessages.MERKLE_TREE_NOT_FOUND_REPO;
import static pl.jackowiak.trustlessfileserver.domain.model.FailureMessages.PIECE_CONTENT_NOT_FOUND_IN_REPO;
import static pl.jackowiak.trustlessfileserver.domain.model.FailureMessages.PIECE_NOT_FOUND_IN_REPO;
import static pl.jackowiak.trustlessfileserver.domain.model.HashingAlgo.SHA_256;
import static pl.jackowiak.trustlessfileserver.domain.model.MerkleUtils.printTree;

class FacadeTest {

    private static final List<byte[]> DUMMY_BYTES = List.of(
            "Number 1".getBytes(),
            "Number 2".getBytes(),
            "Number 3".getBytes(),
            "Number 4".getBytes(),
            "Number 5".getBytes()
    );
    private InMemoryFileServerRepository repository;
    private List<PieceHash> dummyPieces;
    private Facade sut;

    private static Stream<Arguments> expectedProofs() {
        return Stream.of(
                of(0, "ad28403fa6ea4c31c14c70cda95275e87cb8e9b29e9da7414e330654aa95d815",
                        "44056535cf9d76c21e18b004e4a9213b9d925aa42801fb15a7878452811f9fd7",
                        "e2ed7a1cc39fdc95c3210ba97d9deae6c9abcb73cde03f14b1d66115b5930133"),
                of(2, "7286958bee27846f87ed84116050b5563ba1e763551d5d3936117a6ba0858d3e",
                        "f40ba93b3e256b4aa516aec3e28564c05a557eb98702eb9c1d4ef1c2073fc360",
                        "e2ed7a1cc39fdc95c3210ba97d9deae6c9abcb73cde03f14b1d66115b5930133"),
                of(4, "0000000000000000000000000000000000000000000000000000000000000000",
                        "f5a5fd42d16a20302798ef6ed309979b43003d2320d9f0e8ea9831a92759fb4b",
                        "f6c035e3f6ae304787471f195875f0acab3966d9c8f136a1b15db391563a5e75")
        );
    }

    @BeforeEach
    void setUp() {
        repository = new InMemoryFileServerRepository();
        this.sut = new Facade(repository);
        dummyPieces = DUMMY_BYTES.stream()
                .map(bytes -> new PieceHash(new MerkleHash(bytes, SHA_256)))
                .toList();
    }

    @Test
    void shouldThrowNPEWhenTryingToStoreWithNullPersistPiece() {
        // GIVEN
        // WHEN
        assertThatNullPointerException().isThrownBy(() -> sut.persistPiece(null));

        //THEN - exception
    }

    @Test
    void shouldThrowIAEWhenHashingAlgorithmNotSupported() {
        // GIVEN
        var bytes = "Number 1".getBytes();

        // WHEN
        assertThatIllegalArgumentException().isThrownBy(() -> new PersistPiece(bytes, "not-supported-algo", "BASE_64"));

        //THEN - exception
    }

    @Test
    void shouldThrowIAEWhenEncodingAlgorithmNotSupported() {
        // GIVEN
        var bytes = "Number 1".getBytes();

        // WHEN
        assertThatIllegalArgumentException().isThrownBy(() -> new PersistPiece(bytes, "SHA-256", "not-supported-algo"));

        //THEN - exception
    }

    @Test
    void shouldStorePieceInRepositoryWhenCalledWithValidPersistPiece() {
        // GIVEN
        var bytes = "Number 1".getBytes();
        var piece = new PersistPiece(bytes, "SHA-256", "BASE_64");
        var expectedPieceHash = new PieceHash(new MerkleHash(bytes, SHA_256));
        var expectedContent = new MerkleEncoded(BASE_64, bytes);

        // WHEN
        var pieceHash = sut.persistPiece(piece);

        //THEN
        var pieceContent = repository.getPieceContentByPieceHash(expectedPieceHash)
                .orElseGet(() -> fail("Piece was not persisted"));
        var soft = new SoftAssertions();
        soft.assertThat(pieceHash).isEqualTo(expectedPieceHash);
        soft.assertThat(pieceContent).isEqualTo(expectedContent);
        soft.assertAll();
    }

    @Test
    void shouldThrowIAEWhenTryingToStoreMerkleTreeWithEmptyPiecesList() {
        // GIVEN

        // WHEN
        assertThatIllegalArgumentException().isThrownBy(() -> sut.storeAsMerkleTree(List.of()));

        //THEN - exception
    }

    @Test
    void shouldStoreMerkleTreeInRepositoryWhenCalledWithValidPieces() throws DecoderException {
        // GIVEN
        var expectedRootHashString = "5df5a63d861485d6c4c804a509712e769d88d4c2c8a948e65b83213786c09755";
        var expectedRootHash = MerkleHash.rawHash(Hex.decodeHex(expectedRootHashString));

        // WHEN
        var merkleTree = sut.storeAsMerkleTree(dummyPieces);

        //THEN
        var merkleFromRepository = repository.getMerkleTreeByHash(expectedRootHash)
                .orElseGet(() -> fail("Merkle tree was not stored"));
        var soft = new SoftAssertions();
        soft.assertThat(expectedRootHash).isEqualTo(merkleTree.root().hash());
        soft.assertThat(merkleFromRepository.root()).isEqualTo(merkleTree.root());
        soft.assertAll();
    }

    @Test
    void shouldStoreMerkleTreeWithExpectedStructureWhenCallingStore() throws DecoderException {
        // GIVEN
        var expectedRootHashString = "5df5a63d861485d6c4c804a509712e769d88d4c2c8a948e65b83213786c09755";
        var expectedRootHash = MerkleHash.rawHash(Hex.decodeHex(expectedRootHashString));

        // WHEN
        var merkleTree = sut.storeAsMerkleTree(dummyPieces);

        //THEN
        printTree(merkleTree.root());

        var soft = new SoftAssertions();
        // root
        soft.assertThat(expectedRootHash.getHexString()).isEqualTo(expectedRootHashString);

        // root childs nodes
        soft.assertThat(((MerkleNode) merkleTree.root()).left().hash().getHexString()).isEqualTo("f6c035e3f6ae304787471f195875f0acab3966d9c8f136a1b15db391563a5e75");
        soft.assertThat(((MerkleNode) merkleTree.root()).right().hash().getHexString()).isEqualTo("e2ed7a1cc39fdc95c3210ba97d9deae6c9abcb73cde03f14b1d66115b5930133");

        //root grand child nodes
        soft.assertThat(((MerkleNode) ((MerkleNode) merkleTree.root()).left()).left().hash().getHexString()).isEqualTo("f40ba93b3e256b4aa516aec3e28564c05a557eb98702eb9c1d4ef1c2073fc360");
        soft.assertThat(((MerkleNode) ((MerkleNode) merkleTree.root()).left()).right().hash().getHexString()).isEqualTo("44056535cf9d76c21e18b004e4a9213b9d925aa42801fb15a7878452811f9fd7");
        soft.assertThat(((MerkleNode) ((MerkleNode) merkleTree.root()).right()).left().hash().getHexString()).isEqualTo("0ab1aa676a457124f351455896d973e637abc3d14b4df144847f681f6ed8988d");
        soft.assertThat(((MerkleNode) ((MerkleNode) merkleTree.root()).right()).right().hash().getHexString()).isEqualTo("f5a5fd42d16a20302798ef6ed309979b43003d2320d9f0e8ea9831a92759fb4b");

        // leafs
        soft.assertThat(((MerkleLeaf) ((MerkleNode) ((MerkleNode) ((MerkleNode) merkleTree.root()).left()).left()).left()).hash().getHexString()).isEqualTo("de9222aa8821b29c4f1aab37c97d604ab3f4d2e1f16ed0c897f1e048304a3688");
        soft.assertThat(((MerkleLeaf) ((MerkleNode) ((MerkleNode) ((MerkleNode) merkleTree.root()).left()).left()).right()).hash().getHexString()).isEqualTo("ad28403fa6ea4c31c14c70cda95275e87cb8e9b29e9da7414e330654aa95d815");
        soft.assertThat(((MerkleLeaf) ((MerkleNode) ((MerkleNode) ((MerkleNode) merkleTree.root()).left()).right()).left()).hash().getHexString()).isEqualTo("eaf44a47d326731e66abd02faf3d705c0b40d69bb2c27ddf73cd569fd6929794");
        soft.assertThat(((MerkleLeaf) ((MerkleNode) ((MerkleNode) ((MerkleNode) merkleTree.root()).left()).right()).right()).hash().getHexString()).isEqualTo("7286958bee27846f87ed84116050b5563ba1e763551d5d3936117a6ba0858d3e");
        soft.assertThat(((MerkleLeaf) ((MerkleNode) ((MerkleNode) ((MerkleNode) merkleTree.root()).right()).left()).left()).hash().getHexString()).isEqualTo("0414ba35d49928854f70b0f7df8c1c7b8ea2c5b0fd02df8a46ee6452a5666442");
        soft.assertThat(((MerkleLeaf) ((MerkleNode) ((MerkleNode) ((MerkleNode) merkleTree.root()).right()).left()).right()).hash().getHexString()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000");
        soft.assertThat(((MerkleLeaf) ((MerkleNode) ((MerkleNode) ((MerkleNode) merkleTree.root()).right()).right()).left()).hash().getHexString()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000");
        soft.assertThat(((MerkleLeaf) ((MerkleNode) ((MerkleNode) ((MerkleNode) merkleTree.root()).right()).right()).right()).hash().getHexString()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000");

        soft.assertAll();
    }

    @Test
    void shouldReturnEmptyMapWhenNoFilesStoredInRepository() {
        // GIVEN

        // WHEN
        var availableFiles = sut.availableFiles();

        //THEN
        assertThat(availableFiles).isEmpty();
    }

    @Test
    void shouldReturnMapWithMerkleHashAndCorrectNumberOfPiecesWhenFileStoredInRepository() {
        // GIVEN
        var merkleTree = sut.storeAsMerkleTree(dummyPieces);

        // WHEN
        var availableFiles = sut.availableFiles();

        //THEN
        var soft = new SoftAssertions();
        soft.assertThat(availableFiles).hasSize(1);
        soft.assertThat(availableFiles).hasEntrySatisfying(merkleTree.root().hash(), value -> assertThat(value).isEqualTo(5));
        soft.assertAll();
    }

    @Test
    void shouldReturnDomainFailureWhenNoPieceFoundInRepositoryForGivenMerkleHash() throws DecoderException {
        // GIVEN
        var expectedRootHashString = "5df5a63d861485d6c4c804a509712e769d88d4c2c8a948e65b83213786c09755";
        var expectedRootHash = MerkleHash.rawHash(Hex.decodeHex(expectedRootHashString));

        // WHEN
        var availableFiles = sut.getProofForPiece(expectedRootHash, 0);

        //THEN
        var soft = new SoftAssertions();
        soft.assertThat(availableFiles.isLeft()).isTrue();
        soft.assertThat(availableFiles.getLeft()).isEqualTo(new DomainFailure(PIECE_NOT_FOUND_IN_REPO.formatted(expectedRootHashString, 0)));
        soft.assertAll();
    }

    @Test
    void shouldReturnDomainFailureWhenPieceFoundInRepositoryForGivenMerkleHashButIndexNotInBounds() throws DecoderException {
        // GIVEN
        var expectedRootHashString = "5df5a63d861485d6c4c804a509712e769d88d4c2c8a948e65b83213786c09755";
        var expectedRootHash = MerkleHash.rawHash(Hex.decodeHex(expectedRootHashString));
        sut.storeAsMerkleTree(dummyPieces);

        // WHEN
        var availableFiles = sut.getProofForPiece(expectedRootHash, 5);

        //THEN
        var soft = new SoftAssertions();
        soft.assertThat(availableFiles.isLeft()).isTrue();
        soft.assertThat(availableFiles.getLeft()).isEqualTo(new DomainFailure(PIECE_NOT_FOUND_IN_REPO.formatted(expectedRootHashString, 5)));
        soft.assertAll();
    }

    @Test
    void shouldReturnDomainFailureWhenPieceFoundInRepositoryForGivenMerkleHashButNoMerkleTreeInRepo() throws DecoderException {
        // GIVEN
        var expectedRootHashString = "5df5a63d861485d6c4c804a509712e769d88d4c2c8a948e65b83213786c09755";
        var expectedRootHash = MerkleHash.rawHash(Hex.decodeHex(expectedRootHashString));
        var piece = new PersistPiece(DUMMY_BYTES.get(0), "SHA-256", "BASE_64");
        repository.persistPieces(piece);
        repository.testPersistMerkleTree(expectedRootHash, List.of(new PieceHash(piece.getMerkleHash())));

        // WHEN
        var availableFiles = sut.getProofForPiece(expectedRootHash, 0);

        //THEN
        var soft = new SoftAssertions();
        soft.assertThat(availableFiles.isLeft()).isTrue();
        soft.assertThat(availableFiles.getLeft()).isEqualTo(new DomainFailure(MERKLE_TREE_NOT_FOUND_REPO.formatted(expectedRootHashString)));
        soft.assertAll();
    }

    @Test
    void shouldReturnDomainFailureWhenPieceFoundInRepositoryForGivenMerkleHashButPieceHasNoContentInRepo() throws DecoderException {
        // GIVEN
        var expectedPieceHash = "de9222aa8821b29c4f1aab37c97d604ab3f4d2e1f16ed0c897f1e048304a3688";
        var expectedRootHashString = "5df5a63d861485d6c4c804a509712e769d88d4c2c8a948e65b83213786c09755";
        var expectedRootHash = MerkleHash.rawHash(Hex.decodeHex(expectedRootHashString));
        sut.storeAsMerkleTree(dummyPieces);

        // WHEN
        var availableFiles = sut.getProofForPiece(expectedRootHash, 0);

        //THEN
        var soft = new SoftAssertions();
        soft.assertThat(availableFiles.isLeft()).isTrue();
        soft.assertThat(availableFiles.getLeft()).isEqualTo(new DomainFailure(PIECE_CONTENT_NOT_FOUND_IN_REPO.formatted(expectedPieceHash)));
        soft.assertAll();
    }

    @ParameterizedTest
    @MethodSource("expectedProofs")
    void shouldReturnCorrectPieceProofWhenMerkleTreeAndPieceContentsFoundInRepo(int pieceIndex, String siblingHash, String uncleHash, String greatUncleHash) throws DecoderException {
        // GIVEN
        var expectedRootHashString = "5df5a63d861485d6c4c804a509712e769d88d4c2c8a948e65b83213786c09755";
        var expectedRootHash = MerkleHash.rawHash(Hex.decodeHex(expectedRootHashString));
        DUMMY_BYTES.forEach(piece -> sut.persistPiece(new PersistPiece(piece, "SHA-256", "BASE_64")));
        sut.storeAsMerkleTree(dummyPieces);

        // WHEN
        var availableFiles = sut.getProofForPiece(expectedRootHash, pieceIndex);

        //THEN
        var soft = new SoftAssertions();
        soft.assertThat(availableFiles.isRight()).isTrue();
        var proofs = proofsAsStrings(availableFiles.get());
        soft.assertThat(proofs).contains(siblingHash, atIndex(0));
        soft.assertThat(proofs).contains(uncleHash, atIndex(1));
        soft.assertThat(proofs).contains(greatUncleHash, atIndex(2));
        soft.assertThat(proofs).hasSize(3);
        soft.assertAll();
    }

    private List<String> proofsAsStrings(PieceProof pieceProof) {
        return pieceProof.proofs().stream()
                .map(MerkleTreeElement::hash)
                .map(MerkleHash::getHexString)
                .toList();
    }
}
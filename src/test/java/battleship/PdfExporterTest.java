package battleship;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes da exportacao das jogadas para PDF (issue #1).
 */
public class PdfExporterTest {

	@TempDir
	Path tempDir;

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game(Fleet.createRandom());
	}

	@Test
	@DisplayName("Gera o PDF mesmo sem jogadas")
	void exportWithoutMoves() throws IOException {
		File file = tempDir.resolve("vazio.pdf").toFile();
		PdfExporter.export(game, file.getPath());
		assertTrue(file.exists());
		assertTrue(file.length() > 0);
	}

	@Test
	@DisplayName("Gera o PDF depois de algumas rajadas")
	void exportWithMoves() throws IOException {
		game.fireShots(List.of(new Position('A', 1), new Position('B', 2), new Position('C', 3)));
		game.randomEnemyFire();

		File file = tempDir.resolve("jogadas.pdf").toFile();
		PdfExporter.export(game, file.getPath());

		assertEquals(2, game.getAlienMoves().size());
		assertTrue(file.length() > 0);
	}

	@Test
	@DisplayName("Descreve tiros repetidos e fora do tabuleiro")
	void describeResults() {
		game.fireShots(List.of(new Position('A', 1), new Position('A', 1), new Position('K', 5)));
		String text = PdfExporter.describeResults(game.getAlienMoves().get(0));

		assertTrue(text.contains("repetido"));
		assertTrue(text.contains("fora do tabuleiro"));
	}
}

package battleship;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Exporta o historico das jogadas de um jogo para um ficheiro PDF,
 * usando a biblioteca OpenPDF (issue #1).
 */
public class PdfExporter {

	/**
	 * Ficheiro onde o PDF e guardado por omissao.
	 */
	public static final String DEFAULT_FILE = "data/jogadas.pdf";

	private PdfExporter() {
	}

	/**
	 * Gera o PDF com uma tabela com todas as rajadas disparadas contra a nossa frota
	 * e, no fim, o estado da frota.
	 *
	 * @param game     o jogo com as jogadas
	 * @param fileName o caminho do ficheiro PDF a criar
	 * @throws IOException se nao for possivel escrever o ficheiro
	 */
	public static void export(IGame game, String fileName) throws IOException {
		assert game != null;

		Document document = new Document();
		try (FileOutputStream out = new FileOutputStream(fileName)) {
			try {
				write(document, out, game);
			} finally {
				// o documento tem de ser fechado antes do stream, senao o PDF fica vazio
				if (document.isOpen())
					document.close();
			}
		} catch (DocumentException e) {
			throw new IOException("Erro ao criar o PDF", e);
		}
	}

	private static void write(Document document, FileOutputStream out, IGame game) {
		PdfWriter.getInstance(document, out);
		document.open();

		Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
		Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
		Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

		document.add(new Paragraph("Batalha Naval - Historico das jogadas", titleFont));
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		document.add(new Paragraph("Gerado em " + date, normalFont));
		document.add(new Paragraph(" "));

		List<IMove> moves = game.getAlienMoves();
		if (moves.isEmpty()) {
			document.add(new Paragraph("Ainda nao foi disparada nenhuma rajada.", normalFont));
		} else {
			PdfPTable table = new PdfPTable(new float[]{1, 1, 1, 1, 4});
			table.setWidthPercentage(100);

			addHeader(table, "Jogada", headerFont);
			addHeader(table, "Tiro 1", headerFont);
			addHeader(table, "Tiro 2", headerFont);
			addHeader(table, "Tiro 3", headerFont);
			addHeader(table, "Resultado", headerFont);

			for (IMove move : moves) {
				table.addCell(new Phrase(String.valueOf(move.getNumber()), normalFont));
				for (IPosition shot : move.getShots())
					table.addCell(new Phrase(shot.toString(), normalFont));
				table.addCell(new Phrase(describeResults(move), normalFont));
			}
			document.add(table);
		}

		// estado final da frota
		IFleet fleet = game.getMyFleet();
		document.add(new Paragraph(" "));
		document.add(new Paragraph("Estado da frota: " + fleet.getFloatingShips().size() + " a flutuar, "
				+ fleet.getSunkShips().size() + " afundados", headerFont));
	}

	/**
	 * Descreve o resultado de cada tiro de uma jogada, separados por ";".
	 *
	 * @param move a jogada
	 * @return o texto com o resultado dos tiros
	 */
	static String describeResults(IMove move) {
		StringBuilder sb = new StringBuilder();
		for (IGame.ShotResult result : move.getShotResults()) {
			if (!sb.isEmpty())
				sb.append("; ");

			if (!result.valid())
				sb.append("fora do tabuleiro");
			else if (result.repeated())
				sb.append("repetido");
			else if (result.ship() == null)
				sb.append("agua");
			else if (result.sunk())
				sb.append(result.ship().getCategory()).append(" ao fundo");
			else
				sb.append("tiro num(a) ").append(result.ship().getCategory());
		}
		return sb.toString();
	}

	private static void addHeader(PdfPTable table, String text, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setGrayFill(0.85f);
		table.addCell(cell);
	}
}

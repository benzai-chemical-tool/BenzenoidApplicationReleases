package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import spectrums.ResultLogFile;
import spectrums.SpectrumsComputer;

public class Trash {

	public static void main(String[] args) throws IOException {

		File listFile = new File(
				"C:\\Users\\adrie\\Documents\\These\\molecules\\bdd_app\\log_files\\list_log_files.txt");

		BufferedReader r = new BufferedReader(new FileReader(listFile));
		String line;

		BufferedWriter w = new BufferedWriter(new FileWriter(
				new File("C:\\Users\\adrie\\Documents\\These\\molecules\\bdd_app\\negative_frequencies.txt")));

		while ((line = r.readLine()) != null) {

			ResultLogFile result = SpectrumsComputer.parseLogFile(line);

			for (Double d : result.getFrequencies()) {
				if (d < 0.0) {

					String[] split = line.split(Pattern.quote("\\"));
					w.write("/home/avaret/com_irreg_1_9_hexagons/done/" + split[split.length - 1] + "\n");
					break;
				}

			}
		}

		w.close();
		r.close();
	}
}

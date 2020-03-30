package src.utils;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CountWords {

	public static void main(String[] args) {
		try {
			String filePathTest = "data/dzimiks/A/input.txt";
			String filePath = "data/disk1/A/wiki-1.txt";
			List<String> result = Files.readLines(new File(filePath), Charsets.UTF_8);
			List<String> words = new ArrayList<>();
			result.forEach(part -> words.addAll(Arrays.asList(part.split("\\s"))));

			ConcurrentHashMultiset<Object> multiset = ConcurrentHashMultiset.create();
			int arity = 1;
			int wordsLength = words.size();
			List<String> bagOfWords;

			System.out.println("NUMBER OF WORDS: " + wordsLength);

			for (int i = 0; i < wordsLength; i += arity) {
				bagOfWords = getBagOfWords(words, wordsLength, i, i + arity);
				Collections.sort(bagOfWords);
				multiset.add(bagOfWords);

//				System.out.println("bagOfWords: " + bagOfWords);
//				System.out.println("multiset: " + multiset);
//				System.out.println();
			}

			ImmutableSet<Multiset.Entry<Object>> entriesSortedByCount = Multisets.copyHighestCountFirst(multiset).entrySet();
			System.out.println("entriesSortedByCount size: " + entriesSortedByCount.size());
			int i = 0;

			for (Multiset.Entry<Object> entry : entriesSortedByCount) {
				System.out.println(entry);

				if (++i == 10) {
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> getBagOfWords(List<String> words, int length, int startIndex, int endIndex) {
		if (words == null || startIndex > length || startIndex < 0 || startIndex > endIndex) {
			return null;
		}

		if (endIndex > length) {
			return words.subList(startIndex, length);
		}

		return words.subList(startIndex, endIndex);
	}
}

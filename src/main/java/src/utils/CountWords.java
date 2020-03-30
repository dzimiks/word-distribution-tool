package src.utils;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CountWords {

	public ImmutableList<Multiset.Entry<Object>> getMostOccurringBOW(String filePath, int arity) throws IOException {
		List<String> result = Files.readLines(new File(filePath), Charsets.UTF_8);
		List<String> words = new ArrayList<>();
		result.forEach(part -> words.addAll(Arrays.asList(part.split("\\s"))));

		ConcurrentHashMultiset<Object> multiset = ConcurrentHashMultiset.create();
		int wordsLength = words.size();
		List<String> bagOfWords;

		System.out.println("NUMBER OF WORDS: " + wordsLength);

		for (int i = 0; i < wordsLength; i += arity) {
			bagOfWords = getBagOfWords(words, wordsLength, i, i + arity);
			Collections.sort(bagOfWords);
			multiset.add(bagOfWords);
		}

		ImmutableSet<Multiset.Entry<Object>> entriesSortedByCount = Multisets.copyHighestCountFirst(multiset).entrySet();
		System.out.println("entriesSortedByCount size: " + entriesSortedByCount.size());
		return entriesSortedByCount.asList().subList(0, 100);
	}

	private List<String> getBagOfWords(List<String> words, int length, int startIndex, int endIndex) {
		if (words == null || startIndex > length || startIndex < 0 || startIndex > endIndex) {
			return null;
		}

		if (endIndex > length) {
			return words.subList(startIndex, length);
		}

		return words.subList(startIndex, endIndex);
	}
}

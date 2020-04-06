package src.components.cruncher;

import com.google.common.collect.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CruncherWorker implements Callable<Map<String, ImmutableList<Multiset.Entry<Object>>>> {

	private String fileName;
	private String input;
	private int arity;

	public CruncherWorker(String fileName, String input, int arity) {
		this.fileName = fileName;
		this.input = input;
		this.arity = arity;
		System.out.println("CruncherWorker init\n");
	}

	@Override
	public Map<String, ImmutableList<Multiset.Entry<Object>>> call() throws Exception {
		Map<String, ImmutableList<Multiset.Entry<Object>>> output = new ConcurrentHashMap<>();
		// TODO: How much to return?
		ImmutableList<Multiset.Entry<Object>> fullResult = getMostOccurringBOW(input, arity);
		ImmutableList<Multiset.Entry<Object>> result = fullResult.subList(0, Math.min(fullResult.size(), 100));
		output.put(fileName + "-arity" + arity, result);
		return output;
	}

	private ImmutableList<Multiset.Entry<Object>> getMostOccurringBOW(String input, int arity) throws IOException {
		List<String> words = new CopyOnWriteArrayList<>(Arrays.asList(input.split("\\s")));
		ConcurrentHashMultiset<Object> multiset = ConcurrentHashMultiset.create();
		int wordsLength = words.size();
		List<String> bagOfWords;

		System.out.println("NUMBER OF WORDS: " + wordsLength);

		for (int i = 0; i < wordsLength; i++) {
			bagOfWords = Lists.newCopyOnWriteArrayList(getBagOfWords(words, wordsLength, i, i + arity));
			Collections.sort(bagOfWords);
			multiset.add(bagOfWords);
		}

		ImmutableSet<Multiset.Entry<Object>> entriesSortedByCount = Multisets.copyHighestCountFirst(multiset).entrySet();
		System.out.println("entriesSortedByCount size: " + entriesSortedByCount.size());
		return entriesSortedByCount.asList();
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

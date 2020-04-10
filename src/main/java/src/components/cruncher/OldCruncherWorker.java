package src.components.cruncher;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class OldCruncherWorker implements Callable<Map<String, Multiset<Object>>> {

	private String fileName;
	private String input;
	private int arity;

	public OldCruncherWorker(String fileName, String input, int arity) {
		this.fileName = fileName;
		this.input = input;
		this.arity = arity;

//		System.out.println("OldCruncherWorker init\n");
	}

	@Override
	public Map<String, Multiset<Object>> call() {
		Map<String, Multiset<Object>> output = new ConcurrentHashMap<>();
		Multiset<Object> fullResult = getMostOccurringBOW(input, arity);
		output.put(fileName + "-arity" + arity, fullResult);
		return output;
	}

	private Multiset<Object> getMostOccurringBOW(String input, int arity) {
		List<String> words = new CopyOnWriteArrayList<>(Arrays.asList(input.split("\\s")));
		Multiset<Object> multiset = ConcurrentHashMultiset.create();
		int wordsLength = words.size();
		List<String> bagOfWords;

		System.out.println("NUMBER OF WORDS: " + wordsLength);

		for (int i = 0; i < wordsLength; i++) {
			bagOfWords = Lists.newCopyOnWriteArrayList(getBagOfWords(words, wordsLength, i, i + arity));
			Collections.sort(bagOfWords);
			multiset.add(bagOfWords);
		}

		return multiset;
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

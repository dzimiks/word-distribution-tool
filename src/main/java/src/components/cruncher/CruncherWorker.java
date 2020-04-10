package src.components.cruncher;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import src.main.Main;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveTask;

public class CruncherWorker extends RecursiveTask<Map<String, Multiset<Object>>> {

	private String fileName;
	private String input;
	private int arity;
	private int start;
	private int end;

	public CruncherWorker(String fileName, String input, int arity, int start, int end) {
		this.fileName = fileName;
		this.input = input;
		this.arity = arity;
		this.start = start;
		this.end = end;

//		System.out.println("CruncherWorker init\n");
	}

	@Override
	protected Map<String, Multiset<Object>> compute() throws OutOfMemoryError {
//		System.out.println("Start: " + start);
//		System.out.println("End: " + end);

		Map<String, Multiset<Object>> output = new ConcurrentHashMap<>();

		if (end - start <= Main.COUNTER_DATA_LIMIT) {
			Multiset<Object> fullResult = getMostOccurringBOW(input, start, end, arity);
			output.put(fileName + "-arity" + arity, fullResult);
			return output;
		}

		int leftEnd = start + Main.COUNTER_DATA_LIMIT;
		int rightStart;

		while (input.charAt(leftEnd) != ' ' && leftEnd > start) {
			leftEnd--;
		}

		leftEnd--;

		if (arity == 1) {
			rightStart = leftEnd + 1;
		} else {
			rightStart = leftEnd;
			int n = arity - 1;

			while (n > 0) {
				rightStart--;

				if (input.charAt(rightStart) == ' ' && input.charAt(rightStart + 1) != ' ') {
					n--;
				}
			}
		}

		while (input.charAt(rightStart) == ' ') {
			rightStart++;
		}

		CruncherWorker computeJob = new CruncherWorker(fileName, input, arity, start, leftEnd);
		CruncherWorker forkJob = new CruncherWorker(fileName, input, arity, rightStart, end);
		forkJob.fork();

		Map<String, Multiset<Object>> leftResult = computeJob.compute();
		Map<String, Multiset<Object>> rightResult = forkJob.join();

		rightResult.keySet().forEach(key -> {
			if (leftResult.containsKey(key)) {
				leftResult.put(key, Multisets.sum(leftResult.get(key), rightResult.get(key)));
			} else {
				leftResult.put(key, rightResult.get(key));
			}
		});

		return leftResult;
	}

	private Multiset<Object> getMostOccurringBOW(String input, int start, int end, int arity) {
		List<String> words = new CopyOnWriteArrayList<>(Arrays.asList(input.substring(start, end + 1).split("\\s")));
		Multiset<Object> multiset = ConcurrentHashMultiset.create();
		int wordsLength = words.size();
		List<String> bagOfWords;

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

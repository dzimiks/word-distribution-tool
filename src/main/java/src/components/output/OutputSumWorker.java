package src.components.output;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import java.util.Map;
import java.util.concurrent.Callable;

public class OutputSumWorker implements Callable<Multiset<Object>> {

	Map<String, Multiset<Object>> outputData;

	public OutputSumWorker(Map<String, Multiset<Object>> outputData) {
		this.outputData = outputData;
	}

	@Override
	public Multiset<Object> call() throws Exception {
		Multiset<Object> result = HashMultiset.create();

		for (Map.Entry<String, Multiset<Object>> entry : outputData.entrySet()) {
			Multiset<Object> curr = entry.getValue();
			result = Multisets.sum(result, curr);
		}

		return result;
	}
}

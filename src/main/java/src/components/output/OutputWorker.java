package src.components.output;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import java.util.concurrent.Callable;

public class OutputWorker implements Callable<ImmutableList<Multiset.Entry<Object>>> {

	private Multiset<Object> result;

	public OutputWorker(Multiset<Object> result) {
		this.result = result;
	}

	@Override
	public ImmutableList<Multiset.Entry<Object>> call() throws Exception {
		// First 100 results
		ImmutableList<Multiset.Entry<Object>> fullResult = Multisets.copyHighestCountFirst(result).entrySet().asList();
		return fullResult.subList(0, Math.min(fullResult.size(), 100));
	}
}

package src.components.output;

import com.google.common.math.BigIntegerMath;
import javafx.concurrent.Task;
import src.main.Main;

import java.math.BigInteger;
import java.math.RoundingMode;

public class SortProgressBarTask extends Task<BigInteger> {

	private int resultSize;
	private OutputView outputView;

	public SortProgressBarTask(OutputView outputView, int resultSize) {
		this.outputView = outputView;
		this.resultSize = resultSize;
	}

	@Override
	protected BigInteger call() throws Exception {
		BigInteger n = new BigInteger(String.valueOf(resultSize));
		BigInteger logn = new BigInteger(String.valueOf(BigIntegerMath.log10(n, RoundingMode.HALF_EVEN)));
		BigInteger nlogn = n.multiply(logn);
		BigInteger limit = new BigInteger(String.valueOf(Main.SORT_PROGRESS_LIMIT));
		BigInteger value = new BigInteger("0");

		System.out.println("n: " + n);
		System.out.println("logn: " + logn);
		System.out.println("nlogn: " + nlogn);

		long percentDone = 0;

		for (; value.compareTo(nlogn) < 0; value = value.add(limit)) {
			if (value.mod(limit).equals(BigInteger.ZERO)) {
				percentDone++;
				updateProgress(Math.min(percentDone, 100), 100L);
				updateMessage(Math.min(percentDone, 100) + "%");

				System.out.println("Percent done: " + percentDone);
				System.out.println("Value: " + value);
			}
		}

		return n;
	}
}

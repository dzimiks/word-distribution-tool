package src.components.output;

import javafx.concurrent.Task;

import java.math.BigInteger;

public class SortProgressBarTask extends Task<BigInteger> {

	private BigInteger sum;
	private int resultSize;
	private OutputView outputView;

	public SortProgressBarTask(OutputView outputView, int resultSize) {
		this.outputView = outputView;
		this.resultSize = resultSize;
		this.sum = new BigInteger("0");
	}

	@Override
	protected BigInteger call() throws Exception {
//		BigInteger n = new BigInteger(String.valueOf(resultSize));
//		BigInteger logn = new BigInteger(String.valueOf(Math.log(n.doubleValue())));
//		BigInteger nlogn = n.multiply(logn);
//		long val = 0;
//
//		System.out.println("n: " + n);
//		System.out.println("logn: " + logn);
//		System.out.println("nlogn: " + nlogn);
//
//		while (nlogn.compareTo(BigInteger.ZERO) < 0) {
//			nlogn = nlogn.divide(BigInteger.valueOf(Main.SORT_PROGRESS_LIMIT));
//			val = nlogn.longValue();
//			updateProgress(val, 100L);
//			updateMessage(val + "%");
//			System.out.println("nlogn size: " + nlogn);
//			System.out.println("Percent done: " + val);
//		}

		long percentDone = 0;
		int max = resultSize;
		System.out.println("START - Result size: " + resultSize);

		while (percentDone < 100) {
			percentDone++;
			updateProgress(percentDone, 100L);
			updateMessage("Sorting: " + percentDone + "%");
			Thread.sleep(200);
		}

//		while (resultSize > 0) {
//			float div = 1f * resultSize / Main.SORT_PROGRESS_LIMIT;
//			percentDone = 100 - ((div * 100) / max) * 100;
//			resultSize = (int) div;
//			updateProgress(percentDone, 100f);
//			updateMessage(percentDone + "%");
//			System.out.println("Result size: " + resultSize);
//			System.out.println("Percent done: " + percentDone);
//			Thread.sleep(1000);
//		}

//		resultSize : 100% = percentDone : x;
//		percentDone = (x * resultSize) / 100

//		11927986 : 100 = 119279 : x
//		11927986 * x = 119279 * 100
//		x = (119279 * 100) / 11927986
		return sum;
	}
}

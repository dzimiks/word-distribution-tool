package src.components.output;

import javafx.concurrent.Task;
import src.main.Main;

import java.math.BigInteger;

public class ProgressBarTask extends Task<BigInteger> {

	private BigInteger sum;
	private int resultSize;

	public ProgressBarTask(int resultSize) {
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

		float percentDone;
		int max = resultSize;
		System.out.println("START - Result size: " + resultSize);

		while (resultSize > 0) {
			float div = 1f * resultSize / Main.SORT_PROGRESS_LIMIT;
			percentDone = 100 - ((div * 100) / max) * 100;
			resultSize = (int) div;
			updateProgress(percentDone, 100f);
			updateMessage(percentDone + "%");
			System.out.println("Result size: " + resultSize);
			System.out.println("Percent done: " + percentDone);
			Thread.sleep(1000);
		}

//		resultSize : 100% = percentDone : x;
//		percentDone = (x * resultSize) / 100

//		11927986 : 100 = 119279 : x
//		11927986 * x = 119279 * 100
//		x = (119279 * 100) / 11927986
		return sum;
	}
}

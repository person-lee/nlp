package xiaoi;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

/**
 * 1：100元钱分为10份，每份最多不能超过90%，尽量保持正态分布。
 * 
 * 方案A：第一份拿出10%，剩下的随机分配，每次都计算平均值，然后随机。
 * 方案B：第一份拿出10%，随机生成N个1-10000之间的随机数，然后求和获取每个的比例，最后乘以钱的总数。
 * 方案C：首先拿出一定的钱，这部分钱平均分为10份，其中的九份加起来大于10%，然后将每一份加入到随机数中。
 * 		  即是每个人都至少获得10/9的钱。
 * @author cdlibaocang
 *
 */
public class AllotNumber {
	public static final int HIGHEST = 10000;
	
	public static List<Double> randomNumber(double total, int num) {
		if (total < 0.00001 && num < 1) {
			return Collections.EMPTY_LIST;
		} else {
			List<Double> randomList = Lists.newArrayList();
			double retain = total / (num - 1);
			randomList.addAll(allotNumber(total - retain, num, retain / num));
			return randomList;
		}
	}
	
	private static List<Double> allotNum(double total, int num, double retain) {
		if (total < 0.00001 && num < 1) {
			return Collections.EMPTY_LIST;
		} else {
			List<Integer> nums = Lists.newArrayList();
			int sum = 0;
			for (int index = 0; index < num; index++) {
				int randi = randomInt(HIGHEST);
				sum += randi;
				nums.add(randi);
			}
			
			List<Double> randomList = Lists.newArrayList();
			for (Integer eachNum : nums) {
				randomList.add(total * eachNum / sum + retain);
			}
			
			return randomList;
		}
	}
	
	/**
	 * 每次获取一个平均值，然后通过random函数得到一个0-1之间的浮点数，通过平均值乘以一个比例得到一个随机值。
	 * @param total
	 * @param num
	 * @return
	 */
	private static List<Double> allotNumber(double total, int num, double retain) {
		List<Double> randomList = Lists.newArrayList();
		while (num-- > 1) {
			double tokeNumber = allot(total, num);
			total = total - tokeNumber;
			randomList.add(tokeNumber + retain);
		}
		
		// 最后一份
		randomList.add(total + retain);
		return randomList;
	}
	
	private static double allot(double total, int num) {
		double aver = total / num;
		double averNum = aver * randomDouble();
		return averNum;
	}
	
	private static double randomDouble() {
		Random random = new Random();
		return random.nextDouble();
	}
	
	private static int randomInt(int num) {
		Random random = new Random();
		return random.nextInt(num) + 1;
	}
	
	private static long randomLong() {
		Random random = new Random();
		return random.nextLong();
	}

	public static void main(String[] args) {
//		for(int i = 0; i < 1000; i++) {
//			System.out.println(randomNumber(100, 10));
//		}
//		
//		Double sum = 0d;
//		for (Double num : randomNumber(100, 10)) {
//			sum += num;
//		}
		System.out.println("总共：" + randomInt(10) * 1000 * 60 / 10 * 2l);
		
	}
}

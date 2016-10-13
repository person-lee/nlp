package com.lbc.nlp_algorithm.word2vec;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.lbc.nlp_algorithm.word2vec.common.Haffman;
import com.lbc.nlp_algorithm.word2vec.common.MapCount;
import com.lbc.nlp_domain.word2vec.HiddenNeuron;
import com.lbc.nlp_domain.word2vec.Neuron;
import com.lbc.nlp_domain.word2vec.WordNeuron;
import com.lbc.nlp_modules.common.thread.ThreadPoolUtils;

public class MultiLearn {

	private Map<String, Neuron> wordMap = new HashMap<String, Neuron>();
	/**
	 * 训练多少个特征
	 */
	private int layerSize = 200;
	/**
	 * 上下文窗口大小
	 */
	private int window = 5;

	private double sample = 1e-3;
	private double alpha = 0.025;
	private double startingAlpha = alpha;

	public int EXP_TABLE_SIZE = 1000;

	private Boolean isCbow = false;

	private double[] expTable = new double[EXP_TABLE_SIZE];

	private int trainWordsCount = 0;

	private int MAX_EXP = 6;
	private int freqThresold = 5;
	private int capacity = 10;
	
	private int wordCountActual = 0;
	private final byte[] alphaLock = new byte[0];  // alpha同步锁
	private InputStream inputStream;
	private int length = 0x40000000; //1G
	private int sentenceLen = 50;

	public MultiLearn(Boolean isCbow, Integer layerSize, Integer window,
			Double alpha, Double sample) {
		createExpTable();
		if (isCbow != null) {
			this.isCbow = isCbow;
		}
		if (layerSize != null)
			this.layerSize = layerSize;
		if (window != null)
			this.window = window;
		if (alpha != null)
			this.alpha = alpha;
		if (sample != null)
			this.sample = sample;
	}

	public MultiLearn() {
		createExpTable();
	}
	
	public class TrainModel implements Runnable {
		private long nextRandom = 5;
		
		private BlockingQueue<LinkedList<String>> corpusQueue;
        private LinkedList<String> corpusToBeTrained;
        private int trainingWordCount;
        private double tempAlpha;
        
        public TrainModel(BlockingQueue<LinkedList<String>> corpusQueue) {
        	this.corpusQueue = corpusQueue;
		}
		
		@Override
		public void run() {
			try {
				boolean hasCorpus = true;
				while (hasCorpus) {
					corpusToBeTrained = corpusQueue.poll(2, TimeUnit.SECONDS);
					if(CollectionUtils.isNotEmpty(corpusToBeTrained)){
						tempAlpha = alpha;
						trainingWordCount = 0;
						train();
						calAlpha();
					}else{
						hasCorpus = false;
					}
				}
			} catch (IOException ioe) {
				System.err.println(ioe.getMessage());
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		
		private void train() throws IOException{
			List<WordNeuron> sentence = new ArrayList<WordNeuron>();
			trainingWordCount = corpusToBeTrained.size();
			for (String word : corpusToBeTrained) {
				Neuron entry = wordMap.get(word);
				if (entry == null) {
					continue;
				}
				// The subsampling randomly discards frequent words while keeping the ranking same
				if (sample > 0) {
					double ran = (Math.sqrt(entry.freq
							/ (sample * trainWordsCount)) + 1)
							* (sample * trainWordsCount) / entry.freq;
					nextRandom = nextRandom * 25214903917L + 11;
					if (ran < (nextRandom & 0xFFFF) / (double) 65536) {
						continue;
					}
				}
				sentence.add((WordNeuron) entry);
			}

			for (int index = 0; index < sentence.size(); index++) {
				nextRandom = nextRandom * 25214903917L + 11;
				if (isCbow) {
					cbowGram(index, sentence, (int) nextRandom % window, tempAlpha);
				} else {
					skipGram(index, sentence, (int) nextRandom % window, tempAlpha);
				}
			}
		}
		
		private void calAlpha(){
			synchronized (alphaLock){
				wordCountActual += trainingWordCount;
				if(wordCountActual > 10000){
					alpha = startingAlpha * (1 - wordCountActual / (double) (trainWordsCount + 1));
	                if (alpha < startingAlpha * 0.0001) {
	                    alpha = startingAlpha * 0.0001;
	                }
	                System.out.println("alpha:" + alpha + "\tProgress: "
	                        + (int) (wordCountActual / (double) (trainWordsCount + 1) * 100)
	                        + "%\t");
				}
            }
		}
	}

	//多任务训练
	private void multiTrain(){
		try {
			ExecutorService pool = ThreadPoolUtils.getExecutor();
			BlockingQueue<LinkedList<String>> corpusQueue = new ArrayBlockingQueue<LinkedList<String>>(capacity);
			LinkedList<Future> futures = Lists.newLinkedList();
			
			for(int i = 0; i < ThreadPoolUtils.getCorePoolSize(); i++){
				futures.add(pool.submit(new TrainModel(corpusQueue)));
			}
			
			loadCorpus(corpusQueue);
			
			for(Future future : futures){
				future.get();
			}
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		} catch (InterruptedException ie) {
			System.err.println(ie.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	private void loadCorpus(BlockingQueue<LinkedList<String>> corpusQueue) throws Exception{
		if(inputStream == null){
			return;
		}
		Scanner scanner = new Scanner(inputStream);
		LinkedList<String> sentenceList = Lists.newLinkedList();
		while (scanner.hasNext()) {
			sentenceList.add(scanner.next());
			if(sentenceList.size() == sentenceLen){
				corpusQueue.put(sentenceList);
				sentenceList = Lists.newLinkedList();
			}
		}
		corpusQueue.put(sentenceList);
		scanner.close();
		inputStream.reset();
	}
	
	/**
	 * skip gram 模型训练
	 *
	 * @param sentence
	 * @param neu1
	 */
	private void skipGram(int index, List<WordNeuron> sentence, int b, double tempAlpha) {
		// TODO Auto-generated method stub
		WordNeuron word = sentence.get(index);
		int a, c = 0;
		for (a = b; a < window * 2 + 1 - b; a++) {
			if (a == window) {
				continue;
			}
			c = index - window + a;
			if (c < 0 || c >= sentence.size()) {
				continue;
			}

			double[] neu1e = new double[layerSize];// 误差项
			// HIERARCHICAL SOFTMAX
			List<Neuron> neurons = word.neurons;
			WordNeuron we = sentence.get(c);
			for (int i = 0; i < neurons.size(); i++) {
				HiddenNeuron out = (HiddenNeuron) neurons.get(i);
				double f = 0;
				// Propagate hidden -> output
				for (int j = 0; j < layerSize; j++) {
					f += we.syn0[j] * out.syn1[j];
				}
				if (f <= -MAX_EXP || f >= MAX_EXP) {
					continue;
				} else {
					f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
					f = expTable[(int) f];
				}
				// 'g' is the gradient multiplied by the learning rate
				double g = (1 - word.codeArr[i] - f) * tempAlpha;
				// Propagate errors output -> hidden
				for (c = 0; c < layerSize; c++) {
					neu1e[c] += g * out.syn1[c];
				}
				// Learn weights hidden -> output
				for (c = 0; c < layerSize; c++) {
					out.syn1[c] += g * we.syn0[c];
				}
			}

			// Learn weights input -> hidden
			for (int j = 0; j < layerSize; j++) {
				we.syn0[j] += neu1e[j];
			}
		}

	}

	/**
	 * 词袋模型
	 *
	 * @param index
	 * @param sentence
	 * @param b
	 */
	private void cbowGram(int index, List<WordNeuron> sentence, int b, double tempAlpha) {
		WordNeuron word = sentence.get(index);
		int a, c = 0;

		List<Neuron> neurons = word.neurons;
		double[] neu1e = new double[layerSize];// 误差项
		double[] neu1 = new double[layerSize];// 误差项
		WordNeuron last_word;

		for (a = b; a < window * 2 + 1 - b; a++)
			if (a != window) {
				c = index - window + a;
				if (c < 0)
					continue;
				if (c >= sentence.size())
					continue;
				last_word = sentence.get(c);
				if (last_word == null)
					continue;
				for (c = 0; c < layerSize; c++)
					neu1[c] += last_word.syn0[c];
			}

		// HIERARCHICAL SOFTMAX
		for (int d = 0; d < neurons.size(); d++) {
			HiddenNeuron out = (HiddenNeuron) neurons.get(d);
			double f = 0;
			// Propagate hidden -> output
			for (c = 0; c < layerSize; c++)
				f += neu1[c] * out.syn1[c];
			if (f <= -MAX_EXP)
				continue;
			else if (f >= MAX_EXP)
				continue;
			else
				f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
			// 'g' is the gradient multiplied by the learning rate
			// double g = (1 - word.codeArr[d] - f) * alpha;
			// double g = f*(1-f)*( word.codeArr[i] - f) * alpha;
			double g = f * (1 - f) * (word.codeArr[d] - f) * tempAlpha;
			//
			for (c = 0; c < layerSize; c++) {
				neu1e[c] += g * out.syn1[c];
			}
			// Learn weights hidden -> output
			for (c = 0; c < layerSize; c++) {
				out.syn1[c] += g * neu1[c];
			}
		}
		for (a = b; a < window * 2 + 1 - b; a++) {
			if (a != window) {
				c = index - window + a;
				if (c < 0)
					continue;
				if (c >= sentence.size())
					continue;
				last_word = sentence.get(c);
				if (last_word == null)
					continue;
				for (c = 0; c < layerSize; c++)
					last_word.syn0[c] += neu1e[c];
			}

		}
	}
	
	@SuppressWarnings("resource")
	public void scanFile(String filename) throws IOException {
		try {
			FileChannel fc = new RandomAccessFile(filename, "r").getChannel();
			MappedByteBuffer out = fc.map(FileChannel.MapMode.READ_ONLY, 0, length); 
			byte[] dst = new byte[length];
			for (int i = 0; i < length; i++) {
				dst[i] = out.get(i);
			}
			inputStream = new ByteArrayInputStream(dst);		
			inputStream.mark(length);
			
			System.out.println("load file success");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void closeStream(){
		try {
			inputStream.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	private void readVocabByNIO() throws Exception {
		MapCount<String> mc = new MapCount<String>();
		
		if(inputStream == null){
			return;
		}
		Scanner scanner = new Scanner(inputStream);
		while (scanner.hasNext()) {  
			trainWordsCount += 1;
			mc.add(scanner.next());
        }  
		scanner.close();
		inputStream.reset();
		
		for (Entry<String, Integer> element : mc.get().entrySet()) {
			int freq = element.getValue();
            if (freq < freqThresold){
                continue;
            }
            
			wordMap.put(element.getKey(), new WordNeuron(element.getKey(),
					(double) element.getValue() / mc.size(), layerSize));
		}
		System.out.println("read file finished");
	}

	/**
	 * Precompute the exp() table f(x) = x / (x + 1)
	 */
	private void createExpTable() {
		for (int i = 0; i < EXP_TABLE_SIZE; i++) {
			expTable[i] = Math
					.exp(((i / (double) EXP_TABLE_SIZE * 2 - 1) * MAX_EXP));
			expTable[i] = expTable[i] / (expTable[i] + 1);
		}
	}
	
	public void learnFile() throws Exception {
		readVocabByNIO();
		new Haffman(layerSize).make(wordMap.values());

		// 查找每个神经元
		for (Neuron neuron : wordMap.values()) {
			((WordNeuron) neuron).makeNeurons();
		}

		multiTrain();
	}

	/**
	 * 保存模型
	 */
	public void saveModel(File file) {
		// TODO Auto-generated method stub

		try (DataOutputStream dataOutputStream = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(file)))) {
			dataOutputStream.writeInt(wordMap.size());
			dataOutputStream.writeInt(layerSize);
			double[] syn0 = null;
			for (Entry<String, Neuron> element : wordMap.entrySet()) {
				dataOutputStream.writeUTF(element.getKey());
				syn0 = ((WordNeuron) element.getValue()).syn0;
				for (double d : syn0) {
					dataOutputStream.writeFloat(((Double) d).floatValue());
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public int getLayerSize() {
		return layerSize;
	}

	public void setLayerSize(int layerSize) {
		this.layerSize = layerSize;
	}

	public int getWindow() {
		return window;
	}

	public void setWindow(int window) {
		this.window = window;
	}

	public double getSample() {
		return sample;
	}

	public void setSample(double sample) {
		this.sample = sample;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
		this.startingAlpha = alpha;
	}

	public Boolean getIsCbow() {
		return isCbow;
	}

	public void setIsCbow(Boolean isCbow) {
		this.isCbow = isCbow;
	}

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		String path = "F://word2vec/SogouCS/vocab.txt";
		
		try {
			MultiLearn learn = new MultiLearn();
			learn.scanFile(path);
			learn.learnFile();
			learn.closeStream();
			learn.saveModel(new File("F://word2vec/SogouCS/Multi/SogouCS"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
        
		System.out.println("use time " + (System.currentTimeMillis() - start));
	}
}

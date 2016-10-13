package UploadFile;

import java.io.File;

import com.lbc.nlp_modules.uploadFile.DownFileFetch;
import com.lbc.nlp_modules.uploadFile.DownFileInfoBean;

public class TestMethod {
	public TestMethod() {
		try {
			// DownFileInfoBean bean = new DownFileInfoBean(
			// "http://cdn.market.hiapk.com/data/upload//2012/09_27/17/car.wu.wei.kyo.shandian_174928.apk",
			// "D:\\temp",
			// "shandian_174928.apk", 5,true,null);
			File file = new File("C://Users/cdlibaocang/Downloads/answer.doc");
			DownFileInfoBean bean = new DownFileInfoBean(null, "F://",
					"answer.doc", 20, false, file);
			DownFileFetch fileFetch = new DownFileFetch(bean);
			fileFetch.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TestMethod();
	}
}
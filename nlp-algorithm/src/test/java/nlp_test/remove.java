package nlp_test;

import java.util.ArrayList;
import java.util.List;

public class remove {
	public static void main(String[] args){
		List<String> strList = new ArrayList<String>();
		strList.add("00");
		strList.add("1");
		strList.add("0");
		
		for(String str : strList){
			strList.remove(str);
		}
		System.out.println(strList);
	}

}

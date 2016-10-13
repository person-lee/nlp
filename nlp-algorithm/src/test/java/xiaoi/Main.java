package xiaoi;

import java.io.BufferedReader;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.util.ArrayList;  
import java.util.HashMap;  
import java.util.List;  
import java.util.Map;  
import java.util.Map.Entry;  
import java.util.Stack;  
      
public class Main {      
          
    /**   
     * 每次从终端读入一行   
     * @return   
     */      
    private static String readDataFromConsole() {        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));        
        String str = null;        
        try {        
            str = br.readLine();        
        
        } catch (IOException e) {        
            e.printStackTrace();        
        }        
        return str;        
    }      
          
    private static void parseLine(String line, Map<String, List<Integer>> countrys) {      
        if (line != null) {      
            String[] terms = line.split(" ");      
                  
            if (terms!=null) {      
                List<Integer> medals = new ArrayList<Integer>();      
                for (int i=1; i<terms.length; ++i) {      
                    medals.add(Integer.valueOf(terms[i]));      
                }      
                countrys.put(terms[0], medals);      
            }      
        }      
    }      
          
    private static Stack<String> sortMap(Map<String, List<Integer>> countrys) {      
        if (countrys!=null) {      
            Stack<String> sortCountry = new Stack<String>();      
            for (Entry<String, List<Integer>> country : countrys.entrySet()) {      
                if (sortCountry.isEmpty()) {      
                    sortCountry.push(country.getKey());      
                } else {      
                          
                    List<String> ret = null;      
                    String lastCountry = sortCountry.lastElement();      
                    int i=0;      
                    int capacity = sortCountry.indexOf(lastCountry);      
                    while (ret == null && capacity >= i) {      
                        int index = capacity - i;      
                        lastCountry = sortCountry.elementAt(index);      
                        ret = sort(lastCountry, countrys.get(lastCountry), country);      
                        i ++;      
                    }      
                    if (ret == null) {      
                        sortCountry.insertElementAt(country.getKey(), 0);      
                    } else if (sortCountry.contains(ret.get(0))) {      
                        int index = sortCountry.indexOf(ret.get(0));      
                        sortCountry.insertElementAt(ret.get(1), index + 1 );      
                    }else {      
                        sortCountry.insertElementAt(ret.get(0), 0);      
                    }      
                }      
            }      
            return sortCountry;      
        }else {      
            return null;      
        }      
    }      
          
    private static List<String> sort(String country, List<Integer> medals, Entry<String, List<Integer>> subject) {      
        int ret = sortCountry(medals, subject.getValue());      
        if (ret == 1) {      
            List<String> list = new ArrayList<String>();      
            list.add(country);      
            list.add(subject.getKey());             
            return list;      
        } else if (ret == 0) {      
            int sortStr = sortString(country, subject.getKey());      
            if (sortStr == 1) {      
                List<String> list = new ArrayList<String>();      
                list.add(country);      
                list.add(subject.getKey());             
                return list;      
            } else if (sortStr == 0) {      
                List<String> list = new ArrayList<String>();      
                list.add(country);      
                list.add(subject.getKey());             
                return list;      
            } else if (sortStr == -1) {// 继续比较上一个      
                return null;      
            }      
        } else if (ret == -1) {// 比较上一个      
            return null;      
        }      
        return null;      
    }      
          
    private static int sortCountry(List<Integer> object, List<Integer> subject) {      
        if (subject!=null && object!=null) {      
            for (int i = 0; i < object.size(); ++i) {      
                if (object.get(i) > subject.get(i)) {      
                    return 1;      
                } else if (object.get(i) < subject.get(i)) {      
                    return -1;      
                } else {      
                    continue;      
                }      
            }      
            return 0;      
        } else {      
            return -2;      
        }      
    }      
          
    private static int sortString(String object, String subject) {      
        if (subject!=null && object!=null) {      
            return object.compareTo(object);      
        } else {      
            return -2;      
        }      
    }      
          
    public static void main(String[] args) {      
        String reader = Main.readDataFromConsole();      
        int number = reader!=null?Integer.valueOf(reader):0;      
              
        if (number > 0) {      
            Map<String, List<Integer>> countrys = new HashMap<String, List<Integer>>();      
            while (number -- > 0) {      
                String line = Main.readDataFromConsole();      
                parseLine(line, countrys);      
            }      
                  
            if (countrys!=null) {      
                Stack<String> stacks = Main.sortMap(countrys);      
                for (String stack : stacks) {      
                    System.out.println(stack);      
                }      
            }      
        }      
    }      
      
}   
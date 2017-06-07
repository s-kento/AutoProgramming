package search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.search.spell.LevensteinDistance;

/*
 * メソッドのランク付けを行うクラス
 */
public class Ranker {
	public List<MethodInfo> sortByMethodNameSimilarity(String methodName, List<MethodInfo> methods){
		List<MethodInfo> sortedMethods = new ArrayList<MethodInfo>();
		Map<MethodInfo, Float> distance = new HashMap<MethodInfo, Float>();
		for(MethodInfo method:methods){
			distance.put(method, calcLeven(methodName, method.getMethodName()));
		}
		//ソート用のList生成
		List<Map.Entry<MethodInfo, Float>> entries = new ArrayList<Map.Entry<MethodInfo,Float>>(distance.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<MethodInfo,Float>>(){
			@Override
			public int compare(
					Entry<MethodInfo, Float> entry1, Entry<MethodInfo,Float> entry2){
				return ((Float)entry2.getValue()).compareTo((Float)entry1.getValue());
			}
		});
		for(Entry<MethodInfo, Float> entry:entries){
			sortedMethods.add(entry.getKey());
		}
		return sortedMethods;
	}

	public float calcLeven(String str1, String str2){
		LevensteinDistance ld = new LevensteinDistance();
		return ld.getDistance(str1, str2);
	}
}

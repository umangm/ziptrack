package ziptrack.util;

import java.util.HashSet;

public class Interval {
public int firstLeft, firstRight, secondLeft, secondRight;
	
	public Interval(){
		firstLeft = 1;
		firstRight = 1;
		secondLeft = 1;
		secondRight = 1;
	}
	
	public Interval(int x1, int x2, int y1, int y2){
		firstLeft = x1;
		firstRight = x2;
		secondLeft = y1;
		secondRight = y2;
	}
	
	public Interval(Interval i){
		firstLeft = i.firstLeft;
		firstRight = i.firstRight;
		secondLeft = i.secondLeft;
		secondRight = i.secondRight;
	}
	
	public Interval(Interval i, int x){
		firstLeft = i.firstLeft + x;
		firstRight = i.firstRight + x;
		secondLeft = i.secondLeft + x;
		secondRight = i.secondRight + x;
	}
	
	public void shift(int x){
		firstLeft = firstLeft + x;
		firstRight = firstRight + x;
		secondLeft = secondLeft + x;
		secondRight = secondRight + x;
	}
	
	public String toString(){
		return "(<" + firstLeft + ", " + firstRight + ">, <" + secondLeft + ", " + secondRight + ">)";
	}
	
	public static HashSet<Interval> shiftSet(HashSet<Interval> setRange, int x){
		HashSet<Interval> newSet = new HashSet<Interval> ();
		for(Interval i: setRange){
			newSet.add(new Interval(i, x));
		}
		return newSet;
	}
}

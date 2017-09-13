package com.xiaowei.phone;

import java.util.Comparator;

/**
 *
 *
 */
public class PinyinComparator implements Comparator<PhoneMemberBean> {

	public int compare(PhoneMemberBean o1, PhoneMemberBean o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}

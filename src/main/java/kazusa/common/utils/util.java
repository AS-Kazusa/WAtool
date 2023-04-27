package kazusa.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kazusa
 * @version 1.1.0
 */
public class util {

	private static List<Boolean> arrayList(boolean... is) {
		List<Boolean> bs = new ArrayList<>();
		for (boolean b: is) {
			bs.add(b);
		}
		return bs;
	}
}
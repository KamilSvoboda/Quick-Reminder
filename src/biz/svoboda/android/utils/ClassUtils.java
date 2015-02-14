package biz.svoboda.android.utils;

/**
 * Set of class utils
 * @author Kamil Svoboda
 * 
 */
public class ClassUtils {

	/**
	 * Returns current method name
	 * @return method name
	 */
	public static String getCurrentMethod() {
		String methodName = Thread.currentThread().getStackTrace()[1]
				.getMethodName();
		return methodName;
	}
}

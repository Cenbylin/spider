package spider.IDAL;

import spider.bean.SpiderResult;

public interface DoAfterSpider {
	
	/**
	 * 抓取到数据回调 返回是否继续进行抓取
	 * @param spiderResult
	 * @return
	 */
	boolean gotResult(SpiderResult spiderResult);

	/**
	 * 异常回调  返回是否继续进行抓取
	 * @param msg
	 * @return
	 */
	boolean spiderException(String msg);

	/**
	 * 结束回调
	 * @param msg
	 */
	void spiderEnd(String msg);
}

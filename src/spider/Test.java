package spider;

import spider.IDAL.DoAfterSpider;
import spider.bean.SpiderResult;
import spider.thread.Spider;

public class Test implements DoAfterSpider {
	/**
	 * 抓取到数据回调 返回是否继续进行抓取
	 */
	public boolean gotResult(SpiderResult spiderResult) {
		System.out.println("邮箱:" + spiderResult.getAim() +"\n上下文：" + spiderResult.getContext());
		return true;
	}
	
	/**
	 * 异常回调 返回是否继续进行抓取
	 */
	public boolean spiderException(String msg) {
		System.out.println(msg);
		return true;
	}
	
	/**
	 * 结束回调
	 */
	public void spiderEnd(String msg) {
		System.out.println(msg);
	}
	
	/**
	 * 使用说明
	 * 
	 * 1、产生爬虫线程，传入搜索引擎代号、关键字、最高结果获取数、抓取结果处理对象
	 * 2、抓取结果处理对象需要实现DoAfterSpider接口
	 * @param args
	 */
	public static void main(String[] args) {
		//此处Test为结果处理对象
		//引擎代号为1
		//关键字为led mail
		//最高结果获取数为20：即获取到20个就必须结束抓取进程，在此之前可以通过控制 抓取数据和异常的回调返回值 控制。
		Thread thread = new Thread(new Spider(1, "led mail", 20, new Test()));
		thread.start();
	}
}

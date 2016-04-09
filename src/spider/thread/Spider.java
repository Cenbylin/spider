package spider.thread;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import spider.IDAL.DoAfterSpider;
import spider.bean.SpiderConfig;
import spider.bean.State;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Spider implements Runnable {
	Vector<String> source = new Vector<String>();//二级源地址库
	List<String> list = new LinkedList<String>();//地址记录，用于排重
	SpiderConfig spiderConfig = new SpiderConfig();//配置
	DoAfterSpider doAfterSpider;//爬虫结束回调类
	
	int engine ;// 搜索引擎代码
	String  key = null;// 关键字
	String torrent = null;// 种子
	State state = new State();// 控制标识
	String contentString = null;// 待分析页面主体
	int pageNumber = 1;//页数
	
	/**
	 * 构造函数
	 * @param engine 引擎号
	 * @param key 关键字
	 * @param mount 采集结果总数
	 */
	public Spider(int engine, String key, int mount, DoAfterSpider doAfterSpider) {
		super();
		this.engine = engine;
		this.key = key;
		this.doAfterSpider = doAfterSpider;
		state.setMount(mount);
	}
	
	/**
	 * 爬虫主线程(集成一级分析器)
	 */
	public void run() {
		//加载配置
		spiderConfig.loadConfig(engine);
		
		//一级分析器开始
			//变更状态
		state.setAllState(state._START);
			//启动二级分析器线程
		Thread spiderThread2 = new Thread(new SpiderThread2(source, state, spiderConfig, doAfterSpider));
		spiderThread2.start();
			//生成种子
		buildTorrent();
		
		
		//逐页解析出二级源
			//初始化工作
		WebClient webClient;
		if(Boolean.valueOf(spiderConfig.getUseproxy())){
			webClient = new WebClient(BrowserVersion.CHROME, "127.0.0.1", 1080);
		}else{
			webClient = new WebClient(BrowserVersion.CHROME);
		}
			//关闭烦人的控制台输出
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		
		webClient.getOptions().setJavaScriptEnabled(Boolean.parseBoolean(spiderConfig.getUsejs()));//开启js异步加载
		webClient.getOptions().setActiveXNative(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());//支持Ajax
		webClient.setJavaScriptTimeout(10*000);//js执行超时
		webClient.waitForBackgroundJavaScript(10*1000);//js等待超时设置
		webClient.getOptions().setUseInsecureSSL(true);
		
		//逐页扫描
		while(state.getAllState()==state._START){
			//解析
			HtmlPage page = null;
			try {
				page = webClient.getPage(torrent);
			} catch (Exception e1) {
				if(!doAfterSpider.spiderException("Bad Get Torrent Page")){
					state.setAllState(state._STOP);
				}
			}
			for(HtmlAnchor anchor : page.getAnchors()){  
				String href = anchor.getAttribute("href");
				//判断是否为有效地址
				if(href.startsWith("http")){
					//入库
					if(list.indexOf(href) == -1){
						//源地址过滤
						selectHref(href);
						source.add(href);
						list.add(href);
					}
				}
	        }
			System.out.println("源地址库现有未分析链接数："+source.size());
			//生成下一种子
			buildTorrent();
			//适时休眠，等待二级线程处理
			while (state.getAllState()==state._START && source.size()>Integer.valueOf(spiderConfig.getCacheMount())) {
				System.out.println("主线程等待...");
				try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {
					if(!doAfterSpider.spiderException(e.toString())){
						state.setAllState(state._STOP);
					}
				}
			}
			
			//防抓取限制，切页延迟
			try {
				Thread.sleep(spiderConfig.getDelay()*1000);
			} catch (InterruptedException e) {
				if(!doAfterSpider.spiderException(e.toString())){
					state.setAllState(state._STOP);
				}
			}
		}
		//本次爬取结束
		doAfterSpider.spiderEnd("end");
		webClient.close();
	}
	
	
	/**
	 * 生成种子
	 * @return
	 */
	public void buildTorrent(){
		torrent = spiderConfig.getModel().replace("keywords", URLEncoder.encode(key)).replace("pagenumber", ""+pageNumber);
		//页数自增
		pageNumber++;
	}

	/**
	 * 地址过滤 去掉搜索引擎前缀
	 */
	public void selectHref(String href){
		if(spiderConfig.getSelectRule() != null){
			Pattern pattern = Pattern.compile(spiderConfig.getSelectRule());
			Matcher matcher = pattern.matcher(href);
			if(matcher.find()){
				href = matcher.group(spiderConfig.getPlace());
			}
		}
	}
	
}

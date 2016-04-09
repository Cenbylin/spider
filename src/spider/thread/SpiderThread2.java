package spider.thread;

import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import spider.IDAL.DoAfterSpider;
import spider.bean.SpiderConfig;
import spider.bean.SpiderResult;
import spider.bean.State;
import spider.utils.HtmlTool;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class SpiderThread2 implements Runnable {
	Vector<String> source = null;
	State state = null;//全局状态控制
	DoAfterSpider doAfterSpider;//结果处理
	SpiderConfig spiderConfig;//全局配置
	
	//网页获取器
	WebClient webClient;
	String str = null;
	HtmlPage page = null;
	
	/**
	 * 构造函数
	 * @param source
	 * @param state
	 * @param spierConfig
	 */
	public SpiderThread2(Vector<String> source, State state, SpiderConfig spiderConfig, DoAfterSpider doAfterSpider) {
		super();
		this.source = source;
		this.state = state;
		this.spiderConfig = spiderConfig;
		this.doAfterSpider = doAfterSpider;
	}

	/**
	 * 二级分析器
	 */
	public void run() {
		
		//引擎初始化工作
		if(Boolean.valueOf(spiderConfig.getUseproxy())){
			webClient = new WebClient(BrowserVersion.CHROME, "127.0.0.1", 1080);
		}else{
			webClient = new WebClient(BrowserVersion.CHROME);
		}
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.setJavaScriptTimeout(10*000);//js执行超时
		webClient.waitForBackgroundJavaScript(6*1000);//js等待超时设置
		webClient.getOptions().setTimeout(8*1000);//响应超时
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());//支持Ajax
		//关闭控制台输出
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		
		
		Random random = new Random();
		while(state.getAllState()==state._START){
			//判断库中是否有未处理源
			if(source.size() > 0){
				//从库中取出源
				String url = source.get(random.nextInt(source.size()));
				source.remove(url);
				System.out.println("在解析"+url);
				//分析源
				analyze(url);
			}else{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					if(!doAfterSpider.spiderException(e.toString())){
						state.setAllState(state._STOP);
					}
				}
			}
		}
		
	}
	
	/**
	 * 分析页面
	 * @param content
	 */
	public void analyze(String url) {
		String content = null;
		try {
			//获得网页内容
			content = getFromUrl(url);
			// 过滤空格回车
			Pattern p_space = Pattern.compile("\\s*|\t|\r|\n", Pattern.CASE_INSENSITIVE);
			Matcher m_space = p_space.matcher(content);
			content = m_space.replaceAll(""); 
			
			//目标匹配
			Pattern pattern = Pattern.compile(spiderConfig.getMailRegex());
			Matcher matcher = pattern.matcher(content);
			
			while(matcher.find()){
				String result = matcher.group(1);
				//封装结果类
				SpiderResult spiderResult = new SpiderResult();
				spiderResult.setAim(result);//设目标
				spiderResult.setUrl(url);//设地址
				
				Pattern pattern1 = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);
				Matcher matcher1 = pattern1.matcher(content);
				if(matcher1.find()){
					spiderResult.setHead(matcher1.group(1));
				}
				
					//上下文获取 暂定取前后100个索引
				int beginIndex = matcher.end(1)>100 ? matcher.end()-100:0;
				int endIndex = (matcher.end(1)+100) < content.length() ? matcher.end(1)+100 : content.length() ;
				spiderResult.setContext(HtmlTool.getTextFromHtml(content.substring(beginIndex, endIndex)));
				
				//交给处理器处理结果 并决定是否继续
				if(!doAfterSpider.gotResult(spiderResult)){
					state.setAllState(state._STOP);
				}
				
				//判断是否结束
				if(state.ifEnd()){
					break;
				}
			}
		} catch (Exception e) {
			if(!doAfterSpider.spiderException(e.toString())){
				state.setAllState(state._STOP);
			}
		}
		
	}
	
	/**
	 * 获得页面文本
	 */
	public String getFromUrl(String url){
		str = "";
		try {
			page = webClient.getPage(url);
			if(page!=null){
				str = page.asXml();
			}
		} catch (Exception e) {
			//重启浏览器
			if(Boolean.valueOf(spiderConfig.getUseproxy())){
				webClient = new WebClient(BrowserVersion.CHROME, "127.0.0.1", 1080);
			}else{
				webClient = new WebClient(BrowserVersion.CHROME);
			}
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.setJavaScriptTimeout(10*000);//js执行超时
			webClient.waitForBackgroundJavaScript(6*1000);//js等待超时设置
			webClient.getOptions().setTimeout(8*1000);//响应超时
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setUseInsecureSSL(true);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());//支持Ajax
			//关闭控制台输出
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setPrintContentOnFailingStatusCode(false);
			
			if(!doAfterSpider.spiderException("获取页面出错" + url)){
				state.setAllState(state._STOP);
			}
		}
		
		return str;
	}
	
}

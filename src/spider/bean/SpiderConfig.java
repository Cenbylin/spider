package spider.bean;

import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import spider.thread.Spider;

public class SpiderConfig {
	//配置项
	private String mailRegex = "(\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*)";//默认为邮箱匹配
	private String cacheMount = "50";//源地址库缓冲数
	private String model = null;//地址模板
	private String selectRule = null;//源匹配规则
	private int place = 1;// 源匹配，目标组
	private String usejs = "false";
	private String useproxy = "false";//使用代理
	
	
	/**
	 * 配置读取
	 */
	public void loadConfig(int engine){
		//读配置文件
		Element root = null;
		SAXReader reader = new SAXReader();
		try {
			InputStream xmlStream = Spider.class.getClassLoader().getResourceAsStream("torrent-config.xml");
			Document doc = reader.read(xmlStream);
			xmlStream.close();
			root = doc.getRootElement();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		//遍历 找到符合的引擎编号
		List<Element> list = root.elements();
		
		for (Element e : list) {
			
			if(e.getName().equals("search"+engine)){
				//抽取子节点属性
				List<Element> list1 = e.elements();
				for(Element e1 : list1){
					if(e1.getName().equals("model")){
						model = e1.getStringValue();
					}else if(e1.getName().equals("selectRule")){
						selectRule = e1.getStringValue();
					}else if(e1.getName().equals("place")){
						place = Integer.valueOf(e1.getStringValue());
					}else if(e1.getName().equals("usejs")){
						usejs = e1.getStringValue();
					}
				}
			}else if(e.getName().equals("config")){
				//抽取子节点属性
				List<Element> list1 = e.elements();
				for(Element e1 : list1){
					if(e1.getName().equals("mailRegex")){
						mailRegex = "(" + e1.getStringValue() + ")";
					}else if(e1.getName().equals("useproxy")){
						useproxy = e1.getStringValue();
					}else if(e1.getName().equals("cacheMount")){
						cacheMount = e1.getStringValue();
					}
				}
			}
		}
	}


	public String getModel() {
		return model;
	}


	public void setModel(String model) {
		this.model = model;
	}


	public String getSelectRule() {
		return selectRule;
	}


	public void setSelectRule(String selectRule) {
		this.selectRule = selectRule;
	}


	public int getPlace() {
		return place;
	}


	public void setPlace(int place) {
		this.place = place;
	}


	public String getMailRegex() {
		return mailRegex;
	}


	public void setMailRegex(String mailRegex) {
		this.mailRegex = mailRegex;
	}


	public String getUsejs() {
		return usejs;
	}


	public void setUsejs(String usejs) {
		this.usejs = usejs;
	}


	public String getUseproxy() {
		return useproxy;
	}


	public void setUseproxy(String useproxy) {
		this.useproxy = useproxy;
	}


	public String getCacheMount() {
		return cacheMount;
	}


	public void setCacheMount(String cacheMount) {
		this.cacheMount = cacheMount;
	}


	
	
}

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.regex.Pattern;

/**
 * Created by jcl on 2018/5/28
 */
public class MyCrawler extends WebCrawler {
    /**
     * 正则表达式匹配指定的后缀文件
     */
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp4|zip|gz))$");


    /**
     * 这个方法主要是决定哪些url我们需要抓取，返回true表示是我们需要的，返回false表示不是我们需要的Url
     * 第一个参数referringPage封装了当前爬取的页面信息 第二个参数url封装了当前爬取的页面url信息
     * 在这个例子中，我们指定爬虫忽略具有css，js，git，...扩展名的url，只接受以“http://www.ics.uci.edu/”开头的url。
     * 在这种情况下，我们不需要referringPage参数来做出决定。
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();// 得到小写的url
        return !FILTERS.matcher(href).matches() // 正则匹配，过滤掉我们不需要的后缀文件
                && href.startsWith("https://www.guahao.com/hospital");
    }

    /**
     * 当一个页面被提取并准备好被你的程序处理时，这个函数被调用。
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();// 获取url
        if (url.indexOf("https://www.guahao.com/hospital/introduction") == -1) {
            return;
        }
        System.out.println("URL--------------------> " + url);
        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();//// 强制类型转换，获取html数据对象
        String html = htmlParseData.getHtml();//获取页面Html
        try {
            Document doc = Jsoup.parse(html);
            String hosName = doc.getElementById("hospital-card-inner").getElementsByTag("strong").get(0).child(0).text();
            String address = doc.getElementsByClass("address").get(0).child(1).text();
            String tel = doc.getElementsByClass("tel").get(0).child(1).text();
            String website = doc.getElementsByClass("website").get(0).child(1).text();
            System.out.println("医院：" + hosName);
            System.out.println("地址：" + address);
            System.out.println("电话：" + tel);
            System.out.println("官网：" + website);
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public static void main(String[] args) throws Exception {
        String crawlStorageFolder = "d:/crawler";// 定义爬虫数据存储位置
        int numberOfCrawlers = 7;// 定义了7个爬虫，也就是7个线程

        CrawlConfig config = new CrawlConfig();// 定义爬虫配置
//        config.setMaxDepthOfCrawling(2);//爬取深度
        config.setCrawlStorageFolder(crawlStorageFolder);// 设置爬虫文件存储位置

        /*
         * 实例化爬虫控制器。
         */
        PageFetcher pageFetcher = new PageFetcher(config);// 实例化页面获取器
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();// 实例化爬虫机器人配置
        // 实例化爬虫机器人对目标服务器的配置，每个网站都有一个robots.txt文件
        // 规定了该网站哪些页面可以爬，哪些页面禁止爬，该类是对robots.txt规范的实现
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        // 实例化爬虫控制器
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * 对于每次抓取，您需要添加一些种子网址。 这些是抓取的第一个URL，然后抓取工具开始跟随这些页面中的链接
         */
        controller.addSeed("https://www.guahao.com/hospital/all/%E5%85%A8%E5%9B%BD/all/%E4%B8%8D%E9%99%90/p1");
//        controller.addSeed("https://www.guahao.com/hospital/introduction");
        /**
         * 启动爬虫，爬虫从此刻开始执行爬虫任务，根据以上配置
         */
        controller.start(MyCrawler.class, numberOfCrawlers);
    }
}


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class DownloadLinkExtractor
{
	public static String userAgentText = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36";
	
	public static String getMp4uploadDownloadLink(String embedUrl) throws IOException
	{
		Document soup = Jsoup.connect(embedUrl).userAgent(userAgentText).get();
		Elements scripts = soup.getElementsByAttributeValue("type", "text/javascript");
		ArrayList<String> evalTextList = new ArrayList<String>();
		for(Element script : scripts) { if(script.outerHtml().contains("|embed|")) { evalTextList.add(script.outerHtml()); } }
		List<String> evalItems = Arrays.asList(evalTextList.get(0).split("\\|"));
		evalItems = evalItems.subList(evalItems.indexOf("mp4upload") + 1, evalItems.size() - 1);
		String videoId = "";
		for(String a : evalItems)
		{
			if(a.length() > 30 && a.length() < 100)
			{
				videoId = a;
				break;
			}
		}
		//
		evalItems = Arrays.asList(evalTextList.get(0).split("\\|"));
		String w3str = "www";
		ArrayList<String> w3strPossiblesList = new ArrayList<String>();
		for(String s : evalItems)
		{
			//Pattern p1 = Pattern.compile("s\\d+$");
			//Pattern p2 = Pattern.compile("www\\d+$");
			//if(p1.matcher(s).find() || p2.matcher(s).find()) { w3strPossiblesList.add(s); }
			if(s.matches("s\\d+$") || s.matches("www\\d+$")) { w3strPossiblesList.add(s); }
		}
		if(w3strPossiblesList.size() != 0)
		{
			String max = w3strPossiblesList.get(0);
			for(String s : w3strPossiblesList) { if(s.length() > max.length()) { max = s; } }
			w3str = max;
		}
		String downloadLink = String.format("https://%s.mp4upload.com:%s/d/%s/video.mp4",
				w3str, evalItems.get(evalItems.indexOf(videoId)+1), videoId);
		return downloadLink;
	}
	
	public static String getVidcdnDownloadLink(String embedUrl) throws IOException
	{
		Document soup = Jsoup.connect(embedUrl).userAgent(userAgentText).get();
		String jsText = soup.getElementsByAttributeValue("class", "videocontent").get(0).outerHtml();
		Matcher m = Pattern.compile("file: \'(.+?)\'").matcher(jsText); m.find();
		String downloadLink = m.group();
		downloadLink = downloadLink.substring(7, downloadLink.length() - 1);
		return downloadLink;
	}
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("RESULT: " + DownloadLinkExtractor.getMp4uploadDownloadLink("https://www.mp4upload.com/embed-99r0hr81zk9k.html"));
		System.out.println("RESULT: " + DownloadLinkExtractor.getVidcdnDownloadLink("https://vidstreaming.io/load.php?id=OTc2MzI=&title=Boruto%3A+Naruto+Next+Generations+Episode+1"));
	}
}

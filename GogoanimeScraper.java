import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class GogoanimeScraper
{
	public static String userAgentText = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36";
	
	public int EpisodeCount;
	public String AnimeTitle;
	public String AnimeUrl;
	public ArrayList<Episode> EpisodeList;
	public ArrayList<Episode> ScrapedEpisodeList;
	
    public GogoanimeScraper(String url) throws IOException
    {
        Document document = Jsoup.connect(url).userAgent(userAgentText).get();
        String animeId = document.selectFirst("#movie_id").val();
        String animeAlias = document.selectFirst("#alias_anime").val();
        String animeLastEp = document.selectFirst("#episode_page").select("a").last().attr("ep_end");
        String ajaxUrl = String.format(
        		"https://ajax.gogocdn.net/ajax/load-list-episode?ep_start=0&ep_end=%s&id=%s&default_ep=0&alias=%s",
        		animeLastEp, animeId, animeAlias);
        this.AnimeTitle = document.title().replace("at Gogoanime", "").replace("Watch ", "").strip().replaceAll("[<>?\":/|]", "");
        this.AnimeUrl = url;
        Document ajaxSoup = Jsoup.connect(ajaxUrl).userAgent(userAgentText).get();
        this.EpisodeCount = ajaxSoup.select("a").size();
        //--------
        Elements liElements = ajaxSoup.select("li");
        this.EpisodeList = new ArrayList<Episode>();
        for(Element li : liElements)
        {
        	Episode ep = new Episode();
        	ep.Title = this.AnimeTitle + " - " + String.join(" ", li.text().split("\\s+"));
        	ep.Url = String.format("https://www.gogoanime.so%s", li.select("a").attr("href").strip());
        	this.EpisodeList.add(ep);
        }
    }
    
    public void scrapeEpisodes(int start, int end) throws IOException
    {
    	this.ScrapedEpisodeList = new ArrayList<Episode>();
    	for(int i = start - 1; i <= end - 1; i++)
    	{
    		Episode ep = this.EpisodeList.get(i);
    		System.out.println("GET: " + ep.Url);
    		Document soup = Jsoup.connect(ep.Url).userAgent(userAgentText).get();
    		Elements serverList = soup.getElementsByAttributeValue("class", "anime_muti_link").select("li");
    		ep.EmbedServersDict = new Hashtable<String, String>();
    		for(Element li : serverList.subList(1, serverList.size() - 1))
    		{
    			String embedUrl = li.select("a").attr("data-video");
    			if(! embedUrl.contains("https:")) { embedUrl = "https:" + embedUrl; }
    			ep.EmbedServersDict.put(li.className(), embedUrl);
    		}
    		this.ScrapedEpisodeList.add(ep);
    		System.out.println("- Collected: " + ep.Title);
    	}
    }
    
    public static ArrayList<String> searchAnime(String query) throws IOException
    {
    	String queryUrl = String.format("https://www.gogoanime.so/search.html?keyword=%s",
    			query.replace(" ", "%20"));
    	Document soup = Jsoup.connect(queryUrl).userAgent(userAgentText).get();
    	Elements pResults = soup.getElementsByAttributeValue("class", "items").get(0)
    			.getElementsByAttributeValue("class", "name");
    	ArrayList<String> pairedResults = new ArrayList<String>();
    	int c = 0;
    	for(Element p : pResults)
    	{
    		pairedResults.add(p.select("a").attr("title") +
    				"[SEPERATOR]" + "https://www.gogoanime.so" + p.select("a").attr("href"));
    		c++;
    		if(c == 4) { break; }
    	}
    	return pairedResults;
    }
    
    public static void main(String args[]) throws IOException
    {
    	System.out.println("abc|def|ghi".split("\\|")[1]);
    }
}

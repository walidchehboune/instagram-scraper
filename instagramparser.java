/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.application;



import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 *
 * @author walid
 */
public class JavaApplication {

    
    //***************************************************************************
    
    // instagram add some restraction in code 
    
    // in field hashtag put the word hashtag without character '#' is added automaticaly in url 
    
    
    
    
    final static String username = "**********";
    final static String password = "********";
    final static String hashtag="*******"; // dont use # just put the name of hashtag automaticaly will be added in next statement 
    final static String queryHash = "********************";
    final static int timeToSleep = 120000; // the time  to wait before continue in milisecond 1 second = 1000 milisecond  / 60000 milisecond =  1 min
    
     static  int number_of_post_parse = 0; //counter for number of posts 
     
     final static int max_post_to_collect = 1000;
    
    
    
    //*************************************************************************
    
    //*** First arrayList collect all HashTag with info name/id/number_of_posts
    
    //**** Second Arraylist contain info about specific posts this posts has relation with hashtag [many-----many] so each post can bellow to many hashtag and same thing for hashtag each hashtag can bellow to many post
    
     
    
    
    
    static ArrayList<Hashtag> All_Hash = new ArrayList<Hashtag>();
    static ArrayList<Post> All_Post = new ArrayList<Post>();
    static ArrayList<Owner> All_Owner = new ArrayList<Owner>();
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        // TODO code application logic here
        
        
        
        
       BufferedWriter writer = new BufferedWriter(new FileWriter("users.json"));
       
       
       String data ="username="+username+"&password="+password+"&queryParams={\"source\":\"auth_switcher\"}&optIntoOneTap=false";
      
        try{
        
           Request request = new Request("https://www.instagram.com/", "GET");
                   
                String Response =  request.Send(null,null);
                
                
               
              
                
                Build build =new Build(Response);
                
              build.setCookies(request.BuildCookies());
            
              
              Request request_login =  new Request("https://www.instagram.com/accounts/login/?source=auth_switcher", "POST");
              
              request_login.SetBuild(build);
              System.out.println(build.getX_instagram_ajax_X());
              Hashtable<String,String> params = build.BuildParams();
              
              String content = request_login.Send(params, data);
              
              
              build.setCookies(request_login.BuildCookies());
              
                
            
                 Request request_get_Hashtags =  new Request("https://www.instagram.com/web/search/topsearch/?context=blended&query=%23"+hashtag, "GET");
                 
                 request_get_Hashtags.SetBuild(build);
                 
                 content = request_get_Hashtags.Send(build.BuildParams(), null);
                 
                 
                 System.out.println("**************************************************************");
                 System.out.println("*              [HASHTAG]   [NUMBER OF POSTS]                 *");
                 System.out.println("*                                                            *");
                 System.out.println("**************************************************************");
               
              
                
                    JsonObject o =  new Gson().fromJson(content.replace(" ",""),JsonObject.class);
                    
                   
                JsonArray arrayHashtags = o.get("hashtags").getAsJsonArray();
                
              for(int i  =0; i<arrayHashtags.size();i++)
              {
                  
                  JsonObject element = arrayHashtags.get(i).getAsJsonObject();
                  
                  long media_count =  element.getAsJsonObject("hashtag").get("media_count").getAsLong();
                  All_Hash.add(new Hashtag(element.getAsJsonObject("hashtag").get("id").getAsString(),element.getAsJsonObject("hashtag").get("name").getAsString(),media_count));
                  
                  
                  System.out.println("["+i+"]["+element.getAsJsonObject("hashtag").get("name")+"]*********************["+element.getAsJsonObject("hashtag").get("media_count")+"]");
                  System.out.println("***************************************************************");
                  System.out.println("*              Collecting posts info........                  *");
                  System.out.println("*                                                             *");
                  System.out.println("***************************************************************");
               
                  
                  
              }
              /*
               Request QueryHash = new Request("https://www.instagram.com/", "GET");
               
               QueryHash.SetBuild(build);
               Response = QueryHash.Send(build.BuildParams(), null);
               
                Build QueryBuild = new Build(Response);
                  System.out.println("**************************************************************");
                 System.out.println("*              [Query HASH]                                      *");
                 System.out.println("*                                                            *");
                 System.out.println("**************************************************************");
                System.out.println(QueryBuild.getQueryHash());
                */
                Request Accueil = new Request("https://www.instagram.com/explore/tags/"+hashtag+"/?__a=1", "GET");
                
                Accueil.SetBuild(build);
                
                Response =Accueil.Send(null, null);
                
                
                Build AccueilBuild = new Build(Response);
                
                    Accueil.SetBuild(AccueilBuild);
                  
                     o =  new Gson().fromJson(Response.replace(" ",""),JsonObject.class);
                     
                  boolean has_next   = o.getAsJsonObject("graphql").getAsJsonObject("hashtag").getAsJsonObject("edge_hashtag_to_media").getAsJsonObject("page_info").get("has_next_page").getAsBoolean();
                  String end_cursor =  o.getAsJsonObject("graphql").getAsJsonObject("hashtag").getAsJsonObject("edge_hashtag_to_media").getAsJsonObject("page_info").get("end_cursor").getAsString();
                  Filter<Owner> filter_owner = new Filter<Owner>();
                  
                  Owner owner;
                  Post post;
               // System.out.println(AccueilBuild.getAfter());
                 System.out.println("**************************************************************");
                 System.out.println("*              ["+end_cursor+"]                              *");
                 System.out.println("*                                                            *");
                 System.out.println("**************************************************************");
                 
                 JsonArray nodes = o.getAsJsonObject("graphql").getAsJsonObject("hashtag").getAsJsonObject("edge_hashtag_to_media").getAsJsonArray("edges");
                
                 
                 filter_owner.SetCollection(All_Owner);
                 for(int x = 0; x < nodes.size(); x++)
                 {
                     
                    number_of_post_parse = number_of_post_parse + 1;
                    
                   
                  // All_Post.add(new Post(nodes.get(x).getAsJsonObject().getAsJsonObject("node").get("id").getAsString(),nodes.get(x).getAsJsonObject().getAsJsonObject("node").get("shortcode").getAsString(),nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("edge_liked_by").get("count").getAsInt(),nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("edge_media_to_comment").get("count").getAsInt(),nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("edge_media_to_caption").getAsJsonArray("edges").get(0).getAsJsonObject().getAsJsonObject("node").get("text").getAsString()));
                   
                     System.out.println("["+nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("owner").get("id").getAsString()+"]<============>["+nodes.get(x).getAsJsonObject().getAsJsonObject("node").get("id").getAsString()+"]");
                   
                   owner = new Owner(nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("owner").get("id").getAsString());
                     
                      post  =new Post(nodes.get(x).getAsJsonObject().getAsJsonObject("node").get("shortcode").getAsString(), nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("edge_liked_by").get("count").getAsInt(),nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("edge_media_to_caption").getAsJsonArray("edges").get(0).getAsJsonObject().getAsJsonObject("node").get("text").getAsString());
                      //public Post(String shortCode,int liked,int CommentBy,String Caption)
                      
                       owner.Add(post);
                      if(filter_owner.ContainsElement(owner)==-1){
                  
                      All_Owner.add(owner);
                      }else
                      {
                          filter_owner.GetCollection().get(filter_owner.ContainsElement(owner)).Add(post);
                      }
                      
                      All_Post.add(post);
                      
                      filter_owner.SetCollection(All_Owner);
                     
                 }
                 
                 //******know we start  parsing the whole  post 
                 
                 while(has_next && number_of_post_parse<=max_post_to_collect){
                 String url_next ="https://www.instagram.com/graphql/query/?query_hash=174a5243287c5f3a7de741089750ab3b&variables=%7B%22tag_name%22%3A%22sonic%22%2C%22first%22%3A10%2C%22after%22%3A%22"+end_cursor+"%22%7D";
                 Request next10Post = new Request(url_next, "GET");
                 next10Post.SetBuild(build);
                 Response =next10Post.Send(build.BuildParams(), null);
                 
                 
                        o =  new Gson().fromJson(Response.replace(" ",""),JsonObject.class);
                     
                  has_next   = o.getAsJsonObject("data").getAsJsonObject("hashtag").getAsJsonObject("edge_hashtag_to_media").getAsJsonObject("page_info").get("has_next_page").getAsBoolean();
                  end_cursor =  o.getAsJsonObject("data").getAsJsonObject("hashtag").getAsJsonObject("edge_hashtag_to_media").getAsJsonObject("page_info").get("end_cursor").getAsString();
                  
               // System.out.println(AccueilBuild.getAfter());
                System.out.println("**************************************************************");
                 System.out.println("*              ["+end_cursor+"]                              *");
                 System.out.println("*                                                            *");
                 System.out.println("**************************************************************");
                 
                 
                  nodes = o.getAsJsonObject("data").getAsJsonObject("hashtag").getAsJsonObject("edge_hashtag_to_media").getAsJsonArray("edges");
                
                  
                 for(int x = 0; x < nodes.size(); x++)
                 {
                     
                    try{
                   
                  // All_Post.add(new Post(nodes.get(x).getAsJsonObject().getAsJsonObject("node").get("id").getAsString(),nodes.get(x).getAsJsonObject().getAsJsonObject("node").get("shortcode").getAsString(),nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("edge_liked_by").get("count").getAsInt(),nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("edge_media_to_comment").get("count").getAsInt(),nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("edge_media_to_caption").getAsJsonArray("edges").get(0).getAsJsonObject().getAsJsonObject("node").get("text").getAsString()));
                   
                      System.out.println("["+nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("owner").get("id").getAsString()+"]<============>["+nodes.get(x).getAsJsonObject().getAsJsonObject("node").get("id").getAsString()+"]");
                   
                      owner = new Owner(nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("owner").get("id").getAsString());
                     
                      post  =new Post(nodes.get(x).getAsJsonObject().getAsJsonObject("node").get("shortcode").getAsString(), nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("edge_liked_by").get("count").getAsInt(),nodes.get(x).getAsJsonObject().getAsJsonObject("node").getAsJsonObject("edge_media_to_caption").getAsJsonArray("edges").get(0).getAsJsonObject().getAsJsonObject("node").get("text").getAsString());
                      //public Post(String shortCode,int liked,int CommentBy,String Caption)
                       owner.Add(post);
                        if(filter_owner.ContainsElement(owner)==-1){
                     
                      All_Owner.add(owner);
                      }else
                        {
                            
                            filter_owner.GetCollection().get(filter_owner.ContainsElement(owner)).Add(post);
                        }
                     
                      All_Post.add(post);
                      
                      filter_owner.SetCollection(All_Owner);
                    }catch(IndexOutOfBoundsException e)
                    {
                        continue;
                    }
                   
                     number_of_post_parse+=1;
                 }
                 
                 
                System.out.println("********************************************************************************************");
                System.out.println("*                                                                                          *");
                System.out.println("*                           [wait for 2min]                                                *");
                System.out.println("*                                                                                          *");
                System.out.println("********************************************************************************************");
                 
                 
                  Thread.sleep(60000);
                 }
              
                 writer.write("{\n");
                
                 
                 for(Owner i : filter_owner.GetCollection())
                 {
                     writer.write(i.getId()+":[\n");
                     for(Post p : i.getPosts())
                     {
                         writer.write(p.getCode()+",");
                     }
                     writer.write("],\n");
                 }
                 writer.write("}");
                 writer.close();
                 
                 
                }catch(IOException e)
        {
            e.printStackTrace(); 
        }
       
     
      
       }
    
    
    

}



class Filter<T>
{
    
  
    ArrayList<T> collection;
    
    
    public Filter()
    {
        
    }
    
    public Filter(ArrayList<T> collection)
    {
        
      this.collection = new ArrayList<T>();
    }
    public ArrayList<T> GetCollection()
    {
        return this.collection;
    }
    
    public ArrayList<T> ByNumber(int n)
    {
        ArrayList<T> temp = new ArrayList<T>();
       
        for(T o  : collection)
        {
             
            if(o.getClass() == Owner.class)
              if(((Owner)o).getPosts().size() >= n )
                temp.add(o);
            if(o.getClass() == Post.class)
             if(((Post)o).getLikedBy() >= n )
                temp.add(o);
            
            
        }
        
        
                 return temp;
        }
      
    
    public void SetCollection(ArrayList<T> list)
    {
        this.collection =list;
    }
    public int ContainsElement(T element)
    {
        
       
           for(int i = 0;i<this.collection.size();i++)
            {
                
                if(collection.get(i).getClass() == Owner.class)
                {
                   if(((Owner)element).getId() == ((Owner)collection.get(i)).getId())
                       return i;
                }
                if(collection.get(i).getClass() == Post.class)
                {
                       if(((Post)element).getCode()== ((Post)collection.get(i)).getCode())
                       return i;
                }
      
                
            }
            
            return -1;
        
    }
        
       
    
}

class Build
{
    private String response;
    private String csrf_token;
    private String x_instagram_ajax;
    InspectSource source; 
    private String cookies;
    
    private final String user_agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36";
    
    public Build(String response) {
        
            this.response = response;
            
            this.source = new InspectSource(this.response);
            
            csrf_token = source.One("\"csrf_token\":\"([a-zA-Z0-9]+)\"");
             x_instagram_ajax = source.One("\"rollout_hash\":\"([a-zA-Z0-9\\-_\\.:]+)\"");
             
        
    }
    
    public String getAfter()
    {
       return  source.One("\"end_cursor\":\"([a-zA-Z0-9=\\.\\-_]+)\"");
        
    }
    
    public String getQueryHash()
    {
        
        return source.One("(query_hash)");
    }
    
    public void setCookies(String cookies)
    {
        this.cookies = cookies;
    }
    
    public String getCookies(){return this.cookies;}
    
    
    public String getCsrf_token(){return this.csrf_token;}
    
    public String getX_instagram_ajax_X(){return this.x_instagram_ajax;}
    
    public String getUser_agent(){return this.user_agent;}
    
    
    public Hashtable<String,String> BuildParams()
    {
        Hashtable<String,String> params = new Hashtable<String,String>();
        params.put("User-Agent", this.user_agent);
        params.put("X-CSRFToken", this.csrf_token);
        params.put("X-Instagram-AJAX", this.x_instagram_ajax);
        params.put("X-Requested-With","XMLHttpRequest");
        params.put("Set-Cookie",this.cookies);
        
        
        
        
        
        return params;
        
        
    }
    
    
    
    
}



class InspectSource
{
    String source;
    
    InspectSource(String source)
    {
        this.source = source;
        
    }
    
    
    String One(String reference)
    {
       Pattern p =  Pattern.compile(reference);
       Matcher m  = p.matcher(this.source);
       
       while(m.find())
       {
           return m.group(1);
       }
        
       return null;
    }
    
    
    String findHashtag(String reference)
    {
         Pattern p =  Pattern.compile(reference);
       Matcher m  = p.matcher(this.source);
       
       while(m.find())
       {
           return m.group(1);
       }
        
       return null;
        
    }
}

class Request
{
    
    private URL Address;
    
    private String Method;
    
    private InputStream data;
    
    private Build build = null;
    
    private CookieManager cookieManager;
    
    final String User_Agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:70.0) Gecko/20100101 Firefox/70.0";
    
    public Request(String url,String Method) throws MalformedURLException {
        
        cookieManager = new CookieManager();
        
        
        
        CookieHandler.setDefault(cookieManager);
        
        this.Method = Method;
        
       
        Address = new URL(url);
           
    }
    
    public void SetBuild(Build o)
    {
        
        this.build = o;
        
    }
    
    public HttpURLConnection  Prepare(Hashtable<String,String> params,String data) throws IOException
    {
        
        HttpURLConnection con = (HttpURLConnection) Address.openConnection();
        
        con.setRequestMethod(Method);
        
       
            
         if(this.Method == "POST")
         {
             
               
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Set-Cookie", this.build.getCookies());
                con.setDoOutput(true);
                
                
                
                
             
         }if(this.Method == "GET")
         {
             if(params == null && build == null)
                con.setRequestProperty("User-Agent", User_Agent);
             
                   
                 
             if(build != null)
             params = build.BuildParams();
             
        
              con.setRequestProperty("Accept-Encoding","fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3");
              con.setRequestProperty("Accept", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3");
              con.setRequestProperty("Accept-Language","fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3");
              con.setRequestProperty("X-Requested-With","XMLHttpRequest");
            

             
         }
            
            if(params!=null){
                
            for(String key : params.keySet())
            {
                
                System.out.println(key+" "+params.get(key));
                con.setRequestProperty(key, params.get(key));
                
            }
            
            
            }
            if(data != null){
            try(OutputStream os = con.getOutputStream())
            {
                byte[] dat = data.getBytes();
                os.write(dat,0,dat.length);
                
                
               
            }
            
            }
            
    
        
        
        
        return con;
        
        
        
    }
    
    public String Send(Hashtable<String,String> params,String d)throws IOException
    {
        HttpURLConnection con = Prepare(params,d);
        byte[] octes=new byte[10];
        String content = "";
        
        if(con.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            content = readFullyAsString(con.getInputStream(),"UTF-8");
            
        }else{
            
            
        }
       
        return content;
        
    }
    
    
    public String BuildCookies()
    {
        String full_cookies="";
        CookieStore store = cookieManager.getCookieStore();
        
                
                List<HttpCookie> cookies = store.getCookies();
                
                
                for(HttpCookie item : cookies)
                {
                    
                    full_cookies+=item.getName()+"="+item.getValue()+";";
                }
                
                
                
                
                
                
                return full_cookies;
                
                
                
    }
    
    public String readFullyAsString(InputStream inputStream, String encoding) throws IOException {
        return readFully(inputStream).toString(encoding);
    }

    private ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }
    
    
    
    
    
    
}


class Hashtag
{
    private String id;
    private String name;
    
    private long media_count;
    
    private ArrayList<Post> posts;
    
    public Hashtag(String id,String name,long media)
    {
        this.id = id;
        this.name = name;
        this.media_count = media;
        
        this.posts = new ArrayList<Post>();
        
        
    }
    
}

class Post
{
   
    private String shortCode;
    private int likedBy;
   
    private String Caption;
    ArrayList<Hashtag> hashtags;
    public Post(String shortCode,int liked,String Caption)
    {
        hashtags=new ArrayList<Hashtag>();
       
        this.shortCode = shortCode;
        this.likedBy =likedBy;
     
        this.Caption =Caption;
        
    }
   public String getCode()
   {
       return this.shortCode;
   }
   public int getLikedBy()
   {
        return this.likedBy;
   }
    
    public String getText()
    {
        return this.Caption;
    }
}

class Owner
{
    String id;
    ArrayList<Post> posts;
   
    public Owner(String id)
    {
        this.id= id;
        this.posts =new ArrayList<Post>();
        
    }
    
    void Add(Post p)
    {
        this.posts.add(p);
    }
    
    public ArrayList<Post> getPosts()
    {
        return posts;
    }
    public String getId()
    {
        
        return this.id;
    }
}

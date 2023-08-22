import java.net.*;
import java.io.*;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class gdown {

    public static void Download_Direct(String uurl) throws Exception{


      URL GURL = new URL(uurl);
      // here we update { URLConnection } to { HttpURLConnection }
      //URLConnection GRes = GURL.openConnection();
      HttpURLConnection GRes = (HttpURLConnection)GURL.openConnection();

        
      GRes.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6)");
      GRes.setRequestProperty("Range", "bytes=0-");

      String Content_Disposition = GRes.getHeaderField("Content-Disposition");

      System.out.println("Content-Disposition : " + Content_Disposition);


        /* Extract Filename from Content-Disposition */
        String __filename = Content_Disposition.split(";")[1];
        __filename = __filename.substring(11, __filename.length()-1);

        

        

        System.out.println("filename : " + __filename);
        System.out.println("Content-Type : " + GRes.getContentType());
        System.out.println("Content-Length : " + GRes.getContentLengthLong());
        System.out.println("Content-Range : " + GRes.getHeaderField("Content-Range"));

        
       

        /* start Download */


        InputStream raw = GRes.getInputStream();
        InputStream in = new BufferedInputStream(raw);
        
        




        FileOutputStream fis = new FileOutputStream(__filename);
        byte[] buffer = new byte[1024];
        // here if there tmp file we should make count = size of tmp_file in bytes
        int count = 0;
        long sum_count = count;
        
        // count is N of downloaded bytes
        while ((count = in.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
            
            sum_count = sum_count+count;
            // here we calculate downloading percent
            float fpercent = (float) (sum_count) / (GRes.getContentLengthLong());
        
            fpercent = fpercent*100;
            //System.out.printf("%.2f%n",fpercent);
            // Now we convert float to integer to show it as P% format
            int ipercent = (int) (fpercent);

            System.out.println(ipercent+"%");

            
            
        }
        fis.close();
        in.close();

       



    }



    public static String get_url_from_gdrive_confirmation(String HTMLContents){

    
    Pattern pattern = Pattern.compile("id=\"download-form\" action=\"(.+?)\"", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(HTMLContents);
    
    boolean matchFound = matcher.find();
    if(matchFound) {
      String url = matcher.group(0);
      url = url.replace("&amp;", "&");

      url = url.substring(27, url.length()-1);

      return url;
    } else {
      return "null";
    }


      
    }


    public static String GID(String id){

      return "https://drive.google.com/uc?id="+id;

    }





    public static void Download(String myURL) throws Exception{


      URL GURL = new URL(myURL);
      // here we update { URLConnection } to { HttpURLConnection }
      //URLConnection GRes = GURL.openConnection();
      HttpURLConnection GRes = (HttpURLConnection)GURL.openConnection();

      /* Set Request Headers*/
      GRes.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6)");
      GRes.setRequestProperty("Range", "bytes=0-");  // like as Chrome Browser
      


      

      

      if(GRes.getResponseCode()==200 || GRes.getResponseCode()==206){
      
      String Content_Disposition = GRes.getHeaderField("Content-Disposition");

      if (Content_Disposition == null ){

          /*it returned text/html; charset=utf-8*/

          BufferedReader in = new BufferedReader(new InputStreamReader(GURL.openStream()));

          String inputLine;
          String HTMLContents = "";
          while ((inputLine = in.readLine()) != null)
                HTMLContents = HTMLContents + inputLine;
          in.close();

          

          String new_url = get_url_from_gdrive_confirmation(HTMLContents);
          if (new_url=="null"){

          System.out.println("new url is : null");

          }
          else{


            System.out.println("From (original) : " + myURL);
            System.out.println("From (redirected) : " + new_url);

            Download_Direct(new_url);



          }

          
          


      }

      else{



        Download_Direct(myURL);
      


      }


    }else{
      // Response code is not 200 OK || 206

      System.out.println("Server returned HTTP response code: " + GRes.getResponseCode() + " for url : "+GRes.getURL());


    }


    }











    public static void main(String[] args) throws Exception {

        // big https://drive.google.com/uc?id=1-11kz8jSB9kBW4wMoCuM7S8Iveg9Jt0r
        // lite https://drive.google.com/uc?id=1qk3ivTz7bZUNBl-D2ur1_eNG_7b6wm9P

        // Breaking-Bad 460 MB
        // https://drive.google.com/uc?id=1-6jznuEfuKUJGF_QNd9b068k5whtxJtF

        // google.ico 1-3di5UZwle99i1H_aquFJpH-GuVBdNQg


        Download(GID("1-3di5UZwle99i1H_aquFJpH-GuVBdNQg"));


       
    }
}
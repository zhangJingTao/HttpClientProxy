/**
 * Created by ZhangJingtao on 2014/8/25.
 * @see sun.net.www.http.HttpClient
 */
public class Client {
    /**
     * get方式
     *
     * @param destURL
     * @param chacter
     * @return String
     * @throws MalformedURLException
     * @throws IOException
     */
    public static HttpResponse sendGet(HttpClient client, String destURL,
                                       String chacter, HttpServletRequest request,String key)
            throws MalformedURLException, IOException {
        HttpGet get = new HttpGet(destURL);
        System.out.println("=================");
        System.out.println(request.getHeader("User-Agent"));
        System.out.println(request.getHeader("Accept"));
        System.out.println(request.getHeader("Content-Type"));
        System.out.println("=================");
        get.setHeader("User-Agent", request.getHeader("User-Agent"));
        get.setHeader("Accept", request.getHeader("Accept"));
        get.setHeader("Accept-Encoding", request.getHeader("Accept-Encoding"));
        get.setHeader("Connection",request.getHeader("Connection"));
        get.setHeader("Accept-Language", "zh-cn");
        get.setHeader("restSecHeader", key);
        HttpResponse response = client.execute(get);
        return response;
    }

    /**
     * POST方式
     * @param client
     * @param url
     * @param request
     * @param fileUploadPath
     * @param key
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws ClientProtocolException
     * @throws JSONException
     */
    public String post(HttpClient client, String url,
                           HttpServletRequest request,String fileUploadPath,String key)
            throws UnsupportedEncodingException, IOException,
            ClientProtocolException, JSONException {
        boolean isFileUpload = false;
        if (request instanceof MultipartHttpServletRequest) {
            isFileUpload = true;
        }

        HttpPost post = new HttpPost(url);
        // 创建表单参数列表
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        Map<Object, String[]> formValue = request.getParameterMap();
        // 填充表单
        for (Entry<Object, String[]> obj : formValue.entrySet()) {
            qparams.add(new BasicNameValuePair(obj.getKey().toString(), obj.getValue()[0]));
        }
        Map<Long, Map<String, Object>> projectMap = new HashMap<Long, Map<String, Object>>();
        StringBuffer html = new StringBuffer();

        if (isFileUpload) {//文件上传 特殊处理
            MultipartEntity reqEntity = new MultipartEntity();

            MultipartHttpServletRequest fileRequest = (MultipartHttpServletRequest) request;
            Iterator<String> fileNames = fileRequest.getFileNames();
            Map<String, MultipartFile> files = fileRequest.getFileMap();
            while (fileNames.hasNext()) {
                String name = fileNames.next();
                MultipartFile file = files.get(name);
                File uploadFile = new File(fileUploadPath
                        + "/"
                        + System.currentTimeMillis()
                        + "."
                        + file.getOriginalFilename().substring(
                        file.getOriginalFilename().lastIndexOf(".") + 1));
                file.transferTo(uploadFile);
                FileBody fileBody = new FileBody(uploadFile);
                reqEntity.addPart(name, fileBody);
            }
            for (NameValuePair nameValuePair : qparams) {
                StringBody sb = new StringBody(nameValuePair.getValue());
                reqEntity.addPart(nameValuePair.getName(), sb);
            }
            post.setEntity(reqEntity);
        }else {
            post.setEntity(new UrlEncodedFormEntity(qparams, "UTF-8"));
        }

        post.setHeader("User-Agent",request.getHeader("User-Agent"));
        post.setHeader("Accept",request.getHeader("Accept"));
        post.setHeader("Accept-Language", "zh-cn");
        post.setHeader("key", key);

        HttpResponse response2 = client.execute(post);
        int statusCode = response2.getStatusLine().getStatusCode();
        System.out.println("statusCode:" + statusCode);
        if (statusCode == 200) {
            HttpEntity entity2 = response2.getEntity();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(
                    entity2.getContent(),"UTF-8"));
            String buffer2;
            while ((buffer2 = reader2.readLine()) != null) {
                html.append(buffer2+"\n");
            }
            return html.toString();
        } else {
            return "";
        }
    }


}

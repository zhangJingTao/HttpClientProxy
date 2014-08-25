/**
 *  一个代理工具，能够将工程中的请求发送到其他服务器上
 *  支持get post，支持html返回、json返回、流返回
 *  支持文件上传
 *  @see Client
 */
public class Main {

    public static void main(String[] args) {

        @Value(value = "${your url}")
        String url;//代理请求的服务器

        @Value(value = "${a keyPair}")
        String key;//代理请求的密码

        @Value(value = "${file upload temp folder}")
        String fileUploadPath;//文件上传临时存放目录

        @Value(value = "the final server")
        String serverName;//代理请求服务器地址如：http://localhost:8080/serverName

        /**
         * 这个方法拦截了当前命名空间下所有的请求
         *
         */
        @RequestMapping(value = "/**")
        public String fliterUrl(Model uiModel, HttpServletRequest request,
                HttpServletResponse response) throws UnsupportedEncodingException,
                ClientProtocolException, IOException, JSONException {
            String url = request.getRequestURI() + "?";
            if (request.getMethod().toUpperCase().equals("GET")) {
                Map<String, String[]> paramMap = request.getParameterMap();
                for (Entry<String, String[]> obj : paramMap.entrySet()) {
                    url += obj.getKey();
                    if (obj.getValue()[0] != null) {
                        url += "=" + obj.getValue()[0].toString();
                    }
                    url += "&";
                }
                url = url.substring(0, url.length() - 1);
            }
            String contextPath = request.getContextPath();
            url = url.replace(contextPath, "").replace("/"+serverName, "");
            url = url + url;
            log.info("url:" + url);
            HttpClient client = new DefaultHttpClient();
            String content = new String();
            //for Get Method
            if (request.getMethod().toUpperCase().equals("GET")) {
                HttpResponse response2 = new Client().sendGet(client, url,
                        "", request,key);
                int statusCode = response2.getStatusLine().getStatusCode();
                System.out.println("statusCode:" + statusCode);
                StringBuffer html = new StringBuffer();
                if (statusCode == 200) {
                    String responseContextType = response2.getEntity()
                            .getContentType().getValue();
                    //返回的是文件
                    if (!responseContextType.contains("html")
                            && !responseContextType.contains("xml")
                            && !responseContextType.contains("json")) {
                        InputStream is = response2.getEntity().getContent();
                        for (Header h : response2.getAllHeaders()) {
                            if (h.getName().equals("Content-Disposition")) {
                                response.setHeader("Content-disposition", h.getValue());
                                break;
                            }
                        }
                        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
                        int ch;
                        while ((ch = is.read()) != -1) {
                            bytestream.write(ch);
                        }
                        byte imgdata[] = bytestream.toByteArray();
                        String len = new String(imgdata);

                        response.setContentType(responseContextType);
                        BufferedOutputStream bos = null;
                        bos = new BufferedOutputStream(response.getOutputStream());
                        bos.write(imgdata);
                        bos.close();
                        bytestream.close();
                        return null;
                    }
                    //返回的是值（json、html）
                    HttpEntity entity2 = response2.getEntity();
                    BufferedReader reader2 = new BufferedReader(
                            new InputStreamReader(entity2.getContent(), "UTF-8"));
                    String buffer2;
                    while ((buffer2 = reader2.readLine()) != null) {
                        html.append(buffer2 + "\n");
                    }
                }
                content = html.toString();
            } else {
                //post请求处理
                content = new Client().post(client, url, request,fileUploadPath,key);
            }
            //如果返回的数据能被json格式实例化，说明返回的是json
            try {
                JSONObject json = new JSONObject(content);
                return json.toString;
            } catch (Exception e) {
                // TODO: handle exception
            }
            uiModel.addAttribute("content", content);
            return "/modules/temp/page";//modules/temp/page只是一个文件内容填充由html来
        }
    }
}

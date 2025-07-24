package com.socialmedia.parser.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmedia.parser.mediaholder.MediaHolder;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class SubmitController {

    int ind = 0;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    Map<Integer, MediaHolder> mediaCache = new HashMap<>();

    // HttpClient is also designed to be reused for multiple requests
    private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1) // Or HTTP_2 if preferred and supported
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(10)) // Example timeout
        .build();

    private final RestTemplate rest = new RestTemplate();
    Pattern storiesPattern = Pattern.compile("instagram\\.com/stories/([^/]+)/([^/?#]+)");

    @PostMapping("/submit")
    public ResponseEntity<String> receiveText(@RequestBody TextPayload payload) {
        try {
            String inputUrl = payload.getText();

            if (inputUrl.contains("/p/") || inputUrl.contains("/reel")) {
                boolean flag = false;
                if (inputUrl.contains("/reel")) {
                    flag = true;
                }
                String shortcode = extractShortcode(inputUrl, flag);
                HttpHeaders headers = new HttpHeaders();
                ObjectMapper mapper = new ObjectMapper();

                GetInstagramPostRequest postRequest = new GetInstagramPostRequest(shortcode);

                HttpResponse<String> postResponse = getInstagramPostGraphQL(postRequest);
                System.out.println(postResponse.body());
                JsonNode jsonNode;
                MediaType type;
                String path;
                int size = mapper
                    .readTree(postResponse.body())
                    .path("data")
                    .path("xdt_shortcode_media")
                    .path("edge_sidecar_to_children")
                    .path("edges")
                    .size();
                jsonNode = mapper.readTree(postResponse.body()).path("data").path("xdt_shortcode_media");
                if (size == 0) {
                    size = 1;
                    if (jsonNode.has("video_url")) {
                        jsonNode = jsonNode.get("video_url");
                        path = jsonNode.asText();
                        type = MediaType.valueOf("video/mp4");
                        URL url = new URL(path);
                        showImage(0, url, type);
                    } else {
                        jsonNode = mapper
                            .readTree(postResponse.body())
                            .path("data")
                            .path("xdt_shortcode_media")
                            .path("display_resources")
                            .get(0)
                            .get("src");
                        path = jsonNode.asText();
                        type = MediaType.IMAGE_JPEG;
                        URL url = new URL(path);
                        showImage(0, url, type);
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        jsonNode = mapper
                            .readTree(postResponse.body())
                            .path("data")
                            .path("xdt_shortcode_media")
                            .path("edge_sidecar_to_children")
                            .path("edges")
                            .get(i);
                        if (jsonNode.path("node").has("video_url")) {
                            jsonNode = jsonNode.path("node").get("video_url");
                            path = jsonNode.asText();
                            type = MediaType.valueOf("video/mp4");
                            System.out.println("~~~~~~~~~~~~~~~~~~");
                        } else {
                            path = jsonNode.path("node").path("display_resources").get(2).get("src").asText();
                            type = MediaType.IMAGE_JPEG;
                            System.out.println("~~~~~~~~~~~~~~~~~~");
                        }
                        URL url = new URL(path);
                        showImage(i, url, type);
                    }

                    if (postResponse == null || postResponse.body() == null || postResponse.statusCode() != 200) {
                        return ResponseEntity.status(500).body("Failed to retrieve post data.");
                    }

                    ind = 0;
                }
                return ResponseEntity.ok(String.valueOf(size));
            } else if (inputUrl.contains("/stories/")) {
                String path;
                MediaType type;
                int size = 0;
                MediaHolder piece = new MediaHolder();
                boolean flag = true;
                int i = 0;

                String userId = extractStoriesShortcode(inputUrl)[0];
                if (userId.isEmpty()) {
                    flag = false;
                    userId = extractUserId(inputUrl);
                }
                String apiUrl = "https://i.instagram.com/api/v1/users/web_profile_info/?username=" + userId;

                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_3 like Mac OS X)");
                headers.set("x-ig-app-id", "936619743392459");
                headers.set(
                    "Cookie",
                    "fbm_124024574287414=base_domain=.instagram.com; ds_user_id=4545796667; mid=ZaT2DwALAAGMz3WVZO2bhMNlFNOy; oo=v1; ps_n=1; ps_l=1; csrftoken=Xak2wWk70BtU4X4K7YdENwQcutBn3D8Z; ig_did=3E4ADE00-C1EA-4437-A9B5-AD11947FC966; shbid=\"220\\0544545796667\\0541750010377:01f768f836780c1168d1590174a431ed04f216b1970543d28e33403414473c59f980e0d2\"; shbts=\"1718474377\\0544545796667\\0541750010377:01f704dbff9acbf2da9656cad0fcef9f1062370d6461047dde498155c74c878415a08b60\"; datr=zTJvZpDQJjfYzEVLfbitO8E3; sessionid=4545796667%3APaMNx4NYUczn8M%3A10%3AAYfNwdC_RXvrmOfEFwwNtCkVgT3leZj083-yryQ7DhQHhw; fbsr_124024574287414=eODoe66OzlaY9Msj59bwHqYfqaTJLBbqD69nywy_Xcg.eyJ1c2VyX2lkIjoiMTAwMDE0NjAxMjUyODU2IiwiY29kZSI6IkFRRGJXaWN3RV9JMHZZZnJucEp3UnlMTTJ5d2xvZDdQa1NOTFUzZUktVENYb3cxRkxmQ2JKb2Vmd0NmS1JfVzhpZWw0U25EcUxleTlYX2dGbWt3Y21WcGR6VDZ3Tnp3TmxQY3hzcFFDOUw0VXdaUS1EZHBhUVpob2NpZERLZ0RRR3g1RjdmdzhWR3FSTms2WGJPVzBOdHNrNzhUMFhPdGZJa2Fha25nblFmYjI2QzJzU04zQXg4TGJtVlNjOE1pVDB1V25Va1hWTzVQMGlQX3FXLWM4R0JRbUwxdTFHYVVSRVc0TXBCR0F0QjNmR0ljazRkN1oxQ1U3aGxWYTNieTF0SjdHVHZTaTBnSnNneUJpMjhZdWZTVGdkVkRyVExKLUV3aDVBYkkwbHlZeG43S2hpQzNzYjVSYUZLcXJwdG45ZnBULW1TMlA1VkVNQUpESnhvTmxoM3lpIiwib2F1dGhfdG9rZW4iOiJFQUFCd3pMaXhuallCTzMyMEZNS21XOUxzY1pBTEJZRjl3djdqdFh6R1JXWkFIalAxV01HMllGS3E0TXFrNW43SjZmbXFaQ3M4eElubVBnaFR5MVF4Z1JjNEhEbk1kRWRkcWZXVElSU2ZPVkpMbmMzc2pqUVJJblVyN2lxd291aXNJYm94b0pGMEh6ZnJKTk1EZU1LQTg0dFFRM1A2ODNMdUFoT2dLMkg5djNuRUljeWNXb1VVcnJJMGVVckNta1pEIiwiYWxnb3JpdGhtIjoiSE1BQy1TSEEyNTYiLCJpc3N1ZWRfYXQiOjE3MTg3MDE5Nzd9; wd=1912x452; fbsr_124024574287414=eODoe66OzlaY9Msj59bwHqYfqaTJLBbqD69nywy_Xcg.eyJ1c2VyX2lkIjoiMTAwMDE0NjAxMjUyODU2IiwiY29kZSI6IkFRRGJXaWN3RV9JMHZZZnJucEp3UnlMTTJ5d2xvZDdQa1NOTFUzZUktVENYb3cxRkxmQ2JKb2Vmd0NmS1JfVzhpZWw0U25EcUxleTlYX2dGbWt3Y21WcGR6VDZ3Tnp3TmxQY3hzcFFDOUw0VXdaUS1EZHBhUVpob2NpZERLZ0RRR3g1RjdmdzhWR3FSTms2WGJPVzBOdHNrNzhUMFhPdGZJa2Fha25nblFmYjI2QzJzU04zQXg4TGJtVlNjOE1pVDB1V25Va1hWTzVQMGlQX3FXLWM4R0JRbUwxdTFHYVVSRVc0TXBCR0F0QjNmR0ljazRkN1oxQ1U3aGxWYTNieTF0SjdHVHZTaTBnSnNneUJpMjhZdWZTVGdkVkRyVExKLUV3aDVBYkkwbHlZeG43S2hpQzNzYjVSYUZLcXJwdG45ZnBULW1TMlA1VkVNQUpESnhvTmxoM3lpIiwib2F1dGhfdG9rZW4iOiJFQUFCd3pMaXhuallCTzMyMEZNS21XOUxzY1pBTEJZRjl3djdqdFh6R1JXWkFIalAxV01HMllGS3E0TXFrNW43SjZmbXFaQ3M4eElubVBnaFR5MVF4Z1JjNEhEbk1kRWRkcWZXVElSU2ZPVkpMbmMzc2pqUVJJblVyN2lxd291aXNJYm94b0pGMEh6ZnJKTk1EZU1LQTg0dFFRM1A2ODNMdUFoT2dLMkg5djNuRUljeWNXb1VVcnJJMGVVckNta1pEIiwiYWxnb3JpdGhtIjoiSE1BQy1TSEEyNTYiLCJpc3N1ZWRfYXQiOjE3MTg3MDE5Nzd9; rur=\"CLN\\0544545796667\\0541750237984:01f76255c8c0d291b4f2db63d917ec6c091b49aa2ba990ae5cae0c92cb10e3bfe767ded0\""
                );

                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = rest.exchange(apiUrl, HttpMethod.GET, entity, String.class);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode userInnerId = root.path("data").path("user").path("id");

                String storiesUrl = "https://i.instagram.com/api/v1/feed/user/" + userInnerId.asText() + "/story/";
                ResponseEntity<String> idResponse = rest.exchange(storiesUrl, HttpMethod.GET, entity, String.class);
                JsonNode idRoot = mapper.readTree(idResponse.getBody());

                JsonNode items = idRoot.path("reel").path("items");

                if (!items.isArray() || items.size() == 0) {
                    throw new RuntimeException("No story items found.");
                }
                if (flag) {
                    size = 1;
                    while (flag) {
                        if (items.get(i).path("pk").asText().equals(extractStoriesShortcode(inputUrl)[1])) {
                            flag = false;
                        } else {
                            i++;
                        }
                    }
                    if (items.get(i).has("video_versions")) {
                        JsonNode node = items.get(i).path("video_versions").get(0).get("url");
                        path = node.asText();
                        type = MediaType.valueOf("video/mp4");
                    } else {
                        JsonNode node = items.get(i).path("image_versions2").path("candidates").get(0).get("url");
                        path = node.asText();
                        type = MediaType.IMAGE_JPEG;
                    }
                    URL url = new URL(path);
                    System.out.println("Video URL: " + url);
                    showImage(0, url, type);
                } else {
                    size = items.size();
                    for (int j = 0; j < size; j++) {
                        if (items.get(j).has("video_versions")) {
                            JsonNode node = items.get(j).path("video_versions").get(0).get("url");
                            path = node.asText();
                            type = MediaType.valueOf("video/mp4");
                        } else {
                            JsonNode node = items.get(j).path("image_versions2").path("candidates").get(0).get("url");
                            path = node.asText();
                            type = MediaType.IMAGE_JPEG;
                        }
                        URL url = new URL(path);
                        System.out.println("Video URL: " + url);
                        showImage(j, url, type);
                    }
                }

                return ResponseEntity.ok(String.valueOf(size));
            } else {
                // ✅ It’s a profile URL — extract username and get latest post
                Pattern usernamePattern = Pattern.compile("instagram\\.com/([a-zA-Z0-9._]+)/?");
                Matcher matcher = usernamePattern.matcher(inputUrl);
                if (!matcher.find()) {
                    return ResponseEntity.badRequest().body("Invalid Instagram profile URL.");
                }

                String username = matcher.group(1);

                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_3 like Mac OS X)");
                headers.set("x-ig-app-id", "936619743392459");

                HttpEntity<String> entity = new HttpEntity<>(headers);
                String apiUrl = "https://i.instagram.com/api/v1/users/web_profile_info/?username=" + username;
                ResponseEntity<String> response = rest.exchange(apiUrl, HttpMethod.GET, entity, String.class);

                if (!response.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.status(response.getStatusCode()).body("Failed to get user profile.");
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode mediaEdges = root.at("/data/user/edge_owner_to_timeline_media/edges");

                if (!mediaEdges.isArray() || mediaEdges.size() == 0) {
                    return ResponseEntity.status(404).body("No media found on user profile.");
                }

                JsonNode firstPost = mediaEdges.get(0).get("node");
                String mediaUrl = firstPost.get("display_url").asText();

                return ResponseEntity.ok(mediaUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    private String[] extractStoriesShortcode(String url) {
        String[] userdata = new String[2];
        Pattern pattern = Pattern.compile("instagram\\.com/stories/([^/]+)/([^/?#]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            System.out.println(matcher.group(2));
            userdata[0] = matcher.group(1);
            userdata[1] = matcher.group(2);
            return userdata;
        }
        return new String[] { "" };
    }

    private String extractUserId(String url) {
        Pattern pattern = Pattern.compile("instagram\\.com/stories/([^/]+)/?");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String extractShortcode(String url, boolean flag) {
        Pattern pattern = Pattern.compile("instagram\\.com/p/([^/]+)/?");
        if (flag) {
            pattern = Pattern.compile("instagram\\.com/reel/([^/]+)/?");
        }
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static class TextPayload {

        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public void showImage(int i, URL url, MediaType type) throws IOException {
        MediaHolder piece = new MediaHolder();
        InputStream in = new BufferedInputStream(url.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1 != (n = in.read(buf))) {
            out.write(buf, 0, n);
        }
        out.close();
        in.close();

        byte[] response = out.toByteArray();

        piece.setMedia(response, type);

        mediaCache.put(i, piece);
    }

    @GetMapping("/media/{index}")
    public ResponseEntity<byte[]> getMedia(@PathVariable int index) {
        MediaHolder holder = mediaCache.get(index);
        if (holder == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .contentType(holder.getContentType())
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=INSTAtool_" + RandomStringUtils.randomAlphanumeric(8))
            .body(holder.getMedia());
    }

    private static String generateRequestBody(String shortcode) throws JsonProcessingException {
        // Prepare the 'variables' part which needs to be JSON stringified
        Map<String, Object> variables = new HashMap<>();
        variables.put("shortcode", shortcode);
        variables.put("fetch_tagged_user_count", null); // Jackson will serialize null as JSON null
        variables.put("hoisted_comment_id", null);
        variables.put("hoisted_reply_id", null);

        // Main parameters for the form data
        Map<String, String> params = new HashMap<>();
        params.put("av", "0");
        params.put("__d", "www");
        params.put("__user", "0");
        params.put("__a", "1");
        params.put("__req", "b");
        params.put("__hs", "20183.HYP:instagram_web_pkg.2.1...0"); // Static, but potentially dynamic
        params.put("dpr", "3");
        params.put("__ccg", "GOOD");
        params.put("__rev", "1021613311"); // Static, but potentially dynamic
        params.put("__s", "hm5eih:ztapmw:x0losd"); // Static, but potentially dynamic
        params.put("__hsi", "7489787314313612244"); // Static, but potentially dynamic
        params.put(
            "__dyn",
            "7xeUjG1mxu1syUbFp41twpUnwgU7SbzEdF8aUco2qwJw5ux609vCwjE1EE2Cw8G11wBz81s8hwGxu786a3a1YwBgao6C0Mo2swtUd8-U2zxe2GewGw9a361qw8Xxm16wa-0oa2-azo7u3C2u2J0bS1LwTwKG1pg2fwxyo6O1FwlA3a3zhA6bwIxe6V8aUuwm8jwhU3cyVrDyo"
        ); // Static, but potentially dynamic
        params.put(
            "__csr",
            "goMJ6MT9Z48KVkIBBvRfqKOkinBtG-FfLaRgG-lZ9Qji9XGexh7VozjHRKq5J6KVqjQdGl2pAFmvK5GWGXyk8h9GA-m6V5yF4UWagnJzazAbZ5osXuFkVeGCHG8GF4l5yp9oOezpo88PAlZ1Pxa5bxGQ7o9VrFbg-8wwxp1G2acxacGVQ00jyoE0ijonyXwfwEnwWwkA2m0dLw3tE1I80hCg8UeU4Ohox0clAhAtsM0iCA9wap4DwhS1fxW0fLhpRB51m13xC3e0h2t2H801HQw1bu02j-"
        ); // Static, but potentially dynamic
        params.put("__comet_req", "7");
        params.put("lsd", "AVrqPT0gJDo"); // Static, but potentially dynamic
        params.put("jazoest", "2946"); // Static, but potentially dynamic
        params.put("__spin_r", "1021613311"); // Static, but potentially dynamic
        params.put("__spin_b", "trunk"); // Static, but potentially dynamic
        params.put("__spin_t", "1743852001"); // Static, but potentially dynamic
        params.put("__crn", "comet.igweb.PolarisPostRoute");
        params.put("fb_api_caller_class", "RelayModern");
        params.put("fb_api_req_friendly_name", "PolarisPostActionLoadPostQueryQuery");
        params.put("variables", objectMapper.writeValueAsString(variables)); // Add the JSON stringified 'variables'
        params.put("server_timestamps", "true"); // Boolean as string "true"
        params.put("doc_id", "8845758582119845");

        // Build the URL-encoded query string
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sj.add(
                URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) +
                "=" +
                URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", StandardCharsets.UTF_8)
            );
        }
        return sj.toString();
    }

    /**
     * Fetches Instagram post data using a GraphQL query.
     *
     * @param data The request data containing the shortcode.
     * // In JavaScript, requestConfig could be used to pass additional fetch options.
     * // Here, we could add a similar RequestConfigType parameter if needed to customize
     * // the HttpRequest.Builder further, or allow customization of HttpClient.
     * @return The raw JSON string response from Instagram.
     * @throws IOException if an I/O error occurs when sending or receiving.
     * @throws InterruptedException if the operation is interrupted.
     * @throws JsonProcessingException if there's an error forming the request body.
     */
    public static HttpResponse<String> getInstagramPostGraphQL(GetInstagramPostRequest data/*, RequestConfigType requestConfig */)
        throws IOException, InterruptedException, JsonProcessingException {
        String requestUrlString = "https://www.instagram.com/graphql/query";
        String requestBody = generateRequestBody(data.getShortcode());

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(URI.create(requestUrlString))
            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 11; SAMSUNG SM-G973U) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/14.2 Chrome/87.0.4280.141 Mobile Safari/537.36"
            )
            .header("Accept", "*/*")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("X-FB-Friendly-Name", "PolarisPostActionLoadPostQueryQuery")
            .header("X-BLOKS-VERSION-ID", "0d99de0d13662a50e0958bcb112dd651f70dea02e1859073ab25f8f2a477de96") // Static, but potentially dynamic
            .header("X-CSRFToken", "X6LAL9mpB3Dyw7Zy9I91a3COUkHCOINJ") // Highly likely to be dynamic
            .header("X-IG-App-ID", "1217981644879628") // Static app ID
            .header("X-FB-LSD", "AVrqPT0gJDo") // Likely dynamic
            .header("X-ASBD-ID", "359341") // Likely dynamic
            .header("Sec-GPC", "1") // Global Privacy Control header
            .header("Sec-Fetch-Dest", "empty") // Browser context header
            .header("Sec-Fetch-Mode", "cors") // Browser context header
            .header("Sec-Fetch-Site", "same-origin") // Browser context header
            .header("Pragma", "no-cache")
            .header("Cache-Control", "no-cache")
            .header("Referer", "https://www.instagram.com/p/" + data.getShortcode() + "/")
            .timeout(Duration.ofSeconds(15)) // Example request timeout
            .build();

        // Send the request and get the response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // For a production system, you'd check response.statusCode() here
        // e.g., if (response.statusCode() != 200) { throw new IOException("Unexpected status code: " + response.statusCode()); }

        // The raw JSON response body is returned.
        // Typically, you would parse this into a Java object (e.g., IG_GraphQLResponseDto)
        // using objectMapper.readValue(response.body(), IG_GraphQLResponseDto.class);
        return response;
    }

    class GetInstagramPostRequest {

        private String shortcode;

        public GetInstagramPostRequest(String shortcode) {
            this.shortcode = shortcode;
        }

        public String getShortcode() {
            return shortcode;
        }

        public void setShortcode(String shortcode) {
            this.shortcode = shortcode;
        }
    }
}

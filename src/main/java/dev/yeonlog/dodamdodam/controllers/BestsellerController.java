package dev.yeonlog.dodamdodam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BestsellerController {
    @Value("${aladin.api.key}")
    private String aladinApiKey;

    @RequestMapping(value = "/bestseller", method = RequestMethod.GET)
    public String bestsellerPage() {
        return "bestseller/bestseller";
    }

    @RequestMapping(value = "/api/bestseller", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getBestseller(
            @RequestParam(defaultValue = "Bestseller") String queryType,
            @RequestParam(defaultValue = "0") int categoryId,
            @RequestParam(defaultValue = "20") int maxResults) throws Exception {

        String url = "http://www.aladin.co.kr/ttb/api/ItemList.aspx"
                + "?ttbkey=" + aladinApiKey
                + "&QueryType=" + queryType
                + "&MaxResults=" + maxResults
                + "&start=1"
                + "&SearchTarget=Book"
                + "&CategoryId=" + categoryId
                + "&output=js"
                + "&Version=20131101";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sb.toString());
        JsonNode items = root.path("item");

        List<Map<String, Object>> books = new ArrayList<>();
        for (JsonNode item : items) {
            books.add(Map.of(
                    "rank",        item.path("bestRank").asInt(),
                    "title",       item.path("title").asText(),
                    "author",      item.path("author").asText(),
                    "publisher",   item.path("publisher").asText(),
                    "cover",       item.path("cover").asText(),
                    "link",        item.path("link").asText(),
                    "isbn13",      item.path("isbn13").asText(),
                    "category",    item.path("categoryName").asText()
            ));
        }

        return ResponseEntity.ok(Map.of("result", "SUCCESS", "books", books));
    }
}

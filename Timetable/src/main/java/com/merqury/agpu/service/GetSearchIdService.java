package com.merqury.agpu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.merqury.agpu.DTO.Groups;
import com.merqury.agpu.DTO.SearchProduct;
import com.merqury.agpu.enums.TimetableOwner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GetSearchIdService {
    private final String url;
    private final ObjectMapper jsonConverter;
    private final String urlToMainPage;

    public GetSearchIdService(){
        this.jsonConverter = new ObjectMapper();
        url = "http://www.it-institut.ru/SearchString/KeySearch?Id=118&SearchProductName=%s";
        urlToMainPage = "http://www.it-institut.ru/SearchString/Index/118";
    }

    public int getSearchId(String searchText, TimetableOwner owner){

        return switch (owner) {
            case GROUP -> getSearchIdByType(searchText, "Group");
            case TEACHER -> getSearchIdByType(searchText, "Teacher");
            case CLASSROOM -> getSearchIdByType(searchText, "Classroom");
        };
    }

    private int getSearchIdByType(String searchText, String expectedType){
        SearchProduct[] result = tryToGetSearchProductArrayFromUrl(url, searchText);
        return Arrays.stream(result)
                .filter(element -> Objects.equals(element.Type, expectedType))
                .map(element -> element.SearchId)
                .findFirst()
                .orElse(0);
    }

    public String getFullGroupName(String groupName){
        return getSearchContent(groupName, "Group");
    }

    public String getSearchContent(String searchText, String expectedType){
        SearchProduct[] result = tryToGetSearchProductArrayFromUrl(url, searchText);
        return Arrays.stream(result)
                .filter(element -> Objects.equals(element.Type, expectedType))
                .map(element -> element.SearchContent)
                .findFirst()
                .orElse("None");
    }

    private SearchProduct[] tryToGetSearchProductArrayFromUrl(String url, String searchString) {
        try {
            return getSearchProductArrayFromUrl(url, searchString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SearchProduct[] getSearchProductArrayFromUrl(String url, String searchString) throws IOException{
        return jsonConverter.readValue(
                new URL(url.formatted(URLEncoder.encode(searchString, StandardCharsets.UTF_8))),
                SearchProduct[].class
        );
    }

    public List<Groups> getAllGroupsFromMainPage(){
        String dataFromMainPage = getDataFromUrl(urlToMainPage);
        List<Groups> result = new ArrayList<>();
        Document document = Jsoup.parse(dataFromMainPage);
        document.getElementsByClass("card")
                .forEach(element -> result.add(parseCardElement(element)));
        return result;
    }

    private String getDataFromUrl(String url){
        InputStream inputStream = tryToOpenStreamOnUrl(url);
        return readStringFromInputStream(inputStream);
    }

    private String readStringFromInputStream(InputStream stream){
        Scanner sc = new Scanner(stream);
        StringBuilder builder = new StringBuilder();
        while (sc.hasNextLine()){
            builder.append(sc.nextLine()).append("\n");
        }
        return builder.toString();
    }

    private InputStream tryToOpenStreamOnUrl(String url){
        try {
            return openStreamOnUrl(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream openStreamOnUrl(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        return connection.getInputStream();
    }

    private Groups parseCardElement(Element cardElement){
        Groups result = new Groups();
        result.setFacultyName(
                cardElement.getElementsByTag("button").first().text()
        );

        cardElement.getElementsByClass("p-2")
                .forEach(element -> result.getGroups().add(getGroupNameFromP2Element(element)));
        return result;
    }

    private String getGroupNameFromP2Element(Element element){
        return element.getAllElements().first().text();
    }
}

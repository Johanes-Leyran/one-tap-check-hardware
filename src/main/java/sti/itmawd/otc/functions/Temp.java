package sti.itmawd.otc.functions;

import org.json.JSONObject;

import java.net.http.HttpResponse;

public class Temp {
    //import java.net.URI;
    //import java.net.http.HttpClient;
    //import java.net.http.HttpRequest;
    //import java.net.http.HttpResponse;
    //import org.json.JSONArray;
    //import org.json.JSONObject;
    //
    //public class Main {
    //    public static void main(String[] args) throws Exception {
    //        HttpClient httpClient = HttpClient.newBuilder()
    //                .followRedirects(HttpClient.Redirect.ALWAYS)
    //                .build();
    //
    //        HttpRequest request = HttpRequest.newBuilder()
    //                .uri(URI.create("https://mashape-community-urban-dictionary.p.rapidapi.com/define?term=wordington"))
    //                .header("X-RapidAPI-Key", "f189ebea81msh367a739984ac2bbp1dd2bbjsne4f4d43ab8b6")
    //                .header("X-RapidAPI-Host", "mashape-community-urban-dictionary.p.rapidapi.com")
    //                .GET()
    //                .build();
    //
    //        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    //
    //        System.out.println("Status Code: " + response.statusCode());
    //
    //        // Parse the JSON response
    //        JSONObject jsonResponse = new JSONObject(response.body());
    //
    //        // Get the "list" array from the JSON response
    //        JSONArray listArray = jsonResponse.getJSONArray("list");
    //
    //        // Check if there is at least one result
    //        if (listArray.length() > 0) {
    //            // Display only the first result
    //            JSONObject firstResult = listArray.getJSONObject(0);
    //            System.out.println("First Result in JSON format:");
    //            System.out.println(firstResult.toString(2)); // The '2' argument is for indentation
    //        } else {
    //            System.out.println("No results found.");
    //        }
    //    }
    //}
    //
    //HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    //JSONObject obj = new JSONObject(response.body());
    //    System.out.println(response.statusCode());
    //    System.out.println(obj);
}

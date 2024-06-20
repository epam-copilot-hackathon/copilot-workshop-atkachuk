package com.microsoft.hackathon.copilotdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.util.LinkedHashMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;
import java.util.HashMap;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.ByteArrayResource;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

@RestController
public class DemoController {

    @GetMapping("/hello")
    public String getValue(@RequestParam(required = false) String key) {
        if (key == null) {
            return "key not passed";
        } else {
            return "hello " + key;
        }
    }

    @GetMapping("/diffdates")
    public long diffDates(@RequestParam("date1") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date1,
                      @RequestParam("date2") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date2) {
        return ChronoUnit.DAYS.between(date1, date2);
    }

    @GetMapping("/validatePhoneNumber")
    public boolean validatePhoneNumber(@RequestParam String phoneNumber) {
        String regex = "^\\+34[679]\\d{8}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    @GetMapping("/validateDNI")
    public boolean validateDNI(@RequestParam String dni) {
        String regex = "\\d{8}[A-HJ-NP-TV-Z]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dni);
        return matcher.matches();
    }

    @GetMapping("/color/{colorName}")
    public ResponseEntity<String> getColorHexCode(@PathVariable String colorName) {
        try {
            // Read the colors.json file
            InputStream inputStream = getClass().getResourceAsStream("/resources/colors.json");
            if (inputStream == null) {
                throw new FileNotFoundException("colors.json not found in resources directory");
            }

            // Parse the file into a Map
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<Map<String, String>> typeReference = new TypeReference<>() {};
            Map<String, String> colorMap = objectMapper.readValue(inputStream, typeReference);

            // Get the hexadecimal color code for the given color name
            String colorHexCode = colorMap.get(colorName);
            if (colorHexCode == null) {
                return new ResponseEntity<>("Color not found", HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(colorHexCode, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Error reading colors.json", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/joke")
    public String getJoke() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String jokeApiUrl = "https://api.chucknorris.io/jokes/random";
            ResponseEntity<String> response = restTemplate.getForEntity(jokeApiUrl, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            String joke = root.path("value").asText();

            return joke;
        } catch (Exception e) {
            return "Error fetching joke";
        }
    }

    @GetMapping("/parseUrl")
    public Map<String, String> parseUrl(@RequestParam String url) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            URI aURI = new URI(url);
            result.put("protocol", aURI.getScheme());
            result.put("authority", aURI.getAuthority());
            result.put("host", aURI.getHost());
            result.put("port", String.valueOf(aURI.getPort()));
            result.put("path", aURI.getPath());
            result.put("query", aURI.getQuery());
        } catch (URISyntaxException e) {
            result.put("error", "Invalid URL");
        }
        return result;
    }

    @GetMapping("/listFilesAndFolders")
    public ResponseEntity<FolderContentResponse> listFilesAndFolders(@RequestParam String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        List<String> files = new ArrayList<>();
        List<String> folders = new ArrayList<>();
        FolderContentResponse response = new FolderContentResponse();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    files.add(file.getPath());
                } else if (file.isDirectory()) {
                    folders.add(file.getPath());
                }
            }
        }
        response.setFiles(files);
        response.setFolders(folders);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/wordCount")
    public ResponseEntity<Map<String, Integer>> wordCount(@RequestParam String path, @RequestParam String word) {
        Map<String, Integer> response = new HashMap<>();
        File file = new File(path);
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Scanner scanner = new Scanner(line);
                while (scanner.hasNext()) {
                    if (scanner.next().equals(word)) {
                        count++;
                    }
                }
                scanner.close();
            }
        } catch (IOException e) {
            response.put("error", -1);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put(word, count);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/zipFolder")
	public ResponseEntity<Resource> zipFolder(@RequestParam String path) throws IOException {
		Path folderPath = Paths.get(path);
		String zipFileName = folderPath.getFileName().toString() + ".zip";
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

		Files.walk(folderPath).filter(Files::isRegularFile).forEach(file -> {
			try {
				ZipEntry zipEntry = new ZipEntry(folderPath.relativize(file).toString());
				zipOutputStream.putNextEntry(zipEntry);
				FileInputStream fileInputStream = new FileInputStream(file.toFile());
				byte[] bytes = new byte[1024];
				int length;
				while ((length = fileInputStream.read(bytes)) >= 0) {
					zipOutputStream.write(bytes, 0, length);
				}
				fileInputStream.close();
				zipOutputStream.closeEntry();
			} catch (IOException e) {
				throw new RuntimeException("Failed to zip file", e);
			}
		});

		zipOutputStream.close();
		ByteArrayResource resource = new ByteArrayResource(byteArrayOutputStream.toByteArray());

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zipFileName)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}

}
package com.mp3Image.demo.mp3;

import jakarta.servlet.http.HttpServletRequest;
import org.jaudiotagger.tag.images.Artwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class Mp3TagChangeController {


    private Mp3TagChangeHandler mp3TagChangeHandler;
    @Autowired
    private FileStorageService fileStorageService;
    private static final Logger logger = LoggerFactory.getLogger(Mp3TagChangeController.class);


    @PostMapping("/uploadFile")
    public ResponseEntity<Resource> uploadFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "title") Optional<String> title,
                                               @RequestParam(value = "artist") Optional<String> artist,
                                               @RequestParam(value = "album") Optional<String> album,
                                               @RequestParam("artwork") MultipartFile artwork,
                                               HttpServletRequest request) {
        String fileName = fileStorageService.storeFile(file);
        String artworkName = fileStorageService.storeFile(artwork);
        String artist_name=null;
        if(artist.isPresent()){
            artist_name=artist.get();
        }
        String title_name=null;
        if(title.isPresent()){
            title_name=title.get();
        }
        String album_name=null;
        if(album.isPresent()){
            album_name=album.get();
        }
        try {
            File fileasMp3 = new File(fileStorageService.loadFileAsResource(fileName).getURI());
            return changeMp3Tag(artist_name, title_name, album_name, new File(fileStorageService.loadFileAsResource(artworkName).getURI()), fileasMp3,fileName,request);

        } catch (Exception e) {
            logger.warn(e.toString());
            return new ResponseEntity("There has been a Server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Resource> changeMp3Tag(String artist, String title, String album, File artworkFile, File file, String fileName, HttpServletRequest request) {

        String contentType = null;
        this.mp3TagChangeHandler = new Mp3TagChangeHandler(file);
        mp3TagChangeHandler.changeMp3Data(title, artist, album);
        mp3TagChangeHandler.changeMp3CoverArt(artworkFile);
        logger.warn(this.mp3TagChangeHandler.readMp3Data());
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
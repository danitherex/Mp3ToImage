package com.mp3Image.demo.mp3;

import jakarta.servlet.http.HttpServletRequest;
import org.jaudiotagger.tag.images.Artwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Optional;

@RestController
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

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();
        try {
            File fileasMp3 = new File(fileStorageService.loadFileAsResource(fileName).getURI());
            this.mp3TagChangeHandler = new Mp3TagChangeHandler(fileasMp3);
            mp3TagChangeHandler.changeMp3Data(title.get(), artist.get(), album.get());
            File artworkFile = new File(fileStorageService.loadFileAsResource(artworkName).getURI());
            mp3TagChangeHandler.changeMp3CoverArt(artworkFile);
            logger.warn(this.mp3TagChangeHandler.readMp3Data());
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            mp3TagChangeHandler.deleteMp3FileAndArtwork();
            String contentType = null;
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

        } catch (Exception e) {
            logger.warn(e.getMessage());
            return new ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
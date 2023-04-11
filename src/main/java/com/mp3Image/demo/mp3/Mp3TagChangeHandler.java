package com.mp3Image.demo.mp3;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

public class Mp3TagChangeHandler {

    private MP3File file;
    private ID3v24Tag v24tag;
    Logger logger = Logger.getLogger(Mp3TagChangeHandler.class.getName());
    String ArtworkPath;

    AbstractID3v2Tag tagv2;
    private Tag tag;

    public Mp3TagChangeHandler (File mp3File){
        try {
            this.file = (MP3File) AudioFileIO.read(mp3File);
            this.v24tag = this.file.getID3v2TagAsv24();
            this.tagv2 = this.file.getID3v2Tag();
            this.file.setTag(this.v24tag);
            this.tag = this.file.getTag();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String readMp3Data (){
        String data = "Title: " + this.v24tag.getFirst(ID3v24Frames.FRAME_ID_TITLE) + ", Artist: " + this.v24tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST) + ", Album: " + this.v24tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM);
        return data;
    }

    public void changeMp3Data (String title, String artist, String album){
        try {
            if(title != null){
                this.tag.setField(FieldKey.TITLE, title);
            }if(artist != null){
                this.tag.setField(FieldKey.ARTIST, artist);
            }if(album != null){
                this.tag.setField(FieldKey.ALBUM, album);
            }
            AudioFileIO.write(this.file);
        } catch (FieldDataInvalidException e) {
            throw new RuntimeException(e);
        } catch (CannotWriteException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeMp3CoverArt (File artworkFile){
        try {
            this.ArtworkPath = artworkFile.getPath();
            Artwork artwork = ArtworkFactory.createArtworkFromFile(this.file.getFile());
            byte[] bytes = FileUtils.readFileToByteArray(artworkFile);
            artwork.setBinaryData(bytes);
            tagv2.deleteArtworkField();
            tagv2.setField(artwork);
            this.file.setTag(tagv2);
            AudioFileIO.write(this.file);
            logger.info("Artwork added to mp3 file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CannotWriteException e) {
            throw new RuntimeException(e);
        } catch (FieldDataInvalidException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteMp3FileAndArtwork (){
        try {
            Files.delete(this.file.getFile().toPath());
            Files.delete(new File(this.ArtworkPath).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

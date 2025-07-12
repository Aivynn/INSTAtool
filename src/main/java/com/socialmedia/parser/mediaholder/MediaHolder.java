package com.socialmedia.parser.mediaholder;

import org.springframework.http.MediaType;

public class MediaHolder {

    private byte[] mediaBytes;
    private MediaType contentType;

    public void setMedia(byte[] bytes, MediaType type) {
        mediaBytes = bytes;
        contentType = type;
    }

    public byte[] getMedia() {
        return mediaBytes;
    }

    public MediaType getContentType() {
        return contentType;
    }
}

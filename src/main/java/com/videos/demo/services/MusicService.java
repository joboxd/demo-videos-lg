package com.videos.demo.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.videos.demo.entity.MusicEntity;
import com.videos.demo.entity.VideoEntity;
import com.videos.demo.repository.MusicRepository;
import com.videos.demo.repository.VideoRepository;

@Service
public class MusicService {
    private final MusicRepository musicRepository;

    public MusicService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    public MusicEntity saveVideo(MultipartFile file) throws IOException {
        MusicEntity music = new MusicEntity();
        music.setName(file.getOriginalFilename());
        music.setData(file.getBytes());
        music.setContentType(file.getContentType());
        return musicRepository.save(music);
    }

    public File saveVideoToTempFile(Long musicId) throws IOException {
        MusicEntity music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Cancion no encontrada"));

        File tempFile = File.createTempFile("music_" + musicId, ".mp3");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(music.getData());
        }
        return tempFile;
    }

    public MusicEntity getVideo(Long id) {
        return musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video no encontrado"));
    }
}

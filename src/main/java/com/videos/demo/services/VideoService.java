package com.videos.demo.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.videos.demo.entity.VideoEntity;
import com.videos.demo.repository.VideoRepository;

@Service
public class VideoService {
    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public VideoEntity saveVideo(MultipartFile file) throws IOException {
        VideoEntity video = new VideoEntity();
        video.setName(file.getOriginalFilename());
        video.setData(file.getBytes());
        video.setContentType(file.getContentType());
        return videoRepository.save(video);
    }

    public File saveVideoToTempFile(Long videoId) throws IOException {
        VideoEntity video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video no encontrado"));

        File tempFile = File.createTempFile("video_" + videoId, ".mp4");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(video.getData());
        }
        return tempFile;
    }

    public VideoEntity getVideo(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video no encontrado"));
    }
}

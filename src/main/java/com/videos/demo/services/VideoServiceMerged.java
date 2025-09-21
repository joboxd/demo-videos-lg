package com.videos.demo.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.videos.demo.entity.VideoEntity;

@Service
public class VideoServiceMerged {
    private final VideoService videoService;

    public VideoServiceMerged(VideoService videoService) {
        this.videoService = videoService;
    }

    public File mergeVideos(File video1, File video2) throws IOException, InterruptedException {
        File output = File.createTempFile("merged_", ".mp4");
        // Apilandolos verticalmente
        String command = String.format(
                "ffmpeg -i %s -i %s -filter_complex [0:v][1:v]vstack=inputs=2[v] -map [v] -c:v libx264 %s",
                video1.getAbsolutePath(),
                video2.getAbsolutePath(),
                output.getAbsolutePath());
        // para linux
        // ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
        // para windows:
        ProcessBuilder builder = new ProcessBuilder("bash", "/c", command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // logs
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Error al ejecutar FFmpeg, código: " + exitCode);
        }
        return output;
    }

    public VideoEntity saveMergedVideo(File videoMerged) throws IOException {
        return videoService.saveVideo((MultipartFile) videoMerged);
    }
}

package com.videos.demo.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.videos.demo.entity.VideoEntity;
import com.videos.demo.repository.VideoRepository;

@Service
public class VideoServiceMerged {
    private final VideoRepository videoRepository;

    public VideoServiceMerged(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public File mergeVideos(File video1, File video2) throws IOException, InterruptedException {
        File output = File.createTempFile("merged_", ".mp4");

        String filter = "[0:v]scale=540:640:force_original_aspect_ratio=decrease," +
                "pad=540:640:(ow-iw)/2:(oh-ih)/2[v0];" +
                "[1:v]scale=540:640:force_original_aspect_ratio=decrease," +
                "pad=540:640:(ow-iw)/2:(oh-ih)/2[v1];" +
                "[v0][v1]vstack=inputs=2[v]";

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", video1.getAbsolutePath(),
                "-i", video2.getAbsolutePath(),
                "-filter_complex", filter,
                "-map", "[v]",
                "-map", "0:a?",
                "-c:v", "libx264",
                "-preset", "ultrafast",
                "-crf", "28",
                "-c:a", "aac",
                "-shortest",
                output.getAbsolutePath());

        Process process = pb.start();

        // Hilo para consumir la salida estándar
        Thread stdoutThread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println("FFmpeg >> " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Hilo para consumir la salida de error
        Thread stderrThread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.err.println("FFmpeg ERR >> " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        stdoutThread.start();
        stderrThread.start();

        int exitCode = process.waitFor();
        stdoutThread.join();
        stderrThread.join();

        if (exitCode != 0) {
            throw new RuntimeException("Error al ejecutar FFmpeg, código: " + exitCode);
        }

        return output;
    }

    public VideoEntity saveMergedVideo(File videoMerged, String name) throws IOException {
        byte[] data = Files.readAllBytes(videoMerged.toPath());
        VideoEntity entity = new VideoEntity();
        entity.setData(data);
        entity.setName(name);
        String contentType = Files.probeContentType(videoMerged.toPath());
        if (contentType == null) {
            contentType = "video/mp4"; // Valor por defecto
        }
        entity.setContentType(contentType);
        return videoRepository.save(entity);
    }
}

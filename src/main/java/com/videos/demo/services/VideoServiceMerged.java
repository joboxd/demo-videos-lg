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

    public File mergeVideos(File video1, File video2, File music) throws IOException, InterruptedException {
        File output = File.createTempFile("merged_", ".mp4");
        File assFile = File.createTempFile("subtitles", ".ass");

        // Crear contenido ASS (más compatible)
        String assContent = "[Script Info]\n" +
                "ScriptType: v4.00+\n" +
                "PlayResX: 540\n" +
                "PlayResY: 640\n" +
                "WrapStyle: 0\n\n" +
                "[V4+ Styles]\n" +
                "Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, " +
                "Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, " +
                "Alignment, MarginL, MarginR, MarginV, Encoding\n" +
                "Style: Default,Arial,20,&H00FFFFFF,&H000000FF,&H00000000,&H80000000,0,0,0,0,100,100,0,0,1,2,0,5,10,10,10,1\n\n"
                +
                "[Events]\n" +
                "Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n" +
                "Dialogue: 0,0:00:00.00,0:00:05.00,Default,,0,0,0,,Cabina: Este es el inicio de tu viaje, por el cual todo marcha bien\n"
                +
                "Dialogue: 0,0:00:10.00,0:00:15.00,Default,,0,0,0,,Carretera: Pero es en este punto donde pudiste hacerlo mejor...\n";

        Files.write(assFile.toPath(), assContent.getBytes());

        // Usar el filtro ass en lugar de subtitles
        String assPath = assFile.getAbsolutePath()
                .replace("\\", "/")
                .replace(":", "\\:");
        String filter = "[0:v]scale=540:640:force_original_aspect_ratio=decrease," +
                "pad=540:640:(ow-iw)/2:(oh-ih)/2[v0];" +
                "[1:v]scale=540:640:force_original_aspect_ratio=decrease," +
                "pad=540:640:(ow-iw)/2:(oh-ih)/2[v1];" +
                "[v0][v1]vstack=inputs=2[merged];" +
                "[merged]ass=filename='" + assPath + "'[v];" +
                "[2:a]volume=0.3,equalizer=f=1000:width_type=o:width=2:g=-5[a]";
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", video1.getAbsolutePath(),
                "-i", video2.getAbsolutePath(),
                "-i", music.getAbsolutePath(),
                "-filter_complex", filter,
                "-map", "[v]",
                "-map", "[a]",
                "-c:v", "libx264",
                "-preset", "ultrafast",
                "-crf", "28",
                "-c:a", "aac",
                "-shortest",
                output.getAbsolutePath());

        Process process = pb.start();
        System.out.println("Path de archivo sutitlos: " + assPath);
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
        // Limpiar archivo temporal
        assFile.delete();

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

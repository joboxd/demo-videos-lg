package com.videos.demo.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.videos.demo.entity.VideoEntity;
import com.videos.demo.services.VideoService;
import com.videos.demo.services.VideoServiceMerged;

@RestController
@RequestMapping("/api")
public class VideoController {
    private final VideoService videoService;
    private final VideoServiceMerged videoServiceMerged;

    public VideoController(VideoService videoService, VideoServiceMerged videoServiceMerged) {
        this.videoService = videoService;
        this.videoServiceMerged = videoServiceMerged;
    }

    @PostMapping("/up´load")
    public VideoEntity upLoad(@RequestParam("file") MultipartFile file) throws IOException {
        return videoService.saveVideo(file);
    }

    @GetMapping("/getVideoById/{id}")
    public ResponseEntity<byte[]> getVideo(@PathVariable Long id) {
        VideoEntity video = videoService.getVideo(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(video.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + video.getName() + "\"")
                .body(video.getData());
    }

    @PostMapping("/merge")
    public VideoEntity mergeVideos(@RequestParam Long videoId1,
            @RequestParam Long videoId2,
            @RequestParam(defaultValue = "merged.mp4") String name) throws Exception {

        File v1 = videoService.saveVideoToTempFile(videoId1);
        File v2 = videoService.saveVideoToTempFile(videoId2);

        File merged = videoServiceMerged.mergeVideos(v1, v2);

        return videoServiceMerged.saveMergedVideo(merged);
    }
}

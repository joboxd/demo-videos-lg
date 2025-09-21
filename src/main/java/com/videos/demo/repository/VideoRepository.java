package com.videos.demo.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.videos.demo.entity.VideoEntity;

@Repository
public interface VideoRepository extends CrudRepository<VideoEntity, Long> {

}

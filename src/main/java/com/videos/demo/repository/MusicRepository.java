package com.videos.demo.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.videos.demo.entity.MusicEntity;

@Repository
public interface MusicRepository extends CrudRepository<MusicEntity, Long> {

}

package com.corty.backend.controller;

import com.corty.backend.mapper.ClubMapper;
import com.corty.backend.mapper.CourtMapper;
import com.corty.backend.services.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/clubs")
@RequiredArgsConstructor
public class ClubController {
    private final ClubService clubService;
    private final ClubMapper clubMapper;
    private final CourtMapper courtMapper;
}

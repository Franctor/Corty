package com.corty.backend.controller;

import com.corty.backend.mapper.FriendshipMapper;
import com.corty.backend.mapper.PlayerMapper;
import com.corty.backend.services.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/players")
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerService playerService;
    private final PlayerMapper playerMapper;
    private final FriendshipMapper friendshipMapper;
}

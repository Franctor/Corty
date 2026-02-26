package com.corty.backend.controller;

import com.corty.backend.mapper.GenericLookupMapper;
import com.corty.backend.services.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/support")
@RequiredArgsConstructor
public class SupportController {
    private final SupportService supportService;
    private final GenericLookupMapper genericLookupMapper;
}

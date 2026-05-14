package com.taskmanager.controller;

import com.taskmanager.model.MemberLoad;
import com.taskmanager.service.MemberLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-loads")
@CrossOrigin(origins = "http://localhost:3001")
public class MemberLoadController {

    @Autowired
    private MemberLoadService memberLoadService;

    @GetMapping
    public ResponseEntity<List<MemberLoad>> getMemberLoads() {
        return ResponseEntity.ok(memberLoadService.getMemberLoads());
    }

    @GetMapping("/{memberName}")
    public ResponseEntity<MemberLoad> getMemberLoad(@PathVariable String memberName) {
        MemberLoad load = memberLoadService.getMemberLoad(memberName);
        if (load == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(load);
    }

    @GetMapping("/overloaded")
    public ResponseEntity<List<String>> getOverloadedMembers() {
        return ResponseEntity.ok(memberLoadService.getOverloadedMembers());
    }
}

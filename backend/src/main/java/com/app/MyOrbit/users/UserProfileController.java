package com.app.MyOrbit.users;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/profile")
public class UserProfileController {
    private final AuthService authService;
    private final UserProfileRepository profileRepository;

    public UserProfileController(AuthService authService, UserProfileRepository profileRepository) {
        this.authService = authService;
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public ResponseEntity<UserProfile> get(@RequestHeader("Authorization") String authorization) {
        User user = authService.requireUser(authorization);
        return profileRepository.findById(user.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping
    public UserProfile update(@RequestHeader("Authorization") String authorization, @RequestBody UpdateUserProfileRequest request) {
        User user = authService.requireUser(authorization);
        UserProfile profile = profileRepository.findById(user.getId()).orElseGet(UserProfile::new);
        profile.setUserId(user.getId());
        profile.setProgram(textOrEmpty(request.program()));
        profile.setSemester(textOrEmpty(request.semester()));
        profile.setStudentCode(textOrEmpty(request.studentCode()));
        profile.setGradeTarget(textOrEmpty(request.gradeTarget()));
        return profileRepository.save(profile);
    }

    private String textOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
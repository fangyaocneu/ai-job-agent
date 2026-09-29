package com.fangyao.agent;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class CandidateProfileController {

    private final CandidateProfileRepository repository;

    public CandidateProfileController() {

        this.repository =
                new CandidateProfileRepository();
    }

    @GetMapping
    public ResponseEntity<CandidateProfile> getProfile() {

        CandidateProfile profile =
                repository.loadProfile();

        if (profile == null) {

            return ResponseEntity.notFound()
                    .build();
        }

        return ResponseEntity.ok(
                profile
        );
    }

    @PutMapping
    public ResponseEntity<CandidateProfile> updateProfile(
            @RequestBody CandidateProfile profile
    ) {

        CandidateProfile existingProfile =
                repository.loadProfile();

        if (existingProfile == null) {

            return ResponseEntity.notFound()
                    .build();
        }

        profile.setId(
                existingProfile.getId()
        );

        CandidateProfile updatedProfile =
                repository.updateProfile(
                        profile
                );

        return ResponseEntity.ok(
                updatedProfile
        );
    }
}
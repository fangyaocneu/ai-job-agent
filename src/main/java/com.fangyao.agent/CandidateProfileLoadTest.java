package com.fangyao.agent;

public class CandidateProfileLoadTest {

    public static void main(String[] args) {

        CandidateProfileRepository repository =
                new CandidateProfileRepository();

        CandidateProfile profile =
                repository.loadProfile();

        if (profile == null) {

            System.out.println(
                    "No candidate profile found."
            );

            return;
        }

        System.out.println(
                "===== CANDIDATE PROFILE ====="
        );

        System.out.println(
                "Name: "
                        + profile.getName()
        );

        System.out.println(
                "Languages: "
                        + profile.getLanguages()
        );

        System.out.println(
                "Backend: "
                        + profile.getBackendTechnologies()
        );

        System.out.println(
                "Cloud: "
                        + profile.getCloudTechnologies()
        );

        System.out.println(
                "Target Roles: "
                        + profile.getTargetRoles()
        );

        System.out.println(
                "Preferred Locations: "
                        + profile.getPreferredLocations()
        );

        System.out.println(
                "Preferred Work Modes: "
                        + profile.getPreferredWorkModes()
        );

        System.out.println(
                "============================="
        );
    }
}
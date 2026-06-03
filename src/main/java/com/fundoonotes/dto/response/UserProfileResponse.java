package com.fundoonotes.dto.response;
import lombok.*; import java.util.Set;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String profileImage;
    private String profileImageUrl;
    private String accountStatus;
    private Set<String> roles;
}

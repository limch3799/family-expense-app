package com.example.d105.domain.user.dto.request;

import lombok.*;

public class TokenRequest{

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public static class CreateTokenRequest{
  private String token;
  private Long groupId;
  private String deviceId;
  private String deviceType;
}

    @Data
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendMessageRequest{
      private String token;
      private String title;
      private String body;
    }

    @Data
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendMessageByGroupRequest{
        private Long groupId;
    }


}

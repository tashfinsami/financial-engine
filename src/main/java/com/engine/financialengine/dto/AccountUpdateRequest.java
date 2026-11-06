package com.engine.financialengine.dto;

import jakarta.validation.constraints.NotBlank;

public class AccountUpdateRequest {

    @NotBlank
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}